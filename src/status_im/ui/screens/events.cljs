(ns status-im.ui.screens.events
  (:require status-im.bots.events
            status-im.chat.handlers
            status-im.commands.handlers.jail
            status-im.commands.handlers.loading
            status-im.debug.handlers
            status-im.network.handlers
            status-im.protocol.handlers
            status-im.transactions.handlers
            status-im.ui.screens.accounts.events
            status-im.ui.screens.contacts.events
            status-im.ui.screens.discover.events
            status-im.ui.screens.group.chat-settings.events
            status-im.ui.screens.group.events
            status-im.ui.screens.navigation
            status-im.ui.screens.profile.events
            status-im.ui.screens.qr-scanner.events
            status-im.ui.screens.wallet.events
            [re-frame.core :refer [dispatch reg-fx]]
            [status-im.components.status :as status]
            [status-im.components.permissions :as permissions]
            [status-im.constants :refer [console-chat-id]]
            [status-im.data-store.core :as data-store]
            [status-im.i18n :as i18n]
            [status-im.js-dependencies :as dependencies]
            [status-im.ui.screens.db :refer [app-db]]
            [status-im.utils.config :as config]
            [status-im.utils.crypt :as crypt]
            [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]
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
                                  [[:received-bot-response
                                    {:chat-id chat-id}
                                    jail-response]])
                          :when event]
                    (dispatch event)))})))

;;;; COFX

;;;; FX

(reg-fx
 :call-jail
 (fn [{:keys [callback-events-creator] :as opts}]
   (status/call-jail
    (-> opts
        (dissoc :callback-events-creator)
        (assoc :callback
               (fn [jail-response]
                 (doseq [event (callback-events-creator jail-response)]
                   (dispatch event))))))))

(reg-fx
 :call-jail-function
 call-jail-function)

(reg-fx
 :call-jail-function-n
 (fn [opts-seq]
   (doseq [opts opts-seq]
     (call-jail-function opts))))

(reg-fx
  ::init-store
  (fn []
    (data-store/init)))

(reg-fx
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

(defn node-started [result]
  (log/debug "Started Node"))

(defn move-to-internal-storage []
  (status/move-to-internal-storage
   (fn []
     (status/start-node node-started))))

(reg-fx
  ::initialize-geth-fx
  (fn []
    (status/should-move-to-internal-storage?
     (fn [should-move?]
       (if should-move?
         (dispatch [:request-permissions
                    [:read-external-storage]
                    #(move-to-internal-storage)
                    #(dispatch [:move-to-internal-failure-message])])
         (status/start-node node-started))))))

(reg-fx
  ::status-module-initialized-fx
  (fn []
    (status/module-initialized!)))

(reg-fx
  ::stop-rpc-server
  (fn []
    (status/stop-rpc-server)))

(reg-fx
  ::start-rpc-server
  (fn []
    (status/start-rpc-server)))

(reg-fx
  ::request-permissions-fx
  (fn [[permissions then else]]
    (permissions/request-permissions permissions then else)))

(reg-fx
  ::testfairy-alert
  (fn []
    (when config/testfairy-enabled?
      (utils/show-popup
       (i18n/label :testfairy-title)
       (i18n/label :testfairy-message)))))

;;;; Handlers

(register-handler-db
  :set
  (fn [db [_ k v]]
    (assoc db k v)))

(register-handler-db
  :set-in
  (fn [db [_ path v]]
    (assoc-in db path v)))

(register-handler-fx
  :initialize-app
  (fn [_ _]
    {::testfairy-alert nil
     :dispatch-n       [[:initialize-db]
                        [:load-accounts]
                        [:check-console-chat]
                        [:listen-to-network-status!]
                        [:initialize-crypt]
                        [:initialize-geth]]}))

(register-handler-fx
  :initialize-db
  (fn [{{:keys [status-module-initialized? status-node-started?
                network-status network _]} :db} _]
    {::init-store nil
     :db (assoc app-db
                :accounts/current-account-id nil
                :contacts/contacts {}
                :network-status network-status
                :status-module-initialized? (or platform/ios? js/goog.DEBUG status-module-initialized?)
                :status-node-started? status-node-started?
                :network (or network "testnet"))}))

(register-handler-db
  :initialize-account-db
  (fn [db _]
    (-> db
        (assoc :current-chat-id console-chat-id)
        (dissoc :transactions
                :transactions-queue
                :contacts/new-identity))))

(register-handler-fx
  :initialize-account
  (fn [_ [_ address]]
    {:dispatch-n [[:initialize-account-db]
                  [:load-processed-messages]
                  [:initialize-protocol address]
                  [:initialize-sync-listener]
                  [:initialize-chats]
                  [:load-contacts]
                  [:load-contact-groups]
                  [:init-chat]
                  [:init-discoveries]
                  [:init-debug-mode address]
                  [:send-account-update-if-needed]
                  [:start-requesting-discoveries]
                  [:remove-old-discoveries!]
                  [:set :accounts/creating-account? false]
                  [:init-wallet]]}))

(register-handler-fx
  :check-console-chat
  (fn [{{:accounts/keys [accounts] :as db} :db} [_ open-console?]]
    (let [view (if (empty? accounts)
                 :chat
                 :accounts)]
      (merge
       {:db (assoc db :view-id view
                   :navigation-stack (list view))}
       (when (or (empty? accounts) open-console?)
         {:dispatch-n (concat
                       [[:init-console-chat]
                        [:load-commands!]]
                       (when open-console?
                         [[:navigate-to :chat console-chat-id]]))})))))

(register-handler-fx
  :initialize-crypt
  (fn [_ _]
    {::initialize-crypt-fx nil}))

(register-handler-fx
  :initialize-geth
  (fn [_ _]
    {::initialize-geth-fx nil}))

(register-handler-fx
  :webview-geo-permissions-granted
  (fn [{{:keys [webview-bridge]} :db} _]
    (.geoPermissionsGranted webview-bridge)))

(register-handler-fx
  :signal-event
  (fn [_ [_ event-str]]
    (log/debug :event-str event-str)
    (inst/log (str "Signal event: " event-str))
    (let [{:keys [type event]} (types/json->clj event-str)]
      (case type
        "transaction.queued"      (dispatch [:transaction-queued event])
        "transaction.failed"      (dispatch [:transaction-failed event])
        "node.started"            (dispatch [:status-node-started])
        "module.initialized"      (dispatch [:status-module-initialized])
        "local_storage.set"       (dispatch [:set-local-storage event])
        "request_geo_permissions" (dispatch [:request-permissions [:geolocation]
                                             #(dispatch [:webview-geo-permissions-granted])])
        "jail.send_message"       (dispatch [:send-message-from-jail event])
        "jail.show_suggestions"   (dispatch [:show-suggestions-from-jail event])
        (log/debug "Event " type " not handled")))))

(register-handler-fx
  :status-module-initialized
  (fn [{:keys [db]} _]
    {:db (assoc db :status-module-initialized? true)
     ::status-module-initialized-fx nil}))

(register-handler-db
  :status-node-started
  (fn [db]
    (assoc db :status-node-started? true)))

(register-handler-fx
  :app-state-change
  (fn [_ [_ state]]
    (case state
      "background" {::stop-rpc-server nil}
      "active" {::start-rpc-server nil}
      nil)))

(register-handler-fx
  :request-permissions
  (fn [_ [_ permissions then else]]
    {::request-permissions-fx [permissions then else]}))

(register-handler-fx
  :request-geolocation-update
  (fn [_ _]
    {:dispatch [:request-permissions [:geolocation]
                (fn []
                  (let [watch-id (atom nil)]
                    (.getCurrentPosition
                     navigator.geolocation
                     #(dispatch [:update-geolocation (js->clj % :keywordize-keys true)])
                     #(dispatch [:update-geolocation (js->clj % :keywordize-keys true)])
                     (clj->js {:enableHighAccuracy true :timeout 20000 :maximumAge 1000}))
                    (when platform/android?
                      (reset! watch-id
                              (.watchPosition
                               navigator.geolocation
                               #(do
                                  (.clearWatch
                                   navigator.geolocation
                                   @watch-id)
                                  (dispatch [:update-geolocation (js->clj % :keywordize-keys true)])))))))]}))

(register-handler-db
  :update-geolocation
  (fn [db [_ geolocation]]
    (assoc db :geolocation geolocation)))
