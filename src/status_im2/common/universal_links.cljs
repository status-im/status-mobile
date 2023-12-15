(ns status-im2.common.universal-links
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [react-native.async-storage :as async-storage]
    [react-native.core :as rn]
    [schema.core :as schema]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.ethereum.chain :as chain]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

;; domains should be without the trailing slash
(def domains
  {:external "https://status.app"
   :internal "status-app:/"})

(rf/defn handle-browse
  [_ {:keys [url]}]
  (log/info "universal-links: handling browse" url)
  {:browser/show-browser-selection url})

(rf/defn handle-group-chat
  [_ params]
  (log/info "universal-links: handling group" params)
  {:dispatch [:group-chats/create-from-link params]})

(defn own-public-key?
  [{:keys [profile/profile]} public-key]
  (= (:public-key profile) public-key))

(rf/defn handle-private-chat
  [{:keys [db]} {:keys [chat-id]}]
  (log/info "universal-links: handling private chat" chat-id)
  (when chat-id
    (if-not (own-public-key? db chat-id)
      {:dispatch [:chat.ui/start-chat chat-id]}
      {:effects.utils/show-popup {:title   (i18n/label :t/unable-to-read-this-code)
                                  :content (i18n/label :t/can-not-add-yourself)}})))

(rf/defn handle-community-requests
  [cofx {:keys [community-id]}]
  (log/info "universal-links: handling community request  " community-id)
  (navigation/navigate-to cofx :community-requests-to-join {:community-id community-id}))

(rf/defn handle-community
  [cofx {:keys [community-id]}]
  (log/info "universal-links: handling community" community-id)
  (navigation/navigate-to cofx :community {:community-id community-id}))

(rf/defn handle-navigation-to-desktop-community-from-mobile
  {:events [:handle-navigation-to-desktop-community-from-mobile]}
  [cofx deserialized-key]
  (rf/merge
   cofx
   {:dispatch [:navigate-to :community-overview deserialized-key]}
   (navigation/pop-to-root :shell-stack)))

(rf/defn handle-desktop-community
  [_ {:keys [community-id]}]
  (native-module/deserialize-and-compress-key
   community-id
   (fn [deserialized-key]
     (rf/dispatch [:chat.ui/resolve-community-info (str deserialized-key)])
     (rf/dispatch [:handle-navigation-to-desktop-community-from-mobile (str deserialized-key)]))))

(rf/defn handle-community-chat
  [cofx {:keys [chat-id]}]
  (log/info "universal-links: handling community chat" chat-id)
  {:dispatch [:chat/pop-to-root-and-navigate-to-chat chat-id]})

(rf/defn handle-view-profile
  [{:keys [db] :as cofx} {:keys [public-key ens-name]}]
  (log/info "universal-links: handling view profile" public-key)
  (cond
    (and public-key (own-public-key? db public-key))
    (rf/merge cofx
              {:pop-to-root-fx :shell-stack}
              (navigation/navigate-to :my-profile nil))

    public-key
    {:dispatch [:chat.ui/show-profile public-key ens-name]}))

(rf/defn handle-eip681
  [cofx data]
  (rf/merge cofx
            {:dispatch [:wallet-legacy/parse-eip681-uri-and-resolve-ens data true]}
            (navigation/navigate-to :wallet-legacy nil)))

(defn existing-account?
  [{:keys [db]} address]
  (when address
    (some #(when (= (string/lower-case (:address %))
                    (string/lower-case address))
             %)
          (:profile/wallet-accounts db))))

(rf/defn handle-wallet-account
  [cofx {address :account}]
  (when-let [account (existing-account? cofx address)]
    (navigation/navigate-to cofx
                            :wallet-account
                            account)))

(defn handle-not-found
  [full-url]
  (log/info "universal-links: no handler for " full-url))

(defn dispatch-url
  "Dispatch url so we can get access to re-frame/db"
  [url]
  (if-not (nil? url)
    (do
      (async-storage/set-item! (str :chat-id) nil #(rf/dispatch [:universal-links/remove-handling]))
      (re-frame/dispatch [:universal-links/handling])
      (re-frame/dispatch [:universal-links/handle-url url]))
    (log/debug "universal-links: no url")))

(rf/defn on-handle
  {:events [::match-value]}
  [cofx url {:keys [type] :as data}]
  (case type
    :group-chat         (handle-group-chat cofx data)
    :private-chat       (handle-private-chat cofx data)
    :community-requests (handle-community-requests cofx data)
    :community          (handle-community cofx data)
    :desktop-community  (handle-desktop-community cofx data)
    :community-chat     (handle-community-chat cofx data)
    :contact            (handle-view-profile cofx data)
    :browser            (handle-browse cofx data)
    :eip681             (handle-eip681 cofx data)
    :wallet-account     (handle-wallet-account cofx data)
    (handle-not-found url)))

(rf/defn route-url
  "Match a url against a list of routes and handle accordingly"
  [{:keys [db]} url]
  {:router/handle-uri {:chain (chain/chain-keyword db)
                       :chats (:chats db)
                       :uri   url
                       :cb    #(re-frame/dispatch [::match-value url %])}})

(rf/defn store-url-for-later
  "Store the url in the db to be processed on login"
  [{:keys [db]} url]
  (log/info :store-url-for-later)
  {:db (assoc db :universal-links/url url)})

(rf/defn handling
  {:events [:universal-links/handling]}
  [{:keys [db]}]
  {:db (assoc db :universal-links/handling true)})

(rf/defn remove-handling
  {:events [:universal-links/remove-handling]}
  [{:keys [db]}]
  {:db (dissoc db :universal-links/handling)})

(rf/defn handle-url
  "Store url in the database if the user is not logged in, to be processed
  on login, otherwise just handle it."
  {:events [:universal-links/handle-url]}
  [{:keys [db] :as cofx} url]
  (if (:profile/profile db)
    (route-url cofx url)
    (store-url-for-later cofx url)))

(rf/defn process-stored-event
  "Return an event description for processing a url if in the database"
  {:events [:universal-links/process-stored-event]}
  [{:keys [db] :as cofx}]
  (when-let [url (:universal-links/url db)]
    (rf/merge cofx
              {:db (dissoc db :universal-links/url)}
              (handle-url url))))

(defn generate-profile-url
  ([cofx] (generate-profile-url cofx nil))
  ([{:keys [db]} [{:keys [public-key cb]}]]
   (let [profile-public-key (get-in db [:profile/profile :public-key])
         profile?           (or (not public-key) (= public-key profile-public-key))
         ens-name?          (if profile?
                              (get-in db [:profile/profile :ens-name?])
                              (get-in db [:contacts/contacts public-key :ens-name]))
         public-key         (if profile? profile-public-key public-key)]
     (when public-key
       {:json-rpc/call
        [{:method     (if ens-name? "wakuext_shareUserURLWithENS" "wakuext_shareUserURLWithData")
          :params     [public-key]
          :on-success (fn [url]
                        (rf/dispatch [:universal-links/save-profile-url public-key url])
                        (when (fn? cb) (cb)))
          :on-error   #(log/error "failed to wakuext_shareUserURLWithData"
                                  {:error      %
                                   :public-key public-key})}]}))))

(schema/=> generate-profile-url
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:schema
      [:?
       [:map
        [:public-key {:optional true} :schema.common/public-key]
        [:cb {:optional true} fn?]]]]]]
   [:map
    [:json-rpc/call :schema.common/rpc-call]]])

(rf/reg-event-fx :universal-links/generate-profile-url generate-profile-url)

(defn save-profile-url
  [{:keys [db]} [public-key url]]
  (when url
    {:db
     (cond-> db
       (get-in db [:contacts/contacts public-key])
       (assoc-in [:contacts/contacts public-key :universal-profile-url] url)
       (= public-key (get-in db [:profile/profile :public-key]))
       (assoc-in [:profile/profile :universal-profile-url] url))}))

(schema/=> save-profile-url
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:schema [:cat :schema.common/public-key :string]]]]
   [:maybe :map]])

(rf/reg-event-fx :universal-links/save-profile-url save-profile-url)

(defn unwrap-js-url
  [e]
  (-> e
      (js->clj :keywordize-keys true)
      :url))

(def url-event-listener
  (comp dispatch-url unwrap-js-url))

(defn initialize
  "Add an event listener for handling background->foreground transition
  and handles incoming url if the app has been started by clicking on a link"
  []
  (log/debug "universal-links: initializing")
  ;;NOTE: https://github.com/facebook/react-native/issues/15961
  ;; workaround for getInitialURL returning null when opening the
  ;; app from a universal link after closing it with the back button
  (js/setTimeout #(-> (.getInitialURL ^js rn/linking)
                      (.then dispatch-url))
                 200)
  (.addEventListener ^js rn/linking "url" url-event-listener)
  ;;StartSearchForLocalPairingPeers() shouldn't be called ATM from the UI It can be called after the
  ;;error "route ip+net: netlinkrib: permission denied" is fixed on status-go side
  #_(native-module/start-searching-for-local-pairing-peers
     #(log/info "[local-pairing] errors from local-pairing-preflight-outbound-check ->" %)))

(defn finalize
  "Remove event listener for url"
  []
  (log/debug "universal-links: finalizing")
  (.removeEventListener ^js rn/linking "url" url-event-listener))
