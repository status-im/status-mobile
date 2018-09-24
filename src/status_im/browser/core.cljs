(ns status-im.browser.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.browser.permissions :as browser.permissions]
            [status-im.constants :as constants]
            [status-im.data-store.browser :as browser-store]
            [status-im.i18n :as i18n]
            [status-im.js-dependencies :as dependencies]
            [status-im.native-module.core :as status]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.screens.browser.default-dapps :as default-dapps]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.ens :as ens]
            [status-im.utils.ethereum.resolver :as resolver]
            [status-im.utils.fx :as fx]
            [status-im.utils.http :as http]
            [status-im.utils.multihash :as multihash]
            [status-im.utils.platform :as platform]
            [status-im.utils.random :as random]
            [status-im.utils.types :as types]
            [status-im.utils.universal-links.core :as utils.universal-links]
            [taoensso.timbre :as log]))

(fx/defn initialize-browsers
  [{:keys [db all-stored-browsers]}]
  (let [browsers (into {} (map #(vector (:browser-id %) %) all-stored-browsers))]
    {:db (assoc db :browser/browsers browsers)}))

(fx/defn  initialize-dapp-permissions
  [{:keys [db all-dapp-permissions]}]
  (let [dapp-permissions (into {} (map #(vector (:dapp %) %) all-dapp-permissions))]
    {:db (assoc db :dapps/permissions dapp-permissions)}))

(defn get-current-url [{:keys [history history-index]}]
  (when (and history-index history)
    (nth history history-index)))

(defn secure? [{:keys [error? dapp?] :as browser}]
  (or dapp?
      (and (not error?)
           (string/starts-with? (get-current-url browser) "https://"))))

(fx/defn remove-browser
  [{:keys [db]} browser-id]
  {:db            (update-in db [:browser/browsers] dissoc browser-id)
   :data-store/tx [(browser-store/remove-browser-tx browser-id)]})

(defn check-if-dapp-in-list [{:keys [history history-index] :as browser}]
  (let [history-host (http/url-host (try (nth history history-index) (catch js/Error _)))
        dapp         (first (filter #(= history-host (http/url-host (:dapp-url %))) (apply concat (mapv :data default-dapps/all))))]
    (if dapp
      ;;TODO(yenda): the consequence of this is that if user goes to a different
      ;;url from a dapp browser, the name of the browser in the home screen will
      ;;change
      (assoc browser :dapp? true :name (:name dapp))
      (assoc browser :dapp? false :name (i18n/label :t/browser)))))

(defn check-if-phishing-url [{:keys [history history-index] :as browser}]
  (let [history-host (http/url-host (try (nth history history-index) (catch js/Error _)))]
    (assoc browser :unsafe? (dependencies/phishing-detect history-host))))

(fx/defn update-browser
  [{:keys [db now]}
   {:keys [browser-id history history-index error? dapp?] :as browser}]
  (let [updated-browser (-> (assoc browser :timestamp now)
                            (check-if-dapp-in-list)
                            (check-if-phishing-url))]
    {:db            (update-in db
                               [:browser/browsers browser-id]
                               merge updated-browser)
     :data-store/tx [(browser-store/save-browser-tx updated-browser)]}))

(defn get-current-browser [db]
  (get-in db [:browser/browsers (get-in db [:browser/options :browser-id])]))

(defn can-go-back? [{:keys [history-index]}]
  (pos? history-index))

(fx/defn navigate-to-previous-page
  [cofx]
  (let [{:keys [history-index] :as browser} (get-current-browser (:db cofx))]
    (when (can-go-back? browser)
      (update-browser cofx (assoc browser :history-index (dec history-index))))))

(defn can-go-forward? [{:keys [history-index history]}]
  (< history-index (dec (count history))))

(fx/defn navigate-to-next-page
  [cofx]
  (let [{:keys [history-index] :as browser} (get-current-browser (:db cofx))]
    (when (can-go-forward? browser)
      (update-browser cofx (assoc browser :history-index (inc history-index))))))

(fx/defn update-browser-history
  ;; TODO: not clear how this works
  [cofx browser url loading?]
  (when-not loading?
    (let [history-index (:history-index browser)
          history       (:history browser)
          history-url   (get-current-url browser)]
      (when (not= history-url url)
        (let [slash?      (= url (str history-url "/"))
              new-history (if slash?
                            (assoc history history-index url)
                            (conj (subvec history 0 (inc history-index)) url))
              new-index   (if slash?
                            history-index
                            (dec (count new-history)))]
          (update-browser cofx
                          (assoc browser
                                 :history new-history
                                 :history-index new-index)))))))

(defn ens? [host]
  (and (string? host)
       (string/ends-with? host ".eth")))

(defn resolve-ens-multihash-callback [hex]
  (let [hash (when hex (multihash/base58 (multihash/create :sha2-256 (subs hex 2))))]
    (if (and hash (not= hash resolver/default-hash))
      (re-frame/dispatch [:browser.callback/resolve-ens-multihash-success hash])
      (re-frame/dispatch [:browser.callback/resolve-ens-multihash-error]))))

(fx/defn resolve-ens-multihash-success
  [{:keys [db] :as cofx} hash]
  (let [options (:browser/options db)
        browsers (:browser/browsers db)
        browser (get browsers (:browser-id options))
        history-index (:history-index browser)]
    (fx/merge cofx
              {:db (assoc-in db [:browser/options :resolving?] false)}
              (update-browser (assoc-in browser [:history history-index]
                                        (str "https://ipfs.infura.io/ipfs/" hash))))))

(fx/defn resolve-ens-multihash
  [{{:keys [web3 network] :as db} :db} host loading? error?]
  (when (and (not loading?)
             (not error?)
             (ens? host))
    (let [network (get-in db [:account/account :networks network])
          chain   (ethereum/network->chain-keyword network)]
      {:db (assoc-in db [:browser/options :resolving?] true)
       :browser/resolve-ens-multihash {:web3     web3
                                       :registry (get ens/ens-registries
                                                      chain)
                                       :ens-name host
                                       :cb       resolve-ens-multihash-callback}})))

(fx/defn update-browser-option
  [{:keys [db]} option-key option-value]
  {:db (assoc-in db [:browser/options option-key] option-value)})

(fx/defn handle-browser-error
  [cofx]
  (fx/merge cofx
            (update-browser-option :error? true)
            (update-browser-option :loading? false)))

(fx/defn update-browser-on-nav-change
  [cofx browser url loading? error?]
  (when (not= "about:blank" url)
    (let [host (http/url-host url)]
      (fx/merge cofx
                (resolve-ens-multihash host loading? error?)
                (update-browser-history browser url loading?)))))

(fx/defn navigation-state-changed
  [cofx event error?]
  (let [browser (get-current-browser (:db cofx))
        {:strs [url loading]} (js->clj event)]
    (fx/merge cofx
              #(when platform/ios?
                 (update-browser-option % :loading? loading))
              (update-browser-on-nav-change browser url loading error?))))

(fx/defn open-url-in-current-browser
  "Opens a url in the current browser, which mean no new entry is added to the home page
  and history of the current browser is updated so that the user can navigate back to the
  origin url"
  ;; TODO(yenda) is that desirable ?
  [cofx url]
  (let [browser (get-current-browser (:db cofx))
        normalized-url (http/normalize-and-decode-url url)
        host           (http/url-host normalized-url)]
    (fx/merge cofx
              (update-browser-option :url-editing? false)
              (resolve-ens-multihash host false false)
              (update-browser-history browser normalized-url false))))

(fx/defn navigate-to-browser
  [{{:keys [view-id]} :db :as cofx}]
  (if (= view-id :dapp-description)
    (navigation/navigate-reset cofx
                               {:index   1
                                :actions [{:routeName :home}
                                          {:routeName :browser}]})
    (navigation/navigate-to-cofx cofx :browser nil)))

(fx/defn open-url
  "Opens a url in the browser. If a host can be extracted from the url and
  there is already a browser for this host, this browser is reused
  If the browser is reused, the history is flushed"
  [{:keys [db] :as cofx} url]
  (let [normalized-url (http/normalize-and-decode-url url)
        host (http/url-host normalized-url)
        browser {:browser-id    (or host (random/id))
                 :history-index 0
                 :history       [normalized-url]}]
    (fx/merge cofx
              {:db (assoc db :browser/options
                          {:browser-id (:browser-id browser)})}
              (navigate-to-browser)
              (update-browser browser)
              (resolve-ens-multihash host false false))))

(fx/defn open-existing-browser
  "Opens an existing browser with it's history"
  [{:keys [db] :as cofx} browser-id]
  (let [browser (get-in db [:browser/browsers browser-id])]
    (fx/merge cofx
              {:db (assoc db :browser/options
                          {:browser-id browser-id})}
              (update-browser browser)
              (navigation/navigate-to-cofx :browser nil))))

(defn web3-send-async
  [{:keys [method] :as payload} message-id {:keys [db]}]
  (if (or (= method constants/web3-send-transaction)
          (= method constants/web3-personal-sign))
    {:db       (update-in db [:wallet :transactions-queue] conj {:message-id message-id :payload payload})
     ;;TODO(yenda): refactor check-dapps-transactions-queue to remove this dispatch
     :dispatch [:check-dapps-transactions-queue]}
    {:browser/call-rpc [payload
                        #(re-frame/dispatch [:browser.callback/call-rpc
                                             {:type      constants/web3-send-async-callback
                                              :messageId message-id
                                              :error     %1
                                              :result    %2}])]}))

(fx/defn send-to-bridge
  [cofx message]
  {:browser/send-to-bridge {:message message
                            :webview (get-in cofx [:db :webview-bridge])}})

(defn web3-send-async-read-only
  [dapp-name {:keys [method] :as payload} message-id {:keys [db] :as cofx}]
  (let [{:dapps/keys [permissions]} db]
    (if (and (#{"eth_accounts" "eth_coinbase" "eth_sendTransaction" "eth_sign"
                "eth_signTypedData" "personal_sign" "personal_ecRecover"} method)
             (not (some #{"WEB3"} (get-in permissions [dapp-name :permissions]))))
      (send-to-bridge {:type      constants/web3-send-async-callback
                       :messageId message-id
                       :error     "Denied"}
                      cofx)
      (web3-send-async payload message-id cofx))))

(fx/defn handle-scanned-qr-code
  [cofx data message]
  (fx/merge cofx
            (send-to-bridge (assoc message :result data))
            (navigation/navigate-back)))

(fx/defn process-bridge-message
  [{:keys [db] :as cofx} message]
  (let [{:browser/keys [options browsers]} db
        {:keys [browser-id]} options
        browser (get browsers browser-id)
        data    (types/json->clj message)
        {{:keys [url]} :navState :keys [type host permissions payload messageId]} data
        {:keys [dapp? name]} browser
        dapp-name (if dapp? name host)]
    (cond
      (and (= type constants/history-state-changed)
           platform/ios?
           (not= "about:blank" url))
      (update-browser-history browser url false cofx)

      (= type constants/web3-send-async)
      (web3-send-async payload messageId cofx)

      (= type constants/web3-send-async-read-only)
      (web3-send-async-read-only dapp-name payload messageId cofx)

      (= type constants/scan-qr-code)
      (qr-scanner/scan-qr-code cofx
                               {:modal? false}
                               (merge {:handler :browser.bridge.callback/qr-code-scanned}
                                      {:type constants/scan-qr-code-callback
                                       :data data}))

      (= type constants/status-api-request)
      (browser.permissions/process-permissions cofx dapp-name permissions))))

(fx/defn handle-message-link
  [cofx link]
  (if (utils.universal-links/universal-link? link)
    (utils.universal-links/handle-url cofx link)
    {:browser/show-browser-selection link}))

(defn filter-letters-numbers-and-replace-dot-on-dash [value]
  (let [cc (.charCodeAt value 0)]
    (cond (or (and (> cc 96) (< cc 123))
              (and (> cc 64) (< cc 91))
              (and (> cc 47) (< cc 58)))
          value
          (= cc 46)
          "-")))

(fx/defn open-chat-from-browser
  [cofx host]
  (let [topic (string/lower-case (apply str (map filter-letters-numbers-and-replace-dot-on-dash host)))]
    {:dispatch [:create-new-public-chat topic true]}))

(re-frame/reg-fx
 :browser/resolve-ens-multihash
 (fn [{:keys [web3 registry ens-name cb]}]
   (resolver/content web3 registry ens-name cb)))

(re-frame/reg-fx
 :browser/send-to-bridge
 (fn [{:keys [message webview]}]
   (when (and message webview)
     (.sendToBridge webview (types/clj->json message)))))

(re-frame/reg-fx
 :browser/call-rpc
 (fn [[payload callback]]
   (status/call-rpc
    (types/clj->json payload)
    (fn [response]
      (if (= "" response)
        (do
          (log/warn :web3-response-error)
          (callback "web3-response-error" nil))
        (callback nil (.parse js/JSON response)))))))

(re-frame/reg-fx
 :browser/show-browser-selection
 (fn [link]
   (list-selection/browse link)))
