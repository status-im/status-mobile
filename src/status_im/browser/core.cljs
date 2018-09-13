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
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.http :as http]
            [status-im.utils.multihash :as multihash]
            [status-im.utils.platform :as platform]
            [status-im.utils.random :as random]
            [status-im.utils.types :as types]
            [status-im.utils.universal-links.core :as utils.universal-links]
            [taoensso.timbre :as log]))

(defn initialize-browsers
  [{:keys [db all-stored-browsers]}]
  (let [browsers (into {} (map #(vector (:browser-id %) %) all-stored-browsers))]
    {:db (assoc db :browser/browsers browsers)}))

(defn  initialize-dapp-permissions
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

(defn remove-browser [browser-id {:keys [db]}]
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

(defn update-browser
  [{:keys [browser-id history history-index error? dapp?] :as browser}
   {:keys [db now]}]
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

(defn navigate-to-previous-page
  [cofx]
  (let [{:keys [history-index] :as browser} (get-current-browser (:db cofx))]
    (when (can-go-back? browser)
      (update-browser (assoc browser :history-index (dec history-index)) cofx))))

(defn can-go-forward? [{:keys [history-index history]}]
  (< history-index (dec (count history))))

(defn navigate-to-next-page
  [cofx]
  (let [{:keys [history-index] :as browser} (get-current-browser (:db cofx))]
    (when (can-go-forward? browser)
      (update-browser (assoc browser :history-index (inc history-index)) cofx))))

(defn update-browser-history
  ;; TODO: not clear how this works
  [browser url loading? cofx]
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
          (update-browser (assoc browser
                                 :history new-history
                                 :history-index new-index)
                          cofx))))))

(defn ens? [host]
  (and (string? host)
       (string/ends-with? host ".eth")))

(defn resolve-ens-multihash-callback [hex]
  (let [hash (when hex (multihash/base58 (multihash/create :sha2-256 (subs hex 2))))]
    (if (and hash (not= hash resolver/default-hash))
      (re-frame/dispatch [:browser.callback/resolve-ens-multihash-success hash])
      (re-frame/dispatch [:browser.callback/resolve-ens-multihash-error]))))

(defn resolve-ens-multihash-success
  [hash {:keys [db] :as cofx}]
  (let [options (:browser/options db)
        browsers (:browser/browsers db)
        browser (get browsers (:browser-id options))
        history-index (:history-index browser)]
    (handlers-macro/merge-fx
     cofx
     {:db (assoc-in db [:browser/options :resolving?] false)}
     (update-browser (assoc-in browser [:history history-index]
                               (str "https://ipfs.infura.io/ipfs/" hash))))))

(defn resolve-ens-multihash
  [host loading? error? {{:keys [web3 network] :as db} :db}]
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

(defn update-browser-option
  [option-key option-value {:keys [db]}]
  {:db (assoc-in db [:browser/options option-key] option-value)})

(defn handle-browser-error [cofx]
  (handlers-macro/merge-fx cofx
                           (update-browser-option :error? true)
                           (update-browser-option :loading? false)))

(defn update-browser-loading-option
  [loading? cofx]
  ;; TODO(yenda) why are we doing this ?
  (when platform/ios?
    (update-browser-option :loading? loading? cofx)))

(defn update-browser-on-nav-change
  [browser url loading? error? cofx]
  (when (not= "about:blank" url)
    (let [host (http/url-host url)]
      (handlers-macro/merge-fx cofx
                               (resolve-ens-multihash host loading? error?)
                               (update-browser-history browser url loading?)))))

(defn navigation-state-changed [event error? cofx]
  (let [browser (get-current-browser (:db cofx))
        {:strs [url loading]} (js->clj event)]
    (handlers-macro/merge-fx cofx
                             (update-browser-loading-option loading)
                             (update-browser-on-nav-change browser url loading error?))))

(defn open-url-in-current-browser
  "Opens a url in the current browser, which mean no new entry is added to the home page
  and history of the current browser is updated so that the user can navigate back to the
  origin url"
  ;; TODO(yenda) is that desirable ?
  [url cofx]
  (let [browser (get-current-browser (:db cofx))
        normalized-url (http/normalize-and-decode-url url)
        host           (http/url-host normalized-url)]
    (handlers-macro/merge-fx cofx
                             (update-browser-option :url-editing? false)
                             (resolve-ens-multihash host false false)
                             (update-browser-history browser normalized-url false))))

(defn navigate-to-browser
  [{{:keys [view-id]} :db :as cofx}]
  (if (= view-id :dapp-description)
    (navigation/navigate-reset
     {:index   1
      :actions [{:routeName :home}
                {:routeName :browser}]}
     cofx)
    (navigation/navigate-to-cofx :browser nil cofx)))

(defn open-url
  "Opens a url in the browser. If a host can be extracted from the url and
  there is already a browser for this host, this browser is reused
  If the browser is reused, the history is flushed"
  [url {:keys [db] :as cofx}]
  (let [normalized-url (http/normalize-and-decode-url url)
        host (http/url-host normalized-url)
        browser {:browser-id    (or host (random/id))
                 :history-index 0
                 :history       [normalized-url]}]
    (handlers-macro/merge-fx cofx
                             {:db (assoc db :browser/options
                                         {:browser-id (:browser-id browser)})}
                             (navigate-to-browser)
                             (update-browser browser)
                             (resolve-ens-multihash host false false))))

(defn open-existing-browser
  "Opens an existing browser with it's history"
  [browser-id {:keys [db] :as cofx}]
  (let [browser (get-in db [:browser/browsers browser-id])]
    (handlers-macro/merge-fx cofx
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

(defn send-to-bridge [message cofx]
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

(defn handle-scanned-qr-code
  [data message cofx]
  (handlers-macro/merge-fx cofx
                           (send-to-bridge (assoc message :result data))
                           (navigation/navigate-back)))

(defn process-bridge-message
  [message {:keys [db] :as cofx}]
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
      (qr-scanner/scan-qr-code {:modal? false}
                               (merge {:handler :browser.bridge.callback/qr-code-scanned}
                                      {:type constants/scan-qr-code-callback
                                       :data data})
                               cofx)

      (= type constants/status-api-request)
      (browser.permissions/process-permissions dapp-name permissions cofx))))

(defn handle-message-link [link cofx]
  (if (utils.universal-links/universal-link? link)
    (utils.universal-links/handle-url link cofx)
    {:browser/show-browser-selection link}))

(re-frame/reg-fx
 :browser/resolve-ens-multihash
 (fn [{:keys [web3 registry ens-name cb]}]
   (resolver/content web3 registry ens-name cb)))

(re-frame/reg-fx
 :browser/send-to-bridge
 (fn [{:keys [message webview]}]
   (.sendToBridge webview (types/clj->json message))))

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
