(ns status-im.handlers
  (:require
    [re-frame.core :refer [after dispatch dispatch-sync debug]]
    [status-im.db :refer [app-db]]
    [status-im.data-store.core :as data-store]
    [taoensso.timbre :as log]
    [status-im.utils.crypt :refer [gen-random-bytes]]
    [status-im.components.status :as status]
    [status-im.components.permissions :as permissions]
    [status-im.utils.handlers :refer [register-handler] :as u]
    status-im.chat.handlers
    status-im.group-settings.handlers
    status-im.navigation.handlers
    status-im.contacts.handlers
    status-im.discover.handlers
    status-im.new-group.handlers
    status-im.participants.handlers
    status-im.profile.handlers
    status-im.commands.handlers.loading
    status-im.commands.handlers.jail
    status-im.qr-scanner.handlers
    status-im.accounts.handlers
    status-im.protocol.handlers
    status-im.transactions.handlers
    status-im.network.handlers
    status-im.network-settings.handlers
    status-im.debug.handlers
    status-im.bots.handlers
    [status-im.utils.types :as t]
    [status-im.i18n :refer [label]]
    [status-im.constants :refer [console-chat-id]]
    [status-im.utils.ethereum-network :as enet]
    [status-im.utils.instabug :as inst]
    [status-im.utils.platform :as p]
    [status-im.utils.platform :as platform]))

;; -- Common --------------------------------------------------------------

(defn set-el [db [_ k v]]
  (assoc db k v))

(register-handler :set set-el)

(defn set-in [db [_ path v]]
  (assoc-in db path v))

(register-handler :set-in set-in)

(register-handler :initialize-db
  (fn [{:keys [status-module-initialized? status-node-started?
               network-status first-run]} _]
    (data-store/init)
    (assoc app-db :current-account-id nil
                  :contacts {}
                  :network-status network-status
                  :status-module-initialized? (or p/ios? js/goog.DEBUG status-module-initialized?)
                  :status-node-started? status-node-started?
                  :first-run (or (nil? first-run) first-run))))

(register-handler :initialize-account-db
  (fn [db _]
    (-> db
        (assoc :current-chat-id console-chat-id)
        (dissoc :edit-mode
                :transactions
                :transactions-queue
                :new-contact-identity))))

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
      (dispatch [:load-default-networks!])
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

(def ecc (js/require "eccjs"))

(register-handler :initialize-crypt
  (u/side-effect!
    (fn [_ _]
      (log/debug "initializing crypt")
      (gen-random-bytes 1024 (fn [{:keys [error buffer]}]
                               (if error
                                 (do
                                   (log/error "Failed to generate random bytes to initialize sjcl crypto")
                                   (dispatch [:notify-user {:type  :error
                                                            :error error}]))
                                 (do
                                   (->> (.toString buffer "hex")
                                        (.toBits (.. ecc -sjcl -codec -hex))
                                        (.addEntropy (.. ecc -sjcl -random)))
                                   (dispatch [:crypt-initialized]))))))))

(defn move-to-internal-storage [network config]
  (status/move-to-internal-storage
    (fn []
      (dispatch [:set :current-network network])
      (status/start-node config))))

(register-handler :initialize-geth
  (u/side-effect!
    (fn [{:keys [network networks] :as db} [_ config]]
      (let [config' (or config (get-in networks [network :config]))]
        (status/should-move-to-internal-storage?
          (fn [should-move?]
            (if should-move?
              (dispatch [:request-permissions
                         [:read-external-storage]
                         #(move-to-internal-storage network config')
                         #(dispatch [:move-to-internal-failure-message])])
              (do (dispatch [:set :current-network network])
                  (status/start-node config')))))))))

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
          "node.ready" (do (status/after-start!) (dispatch [:status-node-started!]))
          "node.stopped" (status/after-stop!)
          "module.initialized" (dispatch [:status-module-initialized!])
          "local_storage.set" (dispatch [:set-local-storage event])
          "request_geo_permissions" (dispatch [:request-permissions [:geolocation]
                                               #(dispatch [:webview-geo-permissions-granted])])
          (log/debug "Event " type " not handled"))))))

(register-handler :status-module-initialized!
  (after (u/side-effect!
           (fn [_]
             (status/module-initialized!))))
  (fn [db]
    (assoc db :status-module-initialized? true)))

(register-handler :status-node-started!
  (fn [{:keys [on-node-started current-account-id] :as db}]
    (when current-account-id
      (dispatch [:initialize-protocol current-account-id]))
    (when on-node-started (on-node-started))
    (assoc db :status-node-started? true
              :on-node-started nil)))

(register-handler :crypt-initialized
  (u/side-effect!
    (fn [_ _]
      (log/debug "crypt initialized"))))

(register-handler :app-state-change
  (u/side-effect!
    (fn [{:keys [webview-bridge current-network networks
                 was-first-state-active-ios?] :as db}
         [_ state]]
      (case state
        "background" (when platform/android? (status/stop-node))
        "active" (if (or (and was-first-state-active-ios? platform/ios?)
                         platform/android?)
                   (let [config (get-in networks [current-network :config])]
                     (when platform/ios?
                       (status/stop-node))
                     (status/start-node config)
                     (when webview-bridge
                       (.resetOkHttpClient webview-bridge)))
                   (dispatch [:set :was-first-state-active-ios? true]))
        nil))))

(register-handler :request-permissions
  (u/side-effect!
    (fn [_ [_ permissions then else]]
      (permissions/request-permissions
        permissions
        then
        else))))

;; -- User data --------------------------------------------------------------
(register-handler :load-user-phone-number
  (fn [db [_]]
    ;; todo fetch phone number from db
    (assoc db :user-phone-number "123")))
