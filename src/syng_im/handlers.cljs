(ns syng-im.handlers
  (:require
    [re-frame.core :refer [register-handler after dispatch]]
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
    [syng-im.handlers.commands :refer [set-chat-command
                                       set-chat-command-content]]
    [syng-im.handlers.sign-up :as sign-up-service]

    [syng-im.models.chats :refer [create-chat
                                  chat-add-participants
                                  chat-remove-participants]]
    [syng-im.models.chat :refer [signal-chat-updated
                                 set-current-chat-id
                                 current-chat-id
                                 update-new-group-selection
                                 update-new-participants-selection
                                 clear-new-group
                                 clear-new-participants
                                 new-group-selection
                                 set-chat-input-text
                                 new-participants-selection]]
    [syng-im.utils.logging :as log]
    [syng-im.protocol.api :as api]
    [syng-im.constants :refer [text-content-type]]
    [syng-im.navigation :refer [nav-push
                                nav-replace
                                nav-pop]]
    [syng-im.utils.crypt :refer [gen-random-bytes]]
    [syng-im.utils.random :as random]))

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

(register-handler :initialize-crypt
  (fn [db _]
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
                                 (dispatch [:crypt-initialized])))))
    db))

(register-handler :crypt-initialized
  (fn [db _]
    (log/debug "crypt initialized")
    db))

(register-handler :navigate-to
  (fn [db [action navigator route nav-type]]
    (log/debug action route)
    (case nav-type
      :push (nav-push navigator route)
      :replace (nav-replace navigator route))
    db))

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
  (fn [db [action {chat-id :from
                   msg-id  :msg-id :as msg}]]
    (log/debug action "msg" msg)
    (save-message chat-id msg)
    (-> db
        (create-chat chat-id [chat-id] false)
        (signal-chat-updated chat-id))))

(register-handler :group-received-msg
  (fn [db [action {chat-id :group-id :as msg}]]
    (log/debug action "msg" msg)
    (save-message chat-id msg)
    (signal-chat-updated db chat-id)))

(defn joined-chat-msg [chat-id from msg-id]
  (let [contact-name (:name (contacts/contact-by-identity from))]
    (save-message chat-id {:from         "system"
                           :msg-id       msg-id
                           :content      (str (or contact-name from) " received chat invitation")
                           :content-type text-content-type})))

(defn participant-invited-to-group-msg [chat-id identity from msg-id]
  (let [inviter-name (:name (contacts/contact-by-identity from))
        invitee-name (:name (contacts/contact-by-identity identity))]
    (save-message chat-id {:from         "system"
                           :msg-id       msg-id
                           :content      (str (or inviter-name from) " invited " (or invitee-name identity))
                           :content-type text-content-type})))

(defn removed-participant-msg [chat-id identity]
  (let [contact-name (:name (contacts/contact-by-identity identity))]
    (save-message chat-id {:from         "system"
                           :msg-id       (random/id)
                           :content      (str "You've removed " (or contact-name identity))
                           :content-type text-content-type})))

(register-handler :group-chat-invite-acked
  (fn [db [action from group-id ack-msg-id]]
    (log/debug action from group-id ack-msg-id)
    (joined-chat-msg group-id from ack-msg-id)
    (signal-chat-updated db group-id)))

(register-handler :participant-invited-to-group
  (fn [db [action from group-id identity msg-id]]
    (log/debug action msg-id from group-id identity)
    (participant-invited-to-group-msg group-id identity from msg-id)
    (signal-chat-updated db group-id)))

(register-handler :acked-msg
  (fn [db [action from msg-id]]
    (log/debug action from msg-id)
    (update-message! {:msg-id          msg-id
                      :delivery-status :delivered})
    (signal-chat-updated db from)))

(register-handler :msg-delivery-failed
  (fn [db [action msg-id]]
    (log/debug action msg-id)
    (update-message! {:msg-id          msg-id
                      :delivery-status :failed})
    (let [{:keys [chat-id]} (message-by-id msg-id)]
      (signal-chat-updated db chat-id))))

(register-handler :send-chat-msg
  (fn [db [action chat-id text]]
    (log/debug action "chat-id" chat-id "text" text)
    (let [msg (if (= chat-id "console")
                (sign-up-service/send-console-msg text)
                (let [{msg-id     :msg-id
                       {from :from
                        to   :to} :msg} (api/send-user-msg {:to      chat-id
                                                            :content text})]
                  {:msg-id       msg-id
                   :from         from
                   :to           to
                   :content      text
                   :content-type text-content-type
                   :outgoing     true}))]
      (save-message chat-id msg)
      (signal-chat-updated db chat-id))))

(register-handler :send-chat-command
  (fn [db [action chat-id command content]]
    (log/debug action "chat-id" chat-id "command" command "content" content)
    (let [msg (if (= chat-id "console")
                (sign-up-service/send-console-command command content)
                ;; TODO handle command, now sends as plain message
                (let [{msg-id     :msg-id
                       {from :from
                        to   :to} :msg} (api/send-user-msg {:to      chat-id
                                                            :content content})]
                  {:msg-id       msg-id
                   :from         from
                   :to           to
                   :content      content
                   :content-type text-content-type
                   :outgoing     true}))]
      (save-message chat-id msg)
      (signal-chat-updated db chat-id))))

(register-handler :send-group-chat-msg
  (fn [db [action chat-id text]]
    (log/debug action "chat-id" chat-id "text" text)
    (let [{msg-id       :msg-id
           {from :from} :msg} (api/send-group-user-msg {:group-id chat-id
                                                        :content  text})
          msg {:msg-id       msg-id
               :from         from
               :to           nil
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
  (fn [db [action chat-id navigator nav-type]]
    (log/debug action "chat-id" chat-id)
    (let [db (set-current-chat-id db chat-id)]
      (dispatch [:navigate-to navigator {:view-id :chat} nav-type])
      db)))

(register-handler :set-sign-up-chat
  (fn [db [_]]
    (-> db
        (set-current-chat-id "console")
        sign-up-service/intro)))

;; -- Chat --------------------------------------------------------------

(register-handler :set-chat-input-text
  (fn [db [_ text]]
    (set-chat-input-text db text)))

(register-handler :set-chat-command
  (fn [db [_ command-key]]
    (set-chat-command db command-key)))

(register-handler :set-chat-command-content
  (fn [db [_ content]]
    (set-chat-command-content db content)))

(register-handler :show-contacts
  (fn [db [action navigator]]
    (log/debug action)
    (nav-push navigator {:view-id :contact-list})
    db))

(register-handler :select-new-participant
  (fn [db [action identity add?]]
    (log/debug action identity add?)
    (update-new-participants-selection db identity add?)))

(register-handler :show-remove-participants
  (fn [db [action navigator]]
    (log/debug action)
    (nav-push navigator {:view-id :remove-participants})
    (clear-new-participants db)))

(register-handler :remove-selected-participants
  (fn [db [action navigator]]
    (log/debug action)
    (let [identities (-> (new-participants-selection db)
                         (vec))
          chat-id    (current-chat-id db)]
      (chat-remove-participants chat-id identities)
      (nav-pop navigator)
      (doseq [ident identities]
        (api/group-remove-participant chat-id ident)
        (removed-participant-msg chat-id ident))
      (signal-chat-updated db chat-id))))

(register-handler :show-add-participants
  (fn [db [action navigator]]
    (log/debug action)
    (nav-push navigator {:view-id :add-participants})
    (clear-new-participants db)))

(register-handler :add-new-participants
  (fn [db [action navigator]]
    (log/debug action)
    (let [identities (-> (new-participants-selection db)
                         (vec))
          chat-id    (current-chat-id db)]
      (chat-add-participants chat-id identities)
      (nav-pop navigator)
      (doseq [ident identities]
        (api/group-add-participant chat-id ident))
      db)))

(register-handler :show-group-new
  (fn [db [action navigator]]
    (log/debug action)
    (nav-push navigator {:view-id :new-group})
    (clear-new-group db)))

(register-handler :select-for-new-group
  (fn [db [action identity add?]]
    (log/debug action identity add?)
    (update-new-group-selection db identity add?)))

(register-handler :create-new-group
  (fn [db [action group-name navigator]]
    (log/debug action)
    (let [identities (-> (new-group-selection db)
                         (vec))
          group-id   (api/start-group-chat identities group-name)
          db         (create-chat db group-id identities true group-name)]
      (dispatch [:show-chat group-id navigator :replace])
      db)))

(register-handler :group-chat-invite-received
  (fn [db [action from group-id identities group-name]]
    (log/debug action from group-id identities)
    (create-chat db group-id identities true group-name)))

(comment

  )
