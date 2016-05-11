(ns syng-im.handlers
  (:require
    [re-frame.core :refer [register-handler after dispatch debug enrich]]
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
    [syng-im.models.commands :refer [set-commands]]
    [syng-im.handlers.server :as server]
    [syng-im.handlers.contacts :as contacts-service]
    [syng-im.handlers.suggestions :refer [get-command
                                          handle-command
                                          get-command-handler
                                          load-commands
                                          apply-staged-commands
                                          check-suggestion]]
    [syng-im.models.chats :refer [chat-exists?
                                  create-chat
                                  chat-add-participants
                                  chat-remove-participants
                                  set-chat-active
                                  re-join-group-chat
                                  chat-by-id2]]
    [syng-im.utils.logging :as log]
    [syng-im.protocol.api :as api]
    [syng-im.constants :refer [text-content-type
                               content-type-command]]
    [syng-im.navigation :refer [nav-push
                                nav-replace
                                nav-pop]]
    [syng-im.utils.crypt :refer [gen-random-bytes]]
    [syng-im.utils.random :as random]
    syng-im.chat.handlers
    syng-im.navigation.handlers
    syng-im.components.discovery.handlers))

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

(register-handler :set
  (debug
    (fn [db [_ k v]]
      (assoc db k v))))

(register-handler :initialize-db
  (fn [_ _] app-db))

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

(register-handler :load-commands
  (fn [db [action]]
    (log/debug action)
    (load-commands)
    db))

(register-handler :set-commands
  (fn [db [action commands]]
    (log/debug action commands)
    (set-commands db commands)))

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

(defn system-message [msg-id content]
  {:from         "system"
   :msg-id       msg-id
   :content      content
   :content-type text-content-type})

(defn joined-chat-msg [chat-id from msg-id]
  (let [contact-name (:name (contacts/contact-by-identity from))]
    (save-message chat-id {:from         "system"
                           :msg-id       (str msg-id "_" from)
                           :content      (str (or contact-name from) " received chat invitation")
                           :content-type text-content-type})))

(defn participant-invited-to-group-msg [chat-id identity from msg-id]
  (let [inviter-name (:name (contacts/contact-by-identity from))
        invitee-name (if (= identity (api/my-identity))
                       "You"
                       (:name (contacts/contact-by-identity identity)))]
    (save-message chat-id {:from         "system"
                           :msg-id       msg-id
                           :content      (str (or inviter-name from) " invited " (or invitee-name identity))
                           :content-type text-content-type})))

(defn participant-removed-from-group-msg [chat-id identity from msg-id]
  (let [remover-name (:name (contacts/contact-by-identity from))
        removed-name (:name (contacts/contact-by-identity identity))]
    (->> (str (or remover-name from) " removed " (or removed-name identity))
         (system-message msg-id)
         (save-message chat-id))))

(defn you-removed-from-group-msg [chat-id from msg-id]
  (let [remover-name (:name (contacts/contact-by-identity from))]
    (->> (str (or remover-name from) " removed you from group chat")
         (system-message msg-id)
         (save-message chat-id))))

(defn participant-left-group-msg [chat-id from msg-id]
  (let [left-name (:name (contacts/contact-by-identity from))]
    (->> (str (or left-name from) " left")
         (system-message msg-id)
         (save-message chat-id))))

(defn removed-participant-msg [chat-id identity]
  (let [contact-name (:name (contacts/contact-by-identity identity))]
    (->> (str "You've removed " (or contact-name identity))
         (system-message (random/id))
         (save-message chat-id))))

(defn left-chat-msg [chat-id]
  (save-message chat-id {:from         "system"
                         :msg-id       (random/id)
                         :content      "You left this chat"
                         :content-type text-content-type}))

(register-handler :group-chat-invite-acked
  (fn [db [action from group-id ack-msg-id]]
    (log/debug action from group-id ack-msg-id)
    (joined-chat-msg group-id from ack-msg-id)))

(register-handler :participant-removed-from-group
  (fn [db [action from group-id identity msg-id]]
    (log/debug action msg-id from group-id identity)
    (chat-remove-participants group-id [identity])
    (participant-removed-from-group-msg group-id identity from msg-id)))

(register-handler :you-removed-from-group
  (fn [db [action from group-id msg-id]]
    (log/debug action msg-id from group-id)
    (you-removed-from-group-msg group-id from msg-id)
    (set-chat-active group-id false)))

(register-handler :participant-left-group
  (fn [db [action from group-id msg-id]]
    (log/debug action msg-id from group-id)
    (if (= (api/my-identity) from)
      db
      (participant-left-group-msg group-id from msg-id))))

(register-handler :participant-invited-to-group
  (fn [db [action from group-id identity msg-id]]
    (log/debug action msg-id from group-id identity)
    (participant-invited-to-group-msg group-id identity from msg-id)))

(register-handler :acked-msg
  (fn [db [action from msg-id]]
    (log/debug action from msg-id)
    (update-message! {:msg-id          msg-id
                      :delivery-status :delivered})))

(register-handler :msg-delivery-failed
  (fn [db [action msg-id]]
    (log/debug action msg-id)
    (update-message! {:msg-id          msg-id
                      :delivery-status :failed})))

(register-handler :leave-group-chat
  (fn [db [action navigator]]
    (log/debug action)
    (let [chat-id (:current-chat-id db)]
      (api/leave-group-chat chat-id)
      (set-chat-active chat-id false)
      (left-chat-msg chat-id))))

;; -- User data --------------------------------------------------------------

(register-handler :set-user-phone-number
  (fn [db [_ value]]
    (assoc db :user-phone-number value)))

(register-handler :load-user-phone-number
  (fn [db [_]]
    (user-data/load-phone-number)
    db))

;; -- Sign up --------------------------------------------------------------

(register-handler :sync-contacts
  (fn [db [_ handler]]
    (contacts-service/sync-contacts handler)
    db))

;; -- Contacts --------------------------------------------------------------

(register-handler :load-syng-contacts
  (fn [db [_ value]]
    (contacts/load-syng-contacts db)))

;; -- Chats --------------------------------------------------------------
(defn update-new-participants-selection [db identity add?]
  (update db :new-participants (fn [new-participants]
                                 (if add?
                                   (conj new-participants identity)
                                   (disj new-participants identity)))))

(register-handler :select-new-participant
  (fn [db [action identity add?]]
    (log/debug action identity add?)
    (update-new-participants-selection db identity add?)))

(register-handler :remove-selected-participants
  (fn [db [action navigator]]
    (log/debug action)
    (let [identities (vec (:new-participants db))
          chat-id    (:current-chat-id db)]
      (chat-remove-participants chat-id identities)
      (nav-pop navigator)
      (doseq [ident identities]
        (api/group-remove-participant chat-id ident)
        (removed-participant-msg chat-id ident)))))

(register-handler :add-new-participants
  (fn [db [action navigator]]
    (log/debug action)
    (let [identities (vec (:new-participants db))
          chat-id    (:current-chat-id db)]
      (chat-add-participants chat-id identities)
      (nav-pop navigator)
      (doseq [ident identities]
        (api/group-add-participant chat-id ident))
      db)))

(defn update-new-group-selection [db identity add?]
  (update-in db :new-group (fn [new-group]
                             (if add?
                               (conj new-group identity)
                               (disj new-group identity)))))

(register-handler :select-for-new-group
  (fn [db [_ identity add?]]
    (update-new-group-selection db identity add?)))

(register-handler :create-new-group
  (fn [db [action group-name]]
    (log/debug action)
    (let [identities (vec (:new-group db))
          group-id   (api/start-group-chat identities group-name)
          db         (create-chat db group-id identities true group-name)]
      (dispatch [:show-chat group-id :replace])
      db)))

(register-handler :group-chat-invite-received
  (fn [db [action from group-id identities group-name]]
    (log/debug action from group-id identities)
    (if (chat-exists? group-id)
      (re-join-group-chat db group-id identities group-name)
      (create-chat db group-id identities true group-name))))
