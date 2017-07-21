(ns status-im.handlers
  (:require
    [re-frame.core :refer [after dispatch dispatch-sync debug reg-fx]]
    [status-im.db :refer [app-db]]
    [status-im.data-store.core :as data-store]
    [taoensso.timbre :as log]
    [status-im.utils.crypt :refer [gen-random-bytes]]
    [status-im.components.status :as status]
    [status-im.components.permissions :as permissions]
    [status-im.utils.handlers :refer [register-handler register-handler-fx] :as u]
    status-im.chat.handlers
    status-im.group-settings.handlers
    status-im.navigation.handlers
    status-im.contacts.events
    status-im.discover.handlers
    status-im.new-group.handlers
    status-im.profile.handlers
    status-im.commands.handlers.loading
    status-im.commands.handlers.jail
    status-im.qr-scanner.handlers
    status-im.accounts.handlers
    status-im.protocol.handlers
    status-im.transactions.handlers
    status-im.network.handlers
    status-im.debug.handlers
    status-im.bots.handlers
    [status-im.utils.types :as t]
    [status-im.i18n :refer [label]]
    [status-im.constants :refer [console-chat-id]]
    [status-im.utils.ethereum-network :as enet]
    [status-im.utils.instabug :as inst]
    [status-im.utils.platform :as p]
    [status-im.js-dependencies :as dependencies]))

;; -- Common --------------------------------------------------------------

(defn set-el [db [_ k v]]
  (assoc db k v))

(register-handler :set set-el)

(defn set-in [db [_ path v]]
  (assoc-in db path v))

(register-handler :set-in set-in)

(reg-fx
  ::init-store
  (fn []
    (data-store/init)))

(register-handler-fx :initialize-db
  (fn [{{:keys [status-module-initialized? status-node-started?
                network-status network first-run _]} :db} _]
    {::init-store nil
     :db (assoc app-db
           :current-account-id nil
           :contacts/contacts {}
           :network-status network-status
           :status-module-initialized? (or p/ios? js/goog.DEBUG status-module-initialized?)
           :status-node-started? status-node-started?
           :network (or network :testnet)
           :first-run (or (nil? first-run) first-run))}))

(register-handler :initialize-account-db
  (fn [db _]
    (-> db
        (assoc :current-chat-id console-chat-id)
        (dissoc :transactions
                :transactions-queue
                :contacts/new-identity))))

(register-handler :initialize-account
  (u/side-effect!
    (fn [_ [_ address]]
      (dispatch [:initialize-account-db])
      (dispatch [:load-processed-messages])
      (dispatch [:initialize-protocol address])
      (dispatch [:initialize-sync-listener])
      (dispatch [:initialize-chats])
      (dispatch [:load-contacts])
      (dispatch [:load-groups])
      (dispatch [:init-chat])
      (dispatch [:init-discoveries])
      (dispatch [:init-debug-mode address])
      (dispatch [:send-account-update-if-needed])
      (dispatch [:start-requesting-discoveries])
      (dispatch [:remove-old-discoveries!])
      (dispatch [:set :creating-account? false]))))

(register-handler :reset-app
  (u/side-effect!
    (fn [{:keys [first-run] :as db} [_ callback]]
      (dispatch [:initialize-db])
      (dispatch [:load-accounts])

      (dispatch [::init-chats! callback]))))

(register-handler ::init-chats!
  (u/side-effect!
    (fn [{:keys [first-run accounts] :as db} [_ callback]]
      (when first-run
        (dispatch [:set :first-run false]))
      (when (or (not first-run) (empty? accounts))
        (dispatch [:init-console-chat])
        (dispatch [:load-commands!])
        (when callback (callback))))))

(register-handler :initialize-crypt
  (u/side-effect!
    (fn [_ _]
      (log/debug "initializing crypt")
      (gen-random-bytes
        1024
        (fn [{:keys [error buffer]}]
          (if error
            (do
              (log/error "Failed to generate random bytes to initialize sjcl crypto")
              (dispatch [:notify-user {:type  :error
                                       :error error}]))
            (do
              (->> (.toString buffer "hex")
                   (.toBits (.. dependencies/eccjs -sjcl -codec -hex))
                   (.addEntropy (.. dependencies/eccjs -sjcl -random)))
              (dispatch [:crypt-initialized]))))))))

(defn node-started [_ _]
  (log/debug "Started Node")
  (enet/get-network #(dispatch [:set :network %])))

(defn move-to-internal-storage [db]
  (status/move-to-internal-storage
    (fn []
      (status/start-node
        (fn [result]
          (node-started db result))))))

(register-handler :initialize-geth
  (u/side-effect!
    (fn [db _]
      (status/should-move-to-internal-storage?
        (fn [should-move?]
          (if should-move?
            (dispatch [:request-permissions
                       [:read-external-storage]
                       #(move-to-internal-storage db)
                       #(dispatch [:move-to-internal-failure-message])])
            (status/start-node (fn [result] (node-started db result)))))))))

(register-handler :webview-geo-permissions-granted
  (u/side-effect!
    (fn [{:keys [webview-bridge]}]
      (.geoPermissionsGranted webview-bridge))))

(register-handler :signal-event
  (u/side-effect!
    (fn [_ [_ event-str]]
      (log/debug :event-str event-str)
      (inst/log (str "Signal event: " event-str))
      (let [{:keys [type event]} (t/json->clj event-str)]
        (case type
          "transaction.queued" (dispatch [:transaction-queued event])
          "transaction.failed" (dispatch [:transaction-failed event])
          "node.started" (dispatch [:status-node-started!])
          "module.initialized" (dispatch [:status-module-initialized!])
          "local_storage.set" (dispatch [:set-local-storage event])
          "request_geo_permissions" (dispatch [:request-permissions [:geolocation]
                                               #(dispatch [:webview-geo-permissions-granted])])
          "jail.send_message" (dispatch [:send-message-from-jail event])
          "jail.show_suggestions" (dispatch [:show-suggestions-from-jail event])
          (log/debug "Event " type " not handled"))))))

(register-handler :status-module-initialized!
  (after (u/side-effect!
           (fn [_]
             (status/module-initialized!))))
  (fn [db]
    (assoc db :status-module-initialized? true)))

(register-handler :status-node-started!
  (fn [db]
    (assoc db :status-node-started? true)))

(register-handler :crypt-initialized
  (u/side-effect!
    (fn [_ _]
      (log/debug "crypt initialized"))))

(register-handler :app-state-change
  (u/side-effect!
    (fn [{:keys [webview-bridge] :as db} [_ state]]
      (case state
        "background" (status/stop-rpc-server)
        "active" (do (status/start-rpc-server)
                     (when webview-bridge
                       (.resetOkHttpClient webview-bridge)))
        nil))))

(register-handler :request-permissions
  (u/side-effect!
    (fn [_ [_ permissions then else]]
      (permissions/request-permissions
        permissions
        then
        else))))

(register-handler :request-geolocation-update
  (u/side-effect!
    (fn [_ _]
      (dispatch [:request-permissions [:geolocation]
                 (fn []
                   (let [watch-id (atom nil)]
                     (.getCurrentPosition
                       navigator.geolocation
                       #(dispatch [:update-geolocation (js->clj % :keywordize-keys true)])
                       #(dispatch [:update-geolocation (js->clj % :keywordize-keys true)])
                       (clj->js {:enableHighAccuracy true :timeout 20000 :maximumAge 1000}))
                     (when p/android?
                       (reset! watch-id
                               (.watchPosition
                                 navigator.geolocation
                                 #(do
                                    (.clearWatch
                                      navigator.geolocation
                                      @watch-id)
                                    (dispatch [:update-geolocation (js->clj % :keywordize-keys true)]))))
                       (dispatch [:set-in [:debug :watch-id] @watch-id]))))]))))

(register-handler :update-geolocation
  (fn [db [_ geolocation]]
    (assoc db :geolocation geolocation)))
