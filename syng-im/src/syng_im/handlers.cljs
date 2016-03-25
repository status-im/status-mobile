(ns syng-im.handlers
  (:require
    [re-frame.core :refer [register-handler after]]
    [schema.core :as s :include-macros true]
    [syng-im.db :refer [app-db schema]]
    [syng-im.protocol.api :refer [init-protocol]]
    [syng-im.protocol.protocol-handler :refer [make-handler]]
    [syng-im.models.protocol :refer [update-identity
                                     set-initialized]]
    [syng-im.models.messages :refer [save-message]]
    [syng-im.models.chat :refer [set-latest-msg-id]]
    [syng-im.utils.logging :as log]
    [syng-im.protocol.api :as api]
    [syng-im.constants :refer [text-content-type]]))

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
    (set-latest-msg-id db chat-id msg-id)))

(register-handler :send-chat-msg
  (fn [db [_ chat-id text]]
    (log/debug "chat-id" chat-id "text" text)
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
      (set-latest-msg-id db chat-id msg-id))))

;; -- Something --------------------------------------------------------------

(register-handler :set-greeting
  (fn [db [_ value]]
    (assoc db :greeting value)))