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
                                     new-message-arrived]]
    [syng-im.handlers.server :as server]
    [syng-im.utils.logging :as log]))

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

;; -- Handlers --------------------------------------------------------------

(register-handler :initialize-db
  (fn [_ _]
    app-db))

;; -- Common --------------------------------------------------------------

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
    (new-message-arrived db chat-id msg-id)))

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

;; -- Contacts --------------------------------------------------------------

(register-handler :load-syng-contacts
  (fn [db [_ value]]
    (contacts/load-syng-contacts db)))

;; -- Something --------------------------------------------------------------

(register-handler :set-greeting
  (fn [db [_ value]]
    (assoc db :greeting value)))
