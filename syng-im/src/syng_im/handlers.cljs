(ns syng-im.handlers
  (:require
    [re-frame.core :refer [register-handler after]]
    [schema.core :as s :include-macros true]
    [syng-im.db :refer [app-db schema]]
    [syng-im.protocol.api :refer [init-protocol]]
    [syng-im.protocol.protocol-handler :refer [make-handler]]
    [syng-im.models.protocol :refer [update-identity
                                     set-initialized]]
    [syng-im.models.user-data :as user-data]
    [syng-im.models.contacts :as contacts]
    [syng-im.models.messages :refer [save-message
                                     update-message!
                                     message-by-id]]
    [syng-im.handlers.server :as server]
    [syng-im.handlers.contacts :as contacts-service]

    [syng-im.models.chats :refer [create-chat]]
    [syng-im.models.chat :refer [signal-chat-updated
                                 set-current-chat-id]]
    [syng-im.utils.logging :as log]
    [syng-im.protocol.api :as api]
    [syng-im.constants :refer [text-content-type]]
    [syng-im.navigation :refer [nav-push]]))

;; -- Middleware ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/wiki/Using-Handler-Middleware
;;
(defn check-and-throw
  "throw an exception if db doesn't match the schema."
  [a-schema db]
  (if-let [problems (s/check a-schema db)]
    (throw (js/Error. (str "schema check failed: " problems)))))

(def validate-schema-mw
  (after (partial check-and-throw schema)))


;; -- Common --------------------------------------------------------------

(register-handler :initialize-db
  (fn [_ _]
    app-db))

(register-handler :set-loading
  (fn [db [_ value]]
    (assoc db :loading value)))

;; -- Protocol --------------------------------------------------------------

(register-handler :initialize-protocol
  (fn [db [_]]
    (init-protocol (make-handler db))
    db))

(register-handler :protocol-initialized
  (fn [db [_ identity]]
    (-> db
        (update-identity identity)
        (set-initialized true))))

(register-handler :received-msg
  (fn [db [_ {chat-id :from
              msg-id  :msg-id :as msg}]]
    (save-message chat-id msg)
    (-> db
        (create-chat chat-id [chat-id])
        (signal-chat-updated chat-id))))

(register-handler :acked-msg
  (fn [db [_ from msg-id]]
    (update-message! {:msg-id          msg-id
                      :delivery-status :delivered})
    (signal-chat-updated db from)))

(register-handler :msg-delivery-failed
  (fn [db [_ msg-id]]
    (update-message! {:msg-id          msg-id
                      :delivery-status :failed})
    (let [{:keys [chat-id]} (message-by-id msg-id)]
      (signal-chat-updated db chat-id))))

(register-handler :send-chat-msg
  (fn [db [action chat-id text]]
    (log/debug action "chat-id" chat-id "text" text)
    (let [{msg-id     :msg-id
           {from :from
            to   :to} :msg} (api/send-user-msg {:to      chat-id
                                                :content text})
          msg {:msg-id       msg-id
               :from         from
               :to           to
               :content      text
               :content-type text-content-type
               :outgoing     true}]
      (save-message chat-id msg)
      (signal-chat-updated db chat-id))))

;; -- User data --------------------------------------------------------------

(register-handler :set-user-phone-number
  (fn [db [_ value]]
    (assoc db :user-phone-number value)))

(register-handler :load-user-phone-number
  (fn [db [_]]
    (user-data/load-phone-number)
    db))

;; -- Sign up --------------------------------------------------------------

(register-handler :sign-up
  (fn [db [_ phone-number whisper-identity handler]]
    (server/sign-up phone-number whisper-identity handler)
    db))

(register-handler :set-confirmation-code
  (fn [db [_ value]]
    (assoc db :confirmation-code value)))

(register-handler :sign-up-confirm
  (fn [db [_ confirmation-code handler]]
    (server/sign-up-confirm confirmation-code handler)
    db))

(register-handler :sync-contacts
  (fn [db [_ handler]]
    (contacts-service/sync-contacts handler)
    db))

;; -- Contacts --------------------------------------------------------------

(register-handler :load-syng-contacts
  (fn [db [_ value]]
    (contacts/load-syng-contacts db)))

;; -- Chats --------------------------------------------------------------

(register-handler :show-chat
  (fn [db [action chat-id navigator]]
    (log/debug action "chat-id" chat-id)
    (nav-push navigator {:view-id :chat})
    (set-current-chat-id db chat-id)))
