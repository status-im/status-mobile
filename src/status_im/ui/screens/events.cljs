(ns status-im.ui.screens.events
  (:require status-im.bots.events
            status-im.chat.events
            status-im.commands.handlers.jail
            status-im.commands.events.loading
            status-im.network.events
            [status-im.transport.handlers :as transport.handlers]
            status-im.protocol.handlers
            status-im.ui.screens.accounts.events
            status-im.ui.screens.accounts.login.events
            status-im.ui.screens.accounts.recover.events
            [status-im.ui.screens.contacts.events :as contacts]
            status-im.ui.screens.group.chat-settings.events
            status-im.ui.screens.group.events
            [status-im.ui.screens.navigation :as navigation]
            status-im.ui.screens.add-new.new-chat.navigation
            status-im.ui.screens.network-settings.events
            status-im.ui.screens.profile.events
            status-im.ui.screens.qr-scanner.events
            status-im.ui.screens.wallet.events
            status-im.ui.screens.wallet.collectibles.events
            status-im.ui.screens.wallet.send.events
            status-im.ui.screens.wallet.settings.events
            status-im.ui.screens.wallet.transactions.events
            status-im.ui.screens.wallet.choose-recipient.events
            status-im.ui.screens.browser.events
            status-im.ui.screens.offline-messaging-settings.events
            status-im.ui.screens.bootnodes-settings.events
            status-im.ui.screens.currency-settings.events
            status-im.ui.screens.usage-data.events
            status-im.utils.keychain.events
            [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.ui.components.permissions :as permissions]
            [status-im.constants :as constants]
            [status-im.data-store.core :as data-store]
            [status-im.data-store.realm.core :as realm]
            [status-im.utils.keychain.core :as keychain]
            [status-im.i18n :as i18n]
            [status-im.js-dependencies :as dependencies]
            [status-im.ui.components.react :as react]
            [status-im.transport.core :as transport]
            [status-im.transport.inbox :as inbox]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.utils.datetime :as time]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.random :as random]
            [status-im.utils.config :as config]
            [status-im.utils.notifications :as notifications]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.http :as http]
            [status-im.utils.instabug :as instabug]
            [status-im.utils.mixpanel :as mixpanel]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

;;;; Helper fns

(defn- call-jail-function
  [{:keys [chat-id function callback-event-creator] :as opts}]
  (let [path   [:functions function]
        params (select-keys opts [:parameters :context])]
    (status/call-jail
     {:jail-id chat-id
      :path    path
      :params  params
      :callback (fn [jail-response]
                  (when-let [event (if callback-event-creator
                                     (callback-event-creator jail-response)
                                     [:chat-received-message/bot-response
                                      {:chat-id chat-id}
                                      jail-response])]
                    (re-frame/dispatch event)))})))

;;;; COFX

(re-frame/reg-cofx
 :now
 (fn [coeffects _]
   (assoc coeffects :now (time/timestamp))))

(re-frame/reg-cofx
 :random-id
 (fn [coeffects _]
   (assoc coeffects :random-id (random/id))))

(re-frame/reg-cofx
 :random-id-seq
 (fn [coeffects _]
   (assoc coeffects :random-id-seq (repeatedly random/id))))

;;;; FX

(re-frame/reg-fx
 :call-jail
 (fn [args]
   (doseq [{:keys [callback-event-creator] :as opts} args]
     (status/call-jail
      (-> opts
          (dissoc :callback-event-creator)
          (assoc :callback
                 (fn [jail-response]
                   (when-let [event (callback-event-creator jail-response)]
                     (re-frame/dispatch event)))))))))

(re-frame/reg-fx
 :call-jail-function
 call-jail-function)

(re-frame/reg-fx
 :call-jail-function-n
 (fn [opts-seq]
   (doseq [opts opts-seq]
     (call-jail-function opts))))

(defn- http-get [{:keys [url response-validator success-event-creator failure-event-creator timeout-ms]}]
  (let [on-success #(re-frame/dispatch (success-event-creator %))
        on-error   #(re-frame/dispatch (failure-event-creator %))
        opts       {:valid-response? response-validator
                    :timeout-ms      timeout-ms}]
    (http/get url on-success on-error opts)))

(re-frame/reg-fx
 :http-get
 http-get)

(re-frame/reg-fx
 :http-get-n
 (fn [calls]
   (doseq [call calls]
     (http-get call))))

(re-frame/reg-fx
 ::init-store
 (fn [encryption-key]
   (data-store/init encryption-key)))

(defn move-to-internal-storage [config]
  (status/move-to-internal-storage
   #(status/start-node config)))

(re-frame/reg-fx
 :initialize-geth-fx
 (fn [config]
    ;;TODO get rid of this, because we don't need this anymore
   (status/should-move-to-internal-storage?
    (fn [should-move?]
      (if should-move?
        (re-frame/dispatch [:request-permissions {:permissions [:read-external-storage]
                                                  :on-allowed  #(move-to-internal-storage config)}])
        (status/start-node (types/clj->json config)))))))

(re-frame/reg-fx
 ::status-module-initialized-fx
 (fn [_]
   (status/module-initialized!)))

(re-frame/reg-fx
 :request-permissions-fx
 (fn [options]
   (permissions/request-permissions options)))

(re-frame/reg-fx
 ::request-notifications-fx
 (fn [_]
   (notifications/request-permissions)))

(re-frame/reg-fx
 ::testfairy-alert
 (fn [_]
   (when config/testfairy-enabled?
     (utils/show-popup
      (i18n/label :testfairy-title)
      (i18n/label :testfairy-message)))))

(re-frame/reg-fx
 ::init-device-UUID
 (fn []
   (status/get-device-UUID #(re-frame/dispatch [:set :device-UUID %]))))

(re-frame/reg-fx
 ::get-fcm-token-fx
 (fn [_]
   (notifications/get-fcm-token)))

(re-frame/reg-fx
 :show-error
 (fn [content]
   (utils/show-popup "Error" content)))

(re-frame/reg-fx
 :show-confirmation
 (fn [{:keys [title content confirm-button-text on-accept on-cancel]}]
   (utils/show-confirmation title content confirm-button-text on-accept on-cancel)))

(re-frame/reg-fx
 :close-application
 (fn [_]
   (status/close-application)))

(re-frame/reg-fx
 ::app-state-change-fx
 (fn [state]
   (status/app-state-change state)))

;;;; Handlers

(handlers/register-handler-db
 :set
 (fn [db [_ k v]]
   (assoc db k v)))

(handlers/register-handler-db
 :set-in
 (fn [db [_ path v]]
   (assoc-in db path v)))

(handlers/register-handler-fx
 :initialize-keychain
 (fn [_ _]
   {:get-encryption-key [:initialize-app]}))

(def handle-invalid-key-parameters
  {:title               (i18n/label :invalid-key-title)
   :content             (i18n/label :invalid-key-content)
   :confirm-button-text (i18n/label :invalid-key-confirm)
   :on-cancel           #(.exitApp react/back-handler)
   :on-accept (fn []
                (realm/delete-realms)
                (.. (keychain/reset)
                    (then
                     #(re-frame/dispatch [:initialize-keychain]))))})

(handlers/register-handler-fx
 :initialize-app
 (fn [_ [_ encryption-key error]]
   (if (= error :invalid-key)
     {:show-confirmation handle-invalid-key-parameters}
     {::init-device-UUID nil
      ::testfairy-alert  nil
      :dispatch-n        [[:initialize-db encryption-key]
                          [:load-accounts]
                          [:initialize-views]
                          [:listen-to-network-status]
                          [:initialize-geth]]})))

(handlers/register-handler-fx
 :logout
 (fn [{:keys [db] :as cofx} [this-event encryption-key]]
   (if encryption-key
     (let [{:transport/keys [chats]} db]
       (handlers-macro/merge-fx cofx
                                {:dispatch-n [[:initialize-db encryption-key]
                                              [:load-accounts]
                                              [:listen-to-network-status]
                                              [:navigate-to :accounts]]}
                                (navigation/navigate-to-clean nil)
                                (transport/stop-whisper)))
     {:get-encryption-key [this-event]})))

(handlers/register-handler-fx
 :initialize-db
 (fn [{{:keys          [status-module-initialized? status-node-started?
                        network-status network peers-count peers-summary device-UUID]
        :or {network (get app-db :network)}} :db}
      [_ encryption-key]]
   {::init-store encryption-key
    :db          (assoc app-db
                        :contacts/contacts {}
                        :network-status network-status
                        :peers-count (or peers-count 0)
                        :peers-summary (or peers-summary [])
                        :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
                        :status-node-started? status-node-started?
                        :network network
                        :device-UUID device-UUID)}))

(handlers/register-handler-db
 :initialize-account-db
 (fn [{:keys [accounts/accounts accounts/create contacts/contacts networks/networks
              network network-status peers-count peers-summary view-id navigation-stack
              access-scope->commands-responses
              status-module-initialized? status-node-started? device-UUID]
       :or   [network (get app-db :network)]} [_ address]]
   (let [console-contact (get contacts constants/console-chat-id)
         current-account (accounts address)]
     (cond-> (assoc app-db
                    :access-scope->commands-responses access-scope->commands-responses
                    :current-public-key (:public-key current-account)
                    :view-id view-id
                    :navigation-stack navigation-stack
                    :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
                    :status-node-started? status-node-started?
                    :accounts/create create
                    :networks/networks networks
                    :account/account current-account
                    :network-status network-status
                    :network network
                    :peers-summary peers-summary
                    :peers-count peers-count
                    :device-UUID device-UUID)
       console-contact
       (assoc :contacts/contacts {constants/console-chat-id console-contact})))))

(handlers/register-handler-fx
 :initialize-account
 (fn [_ [_ address events-after]]
   {:dispatch-n (cond-> [[:initialize-account-db address]
                         [:initialize-protocol address]
                         [:fetch-web3-node-version]
                         [:initialize-sync-listener]
                         [:load-contacts]
                         [:initialize-chats]
                         [:initialize-browsers]
                         [:send-account-update-if-needed]
                         [:process-pending-messages]
                         [:update-wallet]
                         [:update-transactions]
                         ;[:get-fcm-token]
                         [:update-sign-in-time]]
                  (seq events-after) (into events-after))}))

(handlers/register-handler-fx
 :initialize-views
 (fn [{{:accounts/keys [accounts] :as db} :db} _]
   {:db (if (empty? accounts)
          (assoc db :view-id :intro :navigation-stack (list :intro))
          (let [{:keys [address photo-path name]} (first (sort-by :last-sign-in > (vals accounts)))]
            (-> db
                (assoc :view-id :login
                       :navigation-stack (list :login))
                (update :accounts/login assoc
                        :address address
                        :photo-path photo-path
                        :name name))))}))

(handlers/register-handler-fx
 :initialize-geth
 (fn [{db :db} _]
   (let [default-networks (:networks/networks db)
         default-network  (:network db)
         {:keys [network networks]} (:account/account db)
         network-config   (or (get-in networks [network :config])
                              (get-in default-networks [default-network :config]))]
     {:initialize-geth-fx network-config})))

(handlers/register-handler-fx
 :fetch-web3-node-version-callback
 (fn [{:keys [db]} [_ resp]]
   (when-let [git-commit (nth (re-find #"-([0-9a-f]{7,})/" resp) 1)]
     {:db (assoc db :web3-node-version git-commit)})))

(handlers/register-handler-fx
 :fetch-web3-node-version
 (fn [{{:keys [web3] :as db} :db} _]
   (.. web3 -version (getNode (fn [err resp]
                                (when-not err
                                  (re-frame/dispatch [:fetch-web3-node-version-callback resp])))))
   nil))

(handlers/register-handler-fx
 :webview-geo-permissions-granted
 (fn [{{:keys [webview-bridge]} :db} _]
   (.geoPermissionsGranted webview-bridge)))

(handlers/register-handler-fx
 :get-fcm-token
 (fn [_ _]
   {::get-fcm-token-fx nil}))

;; Because we send command to jail in params and command `:ref` is a lookup vector with
;; keyword in it (for example `["transactor" :command 51 "send"]`), we lose that keyword
;; information in the process of converting to/from JSON, and we need to restore it
(defn- restore-command-ref-keyword
  [orig-params]
  (if [(get-in orig-params [:command :command :ref])]
    (update-in orig-params [:command :command :ref 1] keyword)
    orig-params))

(defn handle-jail-signal [{:keys [chat_id data]}]
  (let [{:keys [event data]} (types/json->clj data)]
    (case event
      "local-storage"    [:set-local-storage {:chat-id chat_id
                                              :data    data}]
      "show-suggestions" [:show-suggestions-from-jail {:chat-id chat_id
                                                       :markup  data}]
      "send-message"     [:chat-send-message/from-jail {:chat-id chat_id
                                                        :message data}]
      "handler-result"   (let [orig-params (:origParams data)]
                           ;; TODO(janherich): figure out and fix chat_id from event
                           [:command-handler! (:chat-id orig-params)
                            (restore-command-ref-keyword orig-params)
                            {:result {:returned (dissoc data :origParams)}}])
      (log/debug "Unknown jail signal " event))))

(handlers/register-handler-fx
 :discovery/summary
 (fn [{:keys [db] :as cofx} [_ peers-summary]]
   (let [previous-summary (:peers-summary db)
         peers-count      (count peers-summary)]
     (handlers-macro/merge-fx cofx
                              {:db (assoc db
                                          :peers-summary peers-summary
                                          :peers-count peers-count)}
                              (transport.handlers/resend-contact-messages previous-summary)
                              (inbox/peers-summary-change-fx previous-summary)))))

(handlers/register-handler-fx
 :signal-event
 (fn [_ [_ event-str]]
   (log/debug :event-str event-str)
   (instabug/log (str "Signal event: " event-str))
   (let [{:keys [type event]} (types/json->clj event-str)
         to-dispatch (case type
                       "sign-request.queued" [:sign-request-queued event]
                       "sign-request.failed" [:sign-request-failed event]
                       "node.started"        [:status-node-started]
                       "node.stopped"        [:status-node-stopped]
                       "module.initialized"  [:status-module-initialized]
                       "jail.signal"         (handle-jail-signal event)
                       "envelope.sent"       [:signals/envelope-status (:hash event) :sent]
                       "envelope.expired"    [:signals/envelope-status (:hash event) :not-sent]
                       "discovery.summary"   [:discovery/summary event]
                       (log/debug "Event " type " not handled"))]
     (when to-dispatch
       {:dispatch to-dispatch}))))

(handlers/register-handler-fx
 :status-module-initialized
 (fn [{:keys [db]} _]
   {:db                            (assoc db :status-module-initialized? true)
    ::status-module-initialized-fx nil}))

(handlers/register-handler-fx
 :status-node-started
 (fn [{{:node/keys [after-start] :as db} :db} _]
   (merge {:db (assoc db :status-node-started? true)}
          (when after-start {:dispatch-n [after-start]}))))

(handlers/register-handler-fx
 :status-node-stopped
 (fn [{{:node/keys [after-stop]} :db} _]
   (when after-stop {:dispatch-n [after-stop]})))

(handlers/register-handler-fx
 :app-state-change
 (fn [{{:keys [network-status mailserver-status]} :db :as cofx} [_ state]]
   (let [app-coming-from-background? (= state "active")]
     (handlers-macro/merge-fx cofx
                              {::app-state-change-fx state}
                              (inbox/request-messages app-coming-from-background?)))))

(handlers/register-handler-fx
 :request-permissions
 (fn [_ [_ options]]
   {:request-permissions-fx options}))

(handlers/register-handler-fx
 :request-notifications
 (fn [_ _]
   {::request-notifications-fx {}}))

(handlers/register-handler-db
 :set-swipe-position
 [re-frame/trim-v]
 (fn [db [item-id value]]
   (assoc-in db [:chat-animations item-id :delete-swiped] value)))

(handlers/register-handler-db
 :show-tab-bar
 (fn [db _]
   (assoc db :tab-bar-visible? true)))

(handlers/register-handler-db
 :hide-tab-bar
 (fn [db _]
   (assoc db :tab-bar-visible? false)))
