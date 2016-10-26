(ns status-im.handlers
  (:require
    [re-frame.core :refer [after dispatch dispatch-sync debug]]
    [schema.core :as s :include-macros true]
    [status-im.db :refer [app-db schema]]
    [status-im.data-store.core :as data-store]
    [taoensso.timbre :as log]
    [status-im.utils.crypt :refer [gen-random-bytes]]
    [status-im.components.status :as status]
    [status-im.utils.handlers :refer [register-handler] :as u]
    status-im.chat.handlers
    status-im.group-settings.handlers
    status-im.navigation.handlers
    status-im.contacts.handlers
    status-im.discovery.handlers
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
    [status-im.utils.types :as t]
    [status-im.constants :refer [console-chat-id]]))

;; -- Common --------------------------------------------------------------

(defn set-el [db [_ k v]]
  (assoc db k v))

(register-handler :set set-el)

(defn set-in [db [_ path v]]
  (assoc-in db path v))

(register-handler :set-in set-in)

(register-handler :set-animation
  (fn [db [_ k v]]
    (assoc-in db [:animations k] v)))

(register-handler :initialize-db
  (fn [_ _]
    (data-store/init)
    (assoc app-db :current-account-id nil)))

(register-handler :initialize-account-db
  (fn [db _]
    (assoc db
      :chats {}
      :current-chat-id console-chat-id)))

(register-handler :initialize-account
  (u/side-effect!
    (fn [_ [_ address]]
      (dispatch [:initialize-account-db])
      (dispatch [:initialize-protocol address])
      (dispatch [:initialize-sync-listener])
      (dispatch [:initialize-chats])
      (dispatch [:load-contacts])
      (dispatch [:init-chat])
      (dispatch [:init-discoveries])
      (dispatch [:send-account-update-if-needed])
      (dispatch [:remove-old-discoveries!]))))

(register-handler :reset-app
  (u/side-effect!
    (fn [_ _]
      (dispatch [:initialize-db])
      (dispatch [:load-accounts])
      (dispatch [:init-console-chat])
      (dispatch [:init-wallet-chat])
      (dispatch [:load-commands! console-chat-id]))))

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
                                        (.toBits (.. js/ecc -sjcl -codec -hex))
                                        (.addEntropy (.. js/ecc -sjcl -random)))
                                   (dispatch [:crypt-initialized]))))))))

(defn node-started [db result]
  (log/debug "Started Node: "))

(register-handler :initialize-geth
  (u/side-effect!
   (fn [db _]
     (log/debug "Starting node")
     (status/start-node (fn [result] (node-started db result))))))

(register-handler :signal-event
  (u/side-effect!
    (fn [_ [_ event-str]]
      (log/debug :event-str event-str)
      (let [{:keys [type event]} (t/json->clj event-str)]
        (case type
          "transaction.queued" (dispatch [:transaction-queued event])
          "node.started" (log/debug "Event *node.started* received")
          (log/debug "Event " type " not handled"))))))

(register-handler :crypt-initialized
  (u/side-effect!
    (fn [_ _]
      (log/debug "crypt initialized"))))

;; -- User data --------------------------------------------------------------
(register-handler :load-user-phone-number
  (fn [db [_]]
    ;; todo fetch phone number from db
    (assoc db :user-phone-number "123")))
