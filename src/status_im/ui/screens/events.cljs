(ns status-im.ui.screens.events
  (:require status-im.bots.events
            status-im.chat.handlers
            status-im.commands.handlers.jail
            status-im.commands.events.loading
            status-im.commands.handlers.debug
            status-im.network.handlers
            status-im.protocol.handlers
            status-im.ui.screens.accounts.events
            status-im.ui.screens.accounts.login.events
            status-im.ui.screens.accounts.recover.events
            status-im.ui.screens.contacts.events
            status-im.ui.screens.discover.events
            status-im.ui.screens.group.chat-settings.events
            status-im.ui.screens.group.events
            status-im.ui.screens.navigation
            status-im.ui.screens.network-settings.events
            status-im.ui.screens.profile.events
            status-im.ui.screens.qr-scanner.events
            status-im.ui.screens.wallet.events
            status-im.ui.screens.wallet.send.events
            status-im.ui.screens.wallet.settings.events
            status-im.ui.screens.wallet.transactions.events
            status-im.ui.screens.wallet.choose-recipient.events
            status-im.ui.screens.browser.events
            [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.permissions :as permissions]
            [status-im.constants :refer [console-chat-id]]
            [status-im.data-store.core :as data-store]
            [status-im.i18n :as i18n]
            [status-im.js-dependencies :as dependencies]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.utils.datetime :as time]
            [status-im.utils.random :as random]
            [status-im.utils.config :as config]
            [status-im.utils.crypt :as crypt]
            [status-im.utils.notifications :as notifications]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.instabug :as inst]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

;;;; Helper fns

(defn- call-jail-function
  [{:keys [chat-id function callback-events-creator] :as opts}]
  (let [path   [:functions function]
        params (select-keys opts [:parameters :context])]
    (status/call-jail
     {:jail-id chat-id
      :path    path
      :params  params
      :callback (fn [jail-response]
                  (doseq [event (if callback-events-creator
                                  (callback-events-creator jail-response)
                                  [[:chat-received-message/bot-response
                                    {:chat-id chat-id}
                                    jail-response]])
                          :when event]
                    (re-frame/dispatch event)))})))

;;;; COFX

(re-frame/reg-cofx
  :now
  (fn [coeffects _]
    (assoc coeffects :now (time/now-ms))))

(re-frame/reg-cofx
  :random-id
  (fn [coeffects _]
    (assoc coeffects :random-id (random/id))))

(re-frame/reg-cofx
  :random-id-seq
  (fn [coeffects _]
    (assoc coeffects :random-id-seq
           ((fn rand-id-seq []
              (cons (random/id) (lazy-seq (rand-id-seq))))))))

;;;; FX

(re-frame/reg-fx
  :call-jail
  (fn [{:keys [callback-events-creator] :as opts}]
    (status/call-jail
     (-> opts
         (dissoc :callback-events-creator)
         (assoc :callback
                (fn [jail-response]
                  (when callback-events-creator
                    (doseq [event (callback-events-creator jail-response)]
                      (re-frame/dispatch event)))))))))

(re-frame/reg-fx
  :call-jail-function
  call-jail-function)

(re-frame/reg-fx
  :call-jail-function-n
  (fn [opts-seq]
    (doseq [opts opts-seq]
      (call-jail-function opts))))

(re-frame/reg-fx
  :http-post
  (fn [{:keys [action data success-event-creator failure-event-creator]}]
    (utils/http-post action
                     data
                     #(re-frame/dispatch (success-event-creator %))
                     #(re-frame/dispatch (failure-event-creator %)))))

(defn- http-get [{:keys [url response-validator success-event-creator failure-event-creator]}]
  (if response-validator
    (utils/http-get url
                    response-validator
                    #(re-frame/dispatch (success-event-creator %))
                    #(re-frame/dispatch (failure-event-creator %)))
    (utils/http-get url
                    #(re-frame/dispatch (success-event-creator %))
                    #(re-frame/dispatch (failure-event-creator %)))))

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
  (fn []
    (data-store/init)))

(re-frame/reg-fx
  ::initialize-crypt-fx
  (fn []
    (crypt/gen-random-bytes
      1024
      (fn [{:keys [error buffer]}]
        (if error
          (log/error "Failed to generate random bytes to initialize sjcl crypto")
          (->> (.toString buffer "hex")
               (.toBits (.. dependencies/eccjs -sjcl -codec -hex))
               (.addEntropy (.. dependencies/eccjs -sjcl -random))))))))

(defn move-to-internal-storage [config]
  (status/move-to-internal-storage
    #(status/start-node config)))

(re-frame/reg-fx
  :initialize-geth-fx
  (fn [config]
    (status/should-move-to-internal-storage?
      (fn [should-move?]
        (if should-move?
          (re-frame/dispatch [:request-permissions
                              [:read-external-storage]
                              #(move-to-internal-storage config)
                              #(re-frame/dispatch [:move-to-internal-failure-message])])
          (status/start-node config))))))

(re-frame/reg-fx
  ::status-module-initialized-fx
  (fn []
    (status/module-initialized!)))

(re-frame/reg-fx
  ::request-permissions-fx
  (fn [[permissions then else]]
    (permissions/request-permissions permissions then else)))

(re-frame/reg-fx
  ::testfairy-alert
  (fn []
    (when config/testfairy-enabled?
      (utils/show-popup
        (i18n/label :testfairy-title)
        (i18n/label :testfairy-message)))))

(re-frame/reg-fx
  ::get-fcm-token-fx
  (fn []
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
  (fn [] (status/close-application)))


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
  :initialize-app
  (fn [_ _]
    {::testfairy-alert nil
     :dispatch-n       [[:initialize-db]
                        [:load-accounts]
                        [:check-console-chat]
                        [:listen-to-network-status!]
                        [:initialize-crypt]
                        [:initialize-geth]]}))

(handlers/register-handler-fx
  :initialize-db
  (fn [{{:keys          [status-module-initialized? status-node-started?
                         network-status network]
         :networks/keys [networks]
         :or {network (get app-db :network)}} :db} _]
    {::init-store nil
     :db          (assoc app-db
                         :accounts/current-account-id nil
                         :contacts/contacts {}
                         :network-status network-status
                         :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
                         :status-node-started? status-node-started?
                         :network network)}))

(handlers/register-handler-db
  :initialize-account-db
  (fn [{:keys [accounts/accounts contacts/contacts networks/networks
               network network-status view-id navigation-stack chats
               access-scope->commands-responses layout-height
               status-module-initialized? status-node-started?]
        :or [network (get app-db :network)]
        :as db} [_ address]]
    (let [console-contact (get contacts console-chat-id)]
      (cond-> (assoc app-db
                     :access-scope->commands-responses access-scope->commands-responses
                     :accounts/current-account-id address
                     :layout-height layout-height
                     ;; TODO (yenda) bad, this is derived data and shouldn't be stored in the db
                     ;; the cost of retrieving public key from db with a function taking using
                     ;; current-account-id is negligeable
                     :current-public-key (:public-key (accounts address))
                     :view-id view-id
                     :navigation-stack navigation-stack
                     :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
                     :status-node-started? status-node-started?
                     :accounts/accounts accounts
                     :accounts/creating-account? false
                     :networks/networks networks
                     :network-status network-status
                     :network network)
        console-contact
        (assoc :contacts/contacts {console-chat-id console-contact})))))

(handlers/register-handler-fx
  :initialize-account
  (fn [_ [_ address events-after]]
    {:dispatch-n (cond-> [[:initialize-account-db address]
                          [:load-processed-messages]
                          [:initialize-protocol address]
                          [:initialize-sync-listener]
                          [:initialize-chats]
                          [:initialize-browsers]
                          [:load-contacts]
                          [:load-contact-groups]
                          [:initialize-debugging {:address address}]
                          [:send-account-update-if-needed]
                          [:init-discoveries]
                          [:update-wallet]
                          [:update-transactions]
                          [:get-fcm-token]]
                   (seq events-after)
                   (into events-after))}))

(handlers/register-handler-fx
  :check-console-chat
  (fn [{{:accounts/keys [accounts] :as db} :db} [_ open-console?]]
    (let [view (if (empty? accounts)
                 :chat
                 :accounts)]
      (merge
       {:db (assoc db
                   :view-id view
                   :navigation-stack (list view))}
       (when (or (empty? accounts) open-console?)
         {:dispatch-n (concat [[:init-console-chat]]
                              (when open-console?
                                [[:navigate-to-chat console-chat-id]]))})))))

(handlers/register-handler-fx
  :initialize-crypt
  (fn [_ _]
    {::initialize-crypt-fx nil}))

(handlers/register-handler-fx
  :initialize-geth
  (fn [{db :db} _]
    (let [{:accounts/keys [current-account-id accounts]} db
          default-networks (:networks/networks db)
          default-network  (:network db)
          {:keys [network networks]} (get accounts current-account-id)
          network-config   (or (get-in networks [network :config])
                               (get-in default-networks [default-network :config]))]
      {:initialize-geth-fx network-config})))

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
      "local-storage" (re-frame/dispatch [:set-local-storage {:chat-id chat_id
                                                              :data    data}])
      "show-suggestions" (re-frame/dispatch [:show-suggestions-from-jail {:chat-id chat_id
                                                                 :markup  data}])
      "send-message" (re-frame/dispatch [:chat-send-message/from-jail {:chat-id chat_id
                                                                       :message data}])
      "handler-result" (let [orig-params (:origParams data)]
                         ;; TODO(janherich): figure out and fix chat_id from event
                         (re-frame/dispatch [:command-handler! (:chat-id orig-params)
                                             (restore-command-ref-keyword orig-params)
                                             {:result {:returned (dissoc data :origParams)}}]))
      (log/debug "Unknown jail signal " event))))

(handlers/register-handler-fx
  :signal-event
  (fn [_ [_ event-str]]
    (log/debug :event-str event-str)
    (inst/log (str "Signal event: " event-str))
    (let [{:keys [type event]} (types/json->clj event-str)]
      (case type
        "transaction.queued"      (re-frame/dispatch [:transaction-queued event])
        "transaction.failed"      (re-frame/dispatch [:transaction-failed event])
        "node.started"            (re-frame/dispatch [:status-node-started])
        "node.stopped"            (re-frame/dispatch [:status-node-stopped])
        "module.initialized"      (re-frame/dispatch [:status-module-initialized])
        "request_geo_permissions" (re-frame/dispatch [:request-permissions [:geolocation]
                                                      #(re-frame/dispatch [:webview-geo-permissions-granted])])
        "jail.signal"             (handle-jail-signal event)
        (log/debug "Event " type " not handled")))))

(handlers/register-handler-fx
  :status-module-initialized
  (fn [{:keys [db]} _]
    {:db                            (assoc db :status-module-initialized? true)
     ::status-module-initialized-fx nil}))

(handlers/register-handler-fx
  :status-node-started
  (fn [{{:node/keys [after-start] :as db} :db}]
    (merge {:db (assoc db :status-node-started? true)}
           (when after-start {:dispatch-n [after-start]}))))

(handlers/register-handler-fx
  :status-node-stopped
  (fn [{{:node/keys [after-stop]} :db}]
    (when after-stop {:dispatch-n [after-stop]})))

(handlers/register-handler-fx
  :app-state-change
  (fn [_ [_ state]]))
;; TODO(rasom): let's not remove this handler, it will be used for
;; pausing node on entering background on android


(handlers/register-handler-fx
  :request-permissions
  (fn [_ [_ permissions then else]]
    {::request-permissions-fx [permissions then else]}))

(handlers/register-handler-fx
  :request-geolocation-update
  (fn [_ _]
    {:dispatch [:request-permissions [:geolocation]
                (fn []
                  (let [watch-id (atom nil)]
                    (.getCurrentPosition
                      react/geolocation
                      #(re-frame/dispatch [:update-geolocation (js->clj % :keywordize-keys true)])
                      #(re-frame/dispatch [:update-geolocation (js->clj % :keywordize-keys true)])
                      (clj->js {:enableHighAccuracy true :timeout 20000 :maximumAge 1000}))
                    (when platform/android?
                      (reset! watch-id
                              (.watchPosition
                                react/geolocation
                                #(do
                                   (.clearWatch
                                     react/geolocation
                                     @watch-id)
                                   (re-frame/dispatch [:update-geolocation (js->clj % :keywordize-keys true)])))))))]}))

(handlers/register-handler-db
  :update-geolocation
  (fn [db [_ geolocation]]
    (assoc db :geolocation geolocation)))
