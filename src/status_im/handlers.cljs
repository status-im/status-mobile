(ns status-im.handlers
  (:require
    [re-frame.core :refer [after dispatch dispatch-sync debug]]
    [status-im.db :refer [app-db]]
    [status-im.data-store.core :as data-store]
    [taoensso.timbre :as log]
    [status-im.utils.crypt :refer [gen-random-bytes]]
    [status-im.components.status :as status]
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
    status-im.debug.handlers
    [status-im.utils.types :as t]
    [status-im.i18n :refer [label]]
    [status-im.constants :refer [console-chat-id]]
    [status-im.utils.ethereum-network :as enet]
    [status-im.utils.instabug :as inst]
    [status-im.utils.platform :as p]))

;; -- Common --------------------------------------------------------------

(defn set-el [db [_ k v]]
  (assoc db k v))

(register-handler :set set-el)

(defn set-in [db [_ path v]]
  (assoc-in db path v))

(register-handler :set-in set-in)

(register-handler :initialize-db
  (fn [{:keys [status-module-initialized? status-node-started?
               network-status network]} _]
    (data-store/init)
    (assoc app-db :current-account-id nil
                  :network-status network-status
                  :status-module-initialized? (or p/ios? js/goog.DEBUG status-module-initialized?)
                  :status-node-started? status-node-started?
                  :network (or network :testnet))))

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
    (fn [_ _]
      (dispatch [:initialize-db])
      (dispatch [:load-accounts])
      (dispatch [:init-console-chat])
      (dispatch [:load-default-contacts!])
      (dispatch [:load-commands!]))))

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

(defn node-started [_ _]
  (log/debug "Started Node")
  (enet/get-network #(dispatch [:set :network %])))

(register-handler :initialize-geth
  (u/side-effect!
    (fn [db _]
      (log/debug "Starting node")
      (status/start-node (fn [result] (node-started db result))))))

(register-handler :signal-event
  (u/side-effect!
    (fn [_ [_ event-str]]
      (log/debug :event-str event-str)
      (inst/log (str "Signal event: " event-str))
      (let [{:keys [type event]} (t/json->clj event-str)]
        (case type
          "transaction.queued" (dispatch [:transaction-queued event])
          "transaction.failed" (dispatch [:transaction-failed event])
          "node.started"       (dispatch [:status-node-started!])
          "module.initialized" (dispatch [:status-module-initialized!])
          "local_storage.set"  (dispatch [:set-local-storage event])
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

;; -- User data --------------------------------------------------------------
(register-handler :load-user-phone-number
  (fn [db [_]]
    ;; todo fetch phone number from db
    (assoc db :user-phone-number "123")))
