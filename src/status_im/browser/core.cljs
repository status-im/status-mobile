(ns status-im.browser.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.browser.permissions :as browser.permissions]
            [status-im.constants :as constants]
            [status-im.data-store.browser :as browser-store]
            [status-im.i18n :as i18n]
            [status-im.js-dependencies :as dependencies]
            [status-im.native-module.core :as status]
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
            [taoensso.timbre :as log]
            [status-im.js-dependencies :as js-dependencies]))

(fx/defn initialize-browsers
  [{:keys [db all-stored-browsers]}]
  (let [browsers (into {} (map #(vector (:browser-id %) %) all-stored-browsers))]
    {:db (assoc db :browser/browsers browsers)}))

(fx/defn  initialize-dapp-permissions
  [{:keys [db all-dapp-permissions]}]
  (let [dapp-permissions (into {} (map #(vector (:dapp %) %) all-dapp-permissions))]
    {:db (assoc db :dapps/permissions dapp-permissions)}))

(fx/defn update-browser-option
  [{:keys [db]} option-key option-value]
  {:db (assoc-in db [:browser/options option-key] option-value)})

(fx/defn update-browser-options
  [{:keys [db]} options]
  {:db (update db :browser/options merge options)})

(defn get-current-browser [db]
  (get-in db [:browser/browsers (get-in db [:browser/options :browser-id])]))

(defn get-current-url [{:keys [history history-index]}]
  (when (and history-index history)
    (nth history history-index)))

(defn secure? [{:keys [error? dapp?]} {:keys [url]}]
  (or dapp?
      (and (not error?)
           (when url
             (string/starts-with? url "https://")))))

(fx/defn remove-browser
  [{:keys [db]} browser-id]
  {:db            (update-in db [:browser/browsers] dissoc browser-id)
   :data-store/tx [(browser-store/remove-browser-tx browser-id)]})

(defn check-if-dapp-in-list [{:keys [history history-index name] :as browser}]
  (let [history-host (http/url-host (try (nth history history-index) (catch js/Error _)))
        dapp         (first (filter #(= history-host (http/url-host (http/normalize-url (:dapp-url %))))
                                    (apply concat (mapv :data default-dapps/all))))]
    (if dapp
      ;;TODO(yenda): the consequence of this is that if user goes to a different
      ;;url from a dapp browser, the name of the browser in the home screen will
      ;;change
      (assoc browser :dapp? true :name (:name dapp))
      (assoc browser :dapp? false :name (or name (i18n/label :t/browser))))))

(defn check-if-phishing-url [{:keys [history history-index] :as browser}]
  (let [history-host (http/url-host (try (nth history history-index) (catch js/Error _)))]
    (cond-> browser history-host (assoc :unsafe? (dependencies/phishing-detect history-host)))))

(defn resolve-ens-content-callback [hex]
  (let [hash (when hex (multihash/base58 (multihash/create :sha2-256 (subs hex 2))))]
    (if (and hash (not= hash resolver/default-hash))
      (re-frame/dispatch [:browser.callback/resolve-ens-multihash-success constants/ipfs-proto-code hash])
      (re-frame/dispatch [:browser.callback/resolve-ens-contenthash]))))

(defn resolve-ens-contenthash-callback [hex]
  (let [proto-code (when hex (subs hex 2 4))
        hash (when hex (multihash/base58 (multihash/create :sha2-256 (subs hex 12))))]
    (if (and proto-code (#{constants/swarm-proto-code constants/ipfs-proto-code} proto-code) hash (not= hash resolver/default-hash))
      (re-frame/dispatch [:browser.callback/resolve-ens-multihash-success proto-code hash])
      (re-frame/dispatch [:browser.callback/resolve-ens-multihash-error]))))

(fx/defn resolve-url
  [{{:keys [web3 network] :as db} :db} {:keys [error? resolved-url]}]
  (when (not error?)
    (let [current-url (get-current-url (get-current-browser db))
          host (http/url-host current-url)]
      (if (and (not resolved-url) (ens/is-valid-eth-name? host))
        (let [network (get-in db [:account/account :networks network])
              chain   (ethereum/network->chain-keyword network)]
          {:db                            (update db :browser/options assoc :resolving? true)
           :browser/resolve-ens-content {:web3     web3
                                         :registry (get ens/ens-registries
                                                        chain)
                                         :ens-name host
                                         :cb       resolve-ens-content-callback}})
        {:db (update db :browser/options assoc :url (or resolved-url current-url) :resolving? false)}))))

(fx/defn resolve-ens-contenthash
  [{{:keys [web3 network] :as db} :db}]
  (let [current-url (get-current-url (get-current-browser db))
        host (http/url-host current-url)]
    (let [network (get-in db [:account/account :networks network])
          chain   (ethereum/network->chain-keyword network)]
      {:db                            (update db :browser/options assoc :resolving? true)
       :browser/resolve-ens-contenthash {:web3     web3
                                         :registry (get ens/ens-registries
                                                        chain)
                                         :ens-name host
                                         :cb       resolve-ens-contenthash-callback}})))

(fx/defn update-browser
  [{:keys [db now]}
   {:keys [browser-id] :as browser}]
  (let [updated-browser (-> (assoc browser :timestamp now)
                            (check-if-dapp-in-list)
                            (check-if-phishing-url))]
    {:db            (update-in db
                               [:browser/browsers browser-id]
                               merge updated-browser)
     :data-store/tx [(browser-store/save-browser-tx updated-browser)]}))

(defn can-go-back? [{:keys [history-index]}]
  (pos? history-index))

(fx/defn navigate-to-previous-page
  [cofx]
  (let [{:keys [history-index] :as browser} (get-current-browser (:db cofx))]
    (when (can-go-back? browser)
      (fx/merge cofx
                (update-browser (assoc browser :history-index (dec history-index)))
                (resolve-url nil)))))

(defn can-go-forward? [{:keys [history-index history]}]
  (< history-index (dec (count history))))

(fx/defn navigate-to-next-page
  [cofx]
  (let [{:keys [history-index] :as browser} (get-current-browser (:db cofx))]
    (when (can-go-forward? browser)
      (fx/merge cofx
                (update-browser (assoc browser :history-index (inc history-index)))
                (resolve-url nil)))))

(fx/defn update-browser-history
  [cofx browser url]
  (let [history-index (:history-index browser)
        history       (:history browser)]
    (let [new-history (conj (subvec history 0 (inc history-index)) url)
          new-index   (dec (count new-history))]
      (update-browser cofx
                      (assoc browser
                             :history new-history
                             :history-index new-index)))))

(fx/defn resolve-ens-multihash-success
  [{:keys [db] :as cofx} proto-code hash]
  (let [current-url (get-current-url (get-current-browser db))
        host (http/url-host current-url)
        path  (subs current-url (+ (.indexOf current-url host) (count host)))
        gateway (if (= constants/ipfs-proto-code proto-code)
                  (let [base32hash (-> (.encode js-dependencies/hi-base32 (alphabase.base58/decode hash))
                                       (string/replace #"=" "")
                                       (string/lower-case))]
                    (str base32hash ".infura.status.im"))
                  (str "swarm-gateways.net/bzz:/" hash))]
    (fx/merge cofx
              {:db (-> (update db :browser/options
                               assoc
                               :url (str "https://" gateway path)
                               :resolving? false)
                       (assoc-in [:browser/options :resolved-ens host] gateway))})))

(fx/defn resolve-ens-multihash-error
  [{:keys [db] :as cofx}]
  (update-browser-options cofx {:url        (get-current-url (get-current-browser db))
                                :resolving? false
                                :error?     true}))

(fx/defn handle-browser-error
  [cofx]
  (fx/merge cofx
            (update-browser-option :error? true)
            (update-browser-option :loading? false)))

(fx/defn handle-pdf
  [_ url]
  (when (and platform/android? (string/ends-with? url ".pdf"))
    {:browser/show-web-browser-selection url}))

(fx/defn handle-message-link
  [cofx link]
  (if (utils.universal-links/universal-link? link)
    (utils.universal-links/handle-url cofx link)
    {:browser/show-browser-selection link}))

(fx/defn update-browser-on-nav-change
  [cofx url error?]
  (let [browser (get-current-browser (:db cofx))
        options (get-in cofx [:db :browser/options])
        current-url (:url options)]
    (when (and (not= "about:blank" url) (not= current-url url) (not= (str current-url "/") url))
      (let [resolved-ens (first (filter #(not= (.indexOf url (second %)) -1) (:resolved-ens options)))
            resolved-url (if resolved-ens (string/replace url (second resolved-ens) (first resolved-ens)) url)]
        (fx/merge cofx
                  (update-browser-history browser resolved-url)
                  (handle-pdf url)
                  (resolve-url {:error? error? :resolved-url (when resolved-ens url)}))))))

(fx/defn update-browser-name
  [cofx title]
  (let [browser (get-current-browser (:db cofx))]
    (when (and (not (:dapp? browser)) title (not (string/blank? title)))
      (update-browser cofx (assoc browser :name title)))))

(fx/defn navigation-state-changed
  [cofx event error?]
  (let [{:strs [url loading title]} (js->clj event)
        deep-link? (utils.universal-links/deep-link? url)]
    (if (utils.universal-links/universal-link? url)
      (when-not (and deep-link? platform/ios?) ;; ios webview handles this
        (utils.universal-links/handle-url cofx url))
      (fx/merge cofx
                (update-browser-option :loading? loading)
                (update-browser-name title)
                (update-browser-on-nav-change url error?)))))

(fx/defn open-url-in-current-browser
  "Opens a url in the current browser, which mean no new entry is added to the home page
  and history of the current browser is updated so that the user can navigate back to the
  origin url"
  [cofx url]
  (let [browser (get-current-browser (:db cofx))
        normalized-url (http/normalize-and-decode-url url)]
    (if (utils.universal-links/universal-link? normalized-url)
      (utils.universal-links/handle-url cofx normalized-url)
      (fx/merge cofx
                (update-browser-option :url-editing? false)
                (update-browser-history browser normalized-url)
                (resolve-url nil)))))

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
    (if (utils.universal-links/universal-link? normalized-url)
      (utils.universal-links/handle-url cofx normalized-url)
      (fx/merge cofx
                {:db (assoc db :browser/options
                            {:browser-id (:browser-id browser)})}
                (navigation/navigate-to-cofx :browser nil)
                (update-browser browser)
                (resolve-url nil)))))

(fx/defn open-existing-browser
  "Opens an existing browser with it's history"
  [{:keys [db] :as cofx} browser-id]
  (let [browser (get-in db [:browser/browsers browser-id])]
    (fx/merge cofx
              {:db (assoc db :browser/options
                          {:browser-id browser-id})}
              (update-browser browser)
              (navigation/navigate-to-cofx :browser nil)
              (resolve-url nil))))

(fx/defn web3-send-async
  [{:keys [db]} {:keys [method] :as payload} message-id]
  (if (or (= constants/web3-send-transaction method)
          (constants/web3-sign-message? method))
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

(fx/defn web3-send-async-read-only
  [{:keys [db] :as cofx} dapp-name {:keys [method] :as payload} message-id]
  (let [{:dapps/keys [permissions]} db]
    (if (and (#{"eth_accounts" "eth_coinbase" "eth_sendTransaction" "eth_sign"
                "eth_signTypedData" "personal_sign" "personal_ecRecover"} method)
             (not (some #{constants/dapp-permission-web3} (get-in permissions [dapp-name :permissions]))))
      (send-to-bridge cofx
                      {:type      constants/web3-send-async-callback
                       :messageId message-id
                       :error     {:code 4100 :message "The requested account has not been authorized by the user."}})
      (web3-send-async cofx payload message-id))))

(fx/defn handle-scanned-qr-code
  [cofx data {:keys [dapp-name permission message-id]}]
  (fx/merge (assoc-in cofx [:db :browser/options :yielding-control?] false)
            (browser.permissions/send-response-to-bridge permission message-id true data)
            (browser.permissions/process-next-permission dapp-name)
            (navigation/navigate-back)))

(fx/defn handle-canceled-qr-code
  [cofx {:keys [dapp-name permission message-id]}]
  (fx/merge (assoc-in cofx [:db :browser/options :yielding-control?] false)
            (browser.permissions/send-response-to-bridge permission message-id true nil)
            (browser.permissions/process-next-permission dapp-name)))

(fx/defn process-bridge-message
  [{:keys [db] :as cofx} message]
  (let [browser (get-current-browser db)
        url-original (get-current-url browser)
        data    (types/json->clj message)
        {{:keys [url]} :navState :keys [type permission payload messageId params]} data
        {:keys [dapp? name]} browser
        dapp-name (if dapp? name (http/url-host url-original))]
    (cond
      (and (= type constants/history-state-changed)
           platform/ios?
           (not= "about:blank" url))
      (fx/merge cofx
                (update-browser-history browser url)
                (resolve-url nil))

      (= type constants/web3-send-async)
      (web3-send-async cofx payload messageId)

      (= type constants/web3-send-async-read-only)
      (web3-send-async-read-only cofx dapp-name payload messageId)

      (= type constants/api-request)
      (browser.permissions/process-permission cofx dapp-name permission messageId params))))

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
    {:dispatch [:chat.ui/start-public-chat topic nil]}))

(re-frame/reg-fx
 :browser/resolve-ens-content
 (fn [{:keys [web3 registry ens-name cb]}]
   (resolver/content web3 registry ens-name cb)))

(re-frame/reg-fx
 :browser/resolve-ens-contenthash
 (fn [{:keys [web3 registry ens-name cb]}]
   (resolver/contenthash web3 registry ens-name cb)))

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

(re-frame/reg-fx
 :browser/show-web-browser-selection
 (fn [link]
   (list-selection/browse-in-web-browser link)))

