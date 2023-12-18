(ns status-im.browser.core
  (:require
    ["eth-phishing-detect" :as eth-phishing-detect]
    [clojure.string :as string]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [react-native.platform :as platform]
    [status-im.bottom-sheet.events :as bottom-sheet]
    [status-im.browser.eip3085 :as eip3085]
    [status-im.browser.eip3326 :as eip3326]
    [status-im.browser.permissions :as browser.permissions]
    [status-im.browser.webview-ref :as webview-ref]
    [status-im.ethereum.ens :as ens]
    [status-im.multiaccounts.update.core :as multiaccounts.update]
    [status-im.signing.core :as signing]
    [status-im.ui.components.list-selection :as list-selection]
    [status-im.utils.deprecated-types :as types]
    [status-im.utils.random :as random]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.events :as chat.events]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.address :as address]
    [utils.debounce :as debounce]
    [utils.ens.core :as utils.ens]
    [utils.ethereum.chain :as chain]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.universal-links :as links]
    [utils.url :as url]))

(rf/defn update-browser-option
  [{:keys [db]} option-key option-value]
  {:db (assoc-in db [:browser/options option-key] option-value)})

(rf/defn update-browser-options
  [{:keys [db]} options]
  {:db (update db :browser/options merge options)})

(defn get-current-browser
  [db]
  (get-in db [:browser/browsers (get-in db [:browser/options :browser-id])]))

(defn get-current-url
  [{:keys [history history-index]
    :or   {history-index 0}}]
  (when history
    (nth history history-index)))

(defn secure?
  [{:keys [error? dapp?]} {:keys [url]}]
  (or dapp?
      (and (not error?)
           (when url
             (string/starts-with? url "https://")))))

(rf/defn remove-browser
  {:events [:browser.ui/remove-browser-pressed]}
  [{:keys [db]} browser-id]
  {:db            (update-in db [:browser/browsers] dissoc browser-id)
   :json-rpc/call [{:method     "wakuext_deleteBrowser"
                    :params     [browser-id]
                    :on-success #()}]})

(rf/defn clear-all-browsers
  {:events [:browser.ui/clear-all-browsers-pressed]}
  [{:keys [db]}]
  {:db            (dissoc db :browser/browsers)
   :json-rpc/call (for [browser-id (keys (get db :browser/browsers))]
                    {:method     "wakuext_deleteBrowser"
                     :params     [browser-id]
                     :on-success #()})})

(defn update-dapp-name
  [{:keys [name] :as browser}]
  (assoc browser :dapp? false :name (or name (i18n/label :t/browser))))

(defn check-if-phishing-url
  [{:keys [history history-index] :as browser}]
  (let [history-host (url/url-host (try (nth history history-index) (catch js/Error _)))]
    (cond-> browser history-host (assoc :unsafe? (eth-phishing-detect history-host)))))

(defn resolve-ens-contenthash-callback
  [url]
  (if (not (string/blank? url))
    (re-frame/dispatch [:browser.callback/resolve-ens-multihash-success url])
    (re-frame/dispatch [:browser.callback/resolve-ens-multihash-error])))

(rf/defn resolve-url
  [{:keys [db]} {:keys [error? resolved-url]}]
  (when (not error?)
    (let [current-url (get-current-url (get-current-browser db))
          host        (url/url-host current-url)]
      (if (and (not resolved-url) (utils.ens/is-valid-eth-name? host))
        {:db                              (update db :browser/options assoc :resolving? true)
         :browser/resolve-ens-contenthash {:chain-id (chain/chain-id db)
                                           :ens-name host
                                           :cb       resolve-ens-contenthash-callback}}
        {:db (update db :browser/options assoc :url (or resolved-url current-url) :resolving? false)}))))

(rf/defn update-browser
  [{:keys [db]}
   {:keys [browser-id] :as browser}]
  (let [updated-browser (-> browser
                            (update-dapp-name)
                            (check-if-phishing-url))]
    {:db            (update-in db
                               [:browser/browsers browser-id]
                               merge
                               updated-browser)
     :json-rpc/call [{:method     "wakuext_addBrowser"
                      :params     [(select-keys updated-browser
                                                [:browser-id :timestamp :name :dapp? :history
                                                 :history-index])]
                      :on-success #()}]}))

(rf/defn store-bookmark
  {:events [:browser/store-bookmark]}
  [{:keys [db]}
   {:keys [url] :as bookmark}]
  {:db            (assoc-in db [:bookmarks/bookmarks url] bookmark)
   :json-rpc/call [{:method     "wakuext_addBookmark"
                    :params     [bookmark]
                    :on-success #()}]})

(rf/defn update-bookmark
  {:events [:browser/update-bookmark]}
  [{:keys [db] :as cofx}
   {:keys [url] :as bookmark}]
  (let [old-bookmark (get-in db [:bookmarks/bookmarks url])
        new-bookmark (merge old-bookmark bookmark)]
    (rf/merge cofx
              {:db            (assoc-in db [:bookmarks/bookmarks url] new-bookmark)
               :json-rpc/call [{:method     "wakuext_updateBookmark"
                                :params     [url bookmark]
                                :on-success #()}]})))

(rf/defn delete-bookmark
  {:events [:browser/delete-bookmark]}
  [{:keys [db] :as cofx}
   url]
  (let [old-bookmark     (get-in db [:bookmarks/bookmarks url])
        removed-bookmark (merge old-bookmark {:removed true})]
    (rf/merge cofx
              {:db            (update db :bookmarks/bookmarks dissoc url)
               :json-rpc/call [{:method     "wakuext_removeBookmark"
                                :params     [url]
                                :on-success #()}]})))

(defn can-go-back?
  [{:keys [history-index]}]
  (pos? history-index))

(rf/defn navigate-to-previous-page
  {:events [:browser.ui/previous-page-button-pressed]}
  [cofx]
  (let [{:keys [history-index] :as browser} (get-current-browser (:db cofx))]
    (when (can-go-back? browser)
      (rf/merge cofx
                (update-browser (assoc browser :history-index (dec history-index)))
                (resolve-url nil)))))

(rf/defn ignore-unsafe
  {:events [:browser/ignore-unsafe]}
  [cofx]
  (let [browser (get-current-browser (:db cofx))
        host    (url/url-host (get-current-url browser))]
    (update-browser cofx (assoc browser :ignore-unsafe host))))

(defn can-go-forward?
  [{:keys [history-index history]}]
  (< history-index (dec (count history))))

(rf/defn navigate-to-next-page
  {:events [:browser.ui/next-page-button-pressed]}
  [cofx]
  (let [{:keys [history-index] :as browser} (get-current-browser (:db cofx))]
    (when (can-go-forward? browser)
      (rf/merge cofx
                (update-browser (assoc browser :history-index (inc history-index)))
                (resolve-url nil)))))

(rf/defn update-browser-history
  [cofx browser url]
  (let [history-index (:history-index browser)
        history       (:history browser)]
    (when history
      (let [new-history (conj (subvec history 0 (inc history-index)) url)
            new-index   (dec (count new-history))]
        (update-browser cofx
                        (assoc browser
                               :history       new-history
                               :history-index new-index))))))

(rf/defn resolve-ens-multihash-success
  {:events [:browser.callback/resolve-ens-multihash-success]}
  [{:keys [db] :as cofx} url]
  (let [current-url (get-current-url (get-current-browser db))
        host        (url/url-host current-url)
        path        (subs current-url (+ (.indexOf ^js current-url host) (count host)))
        gateway     url]
    (rf/merge cofx
              {:db (-> (update db
                               :browser/options
                               assoc
                               :url (str gateway path)
                               :resolving? false)
                       (assoc-in [:browser/options :resolved-ens host] gateway))})))

(rf/defn resolve-ens-multihash-error
  {:events [:browser.callback/resolve-ens-multihash-error]}
  [{:keys [db] :as cofx}]
  (update-browser-options cofx
                          {:url        (get-current-url (get-current-browser db))
                           :resolving? false
                           :error?     true}))

(rf/defn handle-browser-error
  {:events [:browser/error-occured]}
  [cofx]
  (rf/merge cofx
            (update-browser-option :error? true)
            (update-browser-option :loading? false)))

(rf/defn handle-pdf
  [_ url]
  (when (and platform/android? (string/ends-with? url ".pdf"))
    {:browser/show-web-browser-selection url}))

(rf/defn handle-message-link
  {:events [:browser.ui/message-link-pressed]}
  [_ link]
  (when (security/safe-link? link)
    (if (links/universal-link? link)
      {:dispatch [:universal-links/handle-url link]}
      {:browser/show-browser-selection link})))

(rf/defn update-browser-on-nav-change
  [cofx url error?]
  (let [browser     (get-current-browser (:db cofx))
        options     (get-in cofx [:db :browser/options])
        current-url (:url options)]
    (when (and browser
               (not (string/blank? url))
               (not= "about:blank" url)
               (not= current-url url)
               (not= (str current-url "/") url))
      (let [resolved-ens (first (filter (fn [v]
                                          (not= (.indexOf ^js url (second v)) -1))
                                        (:resolved-ens options)))
            resolved-url (if resolved-ens
                           (url/normalize-url (string/replace url
                                                              (second resolved-ens)
                                                              (first resolved-ens)))
                           url)]
        (rf/merge cofx
                  (update-browser-history browser resolved-url)
                  (handle-pdf url)
                  (resolve-url {:error? error? :resolved-url (when resolved-ens url)}))))))

(rf/defn update-browser-name
  [cofx title]
  (let [browser (get-current-browser (:db cofx))]
    (when (and browser (not (:dapp? browser)) title (not (string/blank? title)))
      (update-browser cofx (assoc browser :name title)))))

(rf/defn navigation-state-changed
  {:events [:browser/navigation-state-changed]}
  [cofx event error?]
  (let [{:strs [url loading title]} (js->clj event)
        deep-link?                  (links/deep-link? url)]
    (if (links/universal-link? url)
      (when-not (and deep-link? platform/ios?) ;; ios webview handles this
        {:dispatch [:universal-links/handle-url url]})
      (rf/merge cofx
                (update-browser-option :loading? loading)
                (update-browser-name title)
                (update-browser-on-nav-change url error?)))))

(rf/defn open-url-in-current-browser
  "Opens a url in the current browser, which mean no new entry is added to the home page
  and history of the current browser is updated so that the user can navigate back to the
  origin url"
  {:events [:browser.ui/url-submitted]}
  [cofx url]
  (let [browser        (get-current-browser (:db cofx))
        normalized-url (url/normalize-and-decode-url url)]
    (if (links/universal-link? normalized-url)
      {:dispatch [:universal-links/handle-url normalized-url]}
      (rf/merge cofx
                (update-browser-option :url-editing? false)
                (update-browser-history browser normalized-url)
                (resolve-url nil)))))

(rf/defn open-url
  "Opens a url in the browser. If a host can be extracted from the url and
  there is already a browser for this host, this browser is reused
  If the browser is reused, the history is flushed"
  {:events [:browser.ui/open-url]}
  [{:keys [db] :as cofx} url]
  (let [normalized-url (url/normalize-and-decode-url url)
        browser        {:browser-id    (random/id)
                        :history-index 0
                        :history       [normalized-url]}]
    (if (links/universal-link? normalized-url)
      {:dispatch [:universal-links/handle-url normalized-url]}
      (rf/merge cofx
                {:db (assoc db
                            :browser/options
                            {:browser-id (:browser-id browser)}
                            :browser/screen-id :browser)}
                (navigation/pop-to-root :shell-stack)
                (chat.events/close-chat)
                (navigation/change-tab :browser-stack)
                (update-browser browser)
                (resolve-url nil)))))

(rf/defn open-existing-browser
  "Opens an existing browser with it's history"
  {:events [:browser.ui/browser-item-selected]}
  [{:keys [db] :as cofx} browser-id]
  (let [browser (get-in db [:browser/browsers browser-id])]
    (rf/merge cofx
              {:db (assoc db
                          :browser/options
                          {:browser-id browser-id}
                          :browser/screen-id :browser)}
              (update-browser browser)
              (navigation/change-tab :browser-stack)
              (resolve-url nil))))

(rf/defn open-browser-tabs
  {:events [:browser.ui/open-browser-tabs]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc db :browser/screen-id :browser-tabs)}
            (navigation/change-tab :browser-stack)))

(rf/defn web3-error-callback
  {:events [:browser.dapp/transaction-on-error]}
  [_ message-id message]
  {:browser/send-to-bridge
   {:type      constants/web3-send-async-callback
    :messageId message-id
    :error     message}})

(rf/defn dapp-complete-transaction
  {:events [:browser.dapp/transaction-on-result]}
  [_ message-id id result]
  ;;TODO check and test id
  {:browser/send-to-bridge
   {:type      constants/web3-send-async-callback
    :messageId message-id
    :result    {:jsonrpc "2.0"
                :id      (int id)
                :result  result}}})

(defn utf8-to-hex
  [s]
  (let [hex (native-module/utf8-to-hex (str s))]
    (if (empty? hex)
      nil
      hex)))

(defn normalize-message
  "NOTE (andrey) there is no spec for this, so this implementation just to be compatible with MM"
  [message]
  (if (string/starts-with? message "0x")
    message
    (utf8-to-hex message)))

(defn normalize-sign-message-params
  "NOTE (andrey) we need this function, because params may be mixed up"
  [params typed?]
  (let [[first-param second-param] params]
    (when (and (string? first-param) (string? second-param))
      (cond
        (address/address? first-param)
        [first-param (if typed? second-param (normalize-message second-param))]
        (address/address? second-param)
        [second-param (if typed? first-param (normalize-message first-param))]))))

(rf/defn send-to-bridge
  {:events [:browser.callback/call-rpc]}
  [_ message]
  {:browser/send-to-bridge message})

(defn web3-sign-message?
  [method]
  (#{constants/web3-sign-typed-data constants/web3-sign-typed-data-v3 constants/web3-sign-typed-data-v4
     constants/web3-personal-sign
     constants/web3-eth-sign constants/web3-keycard-sign-typed-data}
   method))

(rf/defn web3-send-async
  [cofx dapp-name {:keys [method params id] :as payload} message-id]
  (let [message?      (web3-sign-message? method)
        dapps-address (get-in cofx [:db :profile/profile :dapps-address])
        typed?        (and (not= constants/web3-personal-sign method)
                           (not= constants/web3-eth-sign method))]
    (if (or message? (= constants/web3-send-transaction method))
      (let [[address data] (cond (and (= method constants/web3-keycard-sign-typed-data)
                                      (not (vector? params)))
                                 ;; We don't use signer argument for keycard sign-typed-data
                                 ["0x0" params]
                                 message?                     (normalize-sign-message-params params
                                                                                             typed?)
                                 :else                        [nil nil])]
        (when (or (not message?) (and address data))
          (signing/sign cofx
                        (merge
                         (if message?
                           {:message {:address  address
                                      :data     data
                                      :v4       (= constants/web3-sign-typed-data-v4 method)
                                      :typed?   typed?
                                      :pinless? (= method constants/web3-keycard-sign-typed-data)
                                      :from     dapps-address}}
                           {:tx-obj (-> params
                                        first
                                        (update :from #(or % dapps-address))
                                        (dissoc :gasPrice))})
                         {:on-result [:browser.dapp/transaction-on-result message-id id]
                          :on-error  [:browser.dapp/transaction-on-error message-id]}))))
      (cond
        (#{"eth_accounts" "eth_coinbase"} method)
        (send-to-bridge
         cofx
         {:type      constants/web3-send-async-callback
          :messageId message-id
          :result    {:jsonrpc "2.0"
                      :id      (int id)
                      :result  (if (= method "eth_coinbase") dapps-address [dapps-address])}})
        (= method "personal_ecRecover")
        {:signing.fx/recover-message {:params       {:message   (first params)
                                                     :signature (second params)}
                                      :on-completed #(re-frame/dispatch
                                                      [:browser.callback/call-rpc
                                                       {:type      constants/web3-send-async-callback
                                                        :messageId message-id
                                                        :result    (types/json->clj %)}])}}
        (= method "wallet_switchEthereumChain")
        (eip3326/handle-switch-ethereum-chain cofx dapp-name id message-id (first params))

        (= method "wallet_addEthereumChain")
        (eip3085/handle-add-ethereum-chain cofx dapp-name id message-id (first params))

        :else
        {:browser/call-rpc [payload
                            #(re-frame/dispatch [:browser.callback/call-rpc
                                                 {:type      constants/web3-send-async-callback
                                                  :messageId message-id
                                                  :error     %1
                                                  :result    %2}])]}))))

(rf/defn handle-no-permissions
  [cofx {:keys [method id]} message-id]
  (if (= method "eth_accounts")
    ;; eth_accounts returns empty array for compatibility with meta-mask
    (send-to-bridge cofx
                    {:type      constants/web3-send-async-callback
                     :messageId message-id
                     :result    {:jsonrpc "2.0"
                                 :id      (int id)
                                 :result  []}})
    (send-to-bridge cofx
                    {:type      constants/web3-send-async-callback
                     :messageId message-id
                     :error     {:code 4100}})))

(def permissioned-method
  #{"eth_accounts" "eth_coinbase" "eth_sendTransaction" "eth_sign"
    "keycard_signTypedData"
    "eth_signTypedData" "personal_sign" "personal_ecRecover"})

(defn has-permissions?
  [{:dapps/keys [permissions]} dapp-name method]
  (boolean
   (and (permissioned-method method)
        (not (some #{constants/dapp-permission-web3} (get-in permissions [dapp-name :permissions]))))))

(rf/defn web3-send-async-read-only
  [{:keys [db] :as cofx} dapp-name {:keys [method] :as payload} message-id]
  (if (has-permissions? db dapp-name method)
    (handle-no-permissions cofx payload message-id)
    (web3-send-async cofx dapp-name payload message-id)))

(rf/defn handle-scanned-qr-code
  {:events [:browser.bridge.callback/qr-code-scanned]}
  [cofx data {{:keys [dapp-name permission message-id]} :data}]
  (rf/merge (assoc-in cofx [:db :browser/options :yielding-control?] false)
            (browser.permissions/send-response-to-bridge permission message-id true data)
            (browser.permissions/process-next-permission dapp-name)
            (navigation/navigate-back)))

(rf/defn handle-canceled-qr-code
  {:events [:browser.bridge.callback/qr-code-canceled]}
  [cofx {{:keys [dapp-name permission message-id]} :data}]
  (rf/merge (assoc-in cofx [:db :browser/options :yielding-control?] false)
            (browser.permissions/send-response-to-bridge permission message-id true nil)
            (browser.permissions/process-next-permission dapp-name)))

(rf/defn process-bridge-message
  {:events [:browser/bridge-message-received]}
  [{:keys [db] :as cofx} message]
  (let [browser                                                                    (get-current-browser
                                                                                    db)
        url-original                                                               (get-current-url
                                                                                    browser)
        data                                                                       (types/json->clj
                                                                                    message)
        {{:keys [url]} :navState :keys [type permission payload messageId params]} data
        {:keys [dapp? name]}                                                       browser
        dapp-name                                                                  (if dapp?
                                                                                     name
                                                                                     (url/url-host
                                                                                      url-original))]
    (cond
      (and (= type constants/history-state-changed)
           (not= "about:blank" url))
      (update-browser-on-nav-change cofx url nil)

      (= type constants/web3-send-async-read-only)
      (web3-send-async-read-only cofx dapp-name payload messageId)

      (= type constants/api-request)
      (browser.permissions/process-permission cofx dapp-name permission messageId params))))

(re-frame/reg-fx
 :browser/resolve-ens-contenthash
 (fn [{:keys [chain-id ens-name cb]}]
   (ens/resource-url chain-id ens-name cb)))

(re-frame/reg-fx
 :browser/send-to-bridge
 (fn [message]
   (let
     [^js webview @webview-ref/webview-ref
      msg
      (str
       "(function() { var __send = function() { if (ReactNativeWebView.onMessage) { ReactNativeWebView.onMessage('"
       (types/clj->json message)
       "');} else {setTimeout(__send, 0)}}; __send();})();")]
     (when (and message webview)
       (.injectJavaScript webview msg)))))

(re-frame/reg-fx
 :browser/call-rpc
 (fn [[payload callback]]
   (native-module/call-rpc
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
   (js/setTimeout #(list-selection/browse link) 500)))

(re-frame/reg-fx
 :browser/show-web-browser-selection
 (fn [link]
   (list-selection/browse-in-web-browser link)))

(re-frame/reg-fx
 :browser/clear-web-data
 (fn []
   (native-module/clear-web-data)))

(defn share-link
  [url]
  (let [link    (links/generate-link :browse :external url)
        message (i18n/label :t/share-dapp-text {:link link})]
    (list-selection/open-share {:message message})))

(rf/defn dapps-account-selected
  {:events [:dapps-account-selected]}
  [{:keys [db] :as cofx} address]
  (rf/merge cofx
            {:browser/clear-web-data nil}
            (bottom-sheet/hide-bottom-sheet-old)
            (browser.permissions/clear-dapps-permissions)
            (multiaccounts.update/multiaccount-update :dapps-address address {})
            #(when (= (:view-id db) :browser)
               (merge (navigation/navigate-back %)
                      {:dispatch [:browser.ui/browser-item-selected
                                  (get-in db [:browser/options :browser-id])]}))))

(rf/defn open-empty-tab
  {:events [:browser.ui/open-empty-tab]}
  [{:keys [db]}]
  (debounce/clear :browser/navigation-state-changed)
  {:db (assoc db :browser/screen-id :empty-tab)})

(rf/defn url-input-pressed
  {:events [:browser.ui/url-input-pressed]}
  [cofx _]
  (update-browser-option cofx :url-editing? true))

(rf/defn url-input-blured
  {:events [:browser.ui/url-input-blured]}
  [cofx]
  (update-browser-option cofx :url-editing? false))

(rf/defn lock-pressed
  {:events [:browser.ui/lock-pressed]}
  [cofx secure?]
  (update-browser-option cofx :show-tooltip (if secure? :secure :not-secure)))

(rf/defn close-tooltip-pressed
  {:events [:browser.ui/close-tooltip-pressed]}
  [cofx]
  (update-browser-option cofx :show-tooltip nil))

(rf/defn loading-started
  {:events [:browser/loading-started]}
  [cofx]
  (update-browser-options cofx {:error? false :loading? true}))

(rf/defn handle-bookmarks
  [{:keys [db]} bookmarks]
  (let [changed-bookmarks (reduce (fn [acc {:keys [url] :as bookmark}]
                                    (assoc acc url bookmark))
                                  {}
                                  bookmarks)
        stored-bookmarks  (get-in db [:bookmarks/bookmarks])]
    {:db (assoc-in db [:bookmarks/bookmarks] (merge stored-bookmarks changed-bookmarks))}))

(rf/defn initialize-dapp-permissions
  {:events [::initialize-dapp-permissions]}
  [{:keys [db]} all-dapp-permissions]
  (let [dapp-permissions (reduce (fn [acc {:keys [dapp] :as dapp-permissions}]
                                   (assoc acc dapp dapp-permissions))
                                 {}
                                 all-dapp-permissions)]
    {:db (assoc db :dapps/permissions dapp-permissions)}))

(rf/defn initialize-browsers
  {:events [::initialize-browsers]}
  [{:keys [db]} all-stored-browsers]
  (let [browsers (reduce (fn [acc {:keys [browser-id] :as browser}]
                           (assoc acc browser-id browser))
                         {}
                         all-stored-browsers)]
    {:db (assoc db :browser/browsers browsers)}))

(rf/defn initialize-bookmarks
  {:events [::initialize-bookmarks]}
  [{:keys [db]} stored-bookmarks]
  (let [bookmarks (reduce (fn [acc {:keys [url] :as bookmark}]
                            (assoc acc url bookmark))
                          {}
                          stored-bookmarks)]
    {:db (assoc db :bookmarks/bookmarks bookmarks)}))

(rf/defn initialize-browser
  [_]
  {:json-rpc/call
   [{:method     "wakuext_getBrowsers"
     :on-success #(re-frame/dispatch [::initialize-browsers %])}
    {:method     "browsers_getBookmarks"
     :on-success #(re-frame/dispatch [::initialize-bookmarks %])}
    {:method     "permissions_getDappPermissions"
     :on-success #(re-frame/dispatch [::initialize-dapp-permissions %])}]})
