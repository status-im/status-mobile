(ns syng-im.handlers
  (:require
    [re-frame.core :refer [register-handler after dispatch debug enrich]]
    [schema.core :as s :include-macros true]
    [syng-im.persistence.realm :as r]
    [syng-im.db :refer [app-db schema]]
    [syng-im.persistence.simple-kv-store :as kv]
    [syng-im.protocol.state.storage :as storage]
    [syng-im.db :as db :refer [app-db schema]]
    [syng-im.protocol.api :refer [init-protocol]]
    [syng-im.protocol.protocol-handler :refer [make-handler]]
    [syng-im.models.protocol :refer [update-identity
                                     set-initialized]]
    [syng-im.models.contacts :as contacts]
    [syng-im.models.messages :refer [save-message
                                     update-message!
                                     clear-history]]
    [syng-im.models.commands :refer [set-commands]]
    [syng-im.handlers.server :as server]
    [syng-im.chat.suggestions :refer [load-commands]]
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
    [syng-im.utils.handlers :as u]
    syng-im.chat.handlers
    [syng-im.group-settings.handlers :refer [delete-chat]]
    syng-im.navigation.handlers
    syng-im.contacts.handlers
    syng-im.discovery.handlers
    syng-im.new-group.handlers
    syng-im.participants.handlers))

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

(register-handler :set-in
  (debug
    (fn [db [_ path v]]
      (assoc-in db path v))))

(register-handler :initialize-db
  (fn [_ _]
    (assoc app-db
      :signed-up (storage/get kv/kv-store :signed-up))))

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

(register-handler :crypt-initialized
  (u/side-effect!
    (fn [_ _]
      (log/debug "crypt initialized"))))

(register-handler :load-commands
  (u/side-effect!
    (fn [_ [action]]
      (log/debug action)
      (load-commands))))

(register-handler :set-commands
  (fn [db [action commands]]
    (log/debug action commands)
    (set-commands db commands)))

;; -- Protocol --------------------------------------------------------------

(register-handler :initialize-protocol
  (u/side-effect!
    (fn [db [_]]
      (init-protocol (make-handler db)))))

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
  (u/side-effect!
    (fn [_ [action from group-id ack-msg-id]]
      (log/debug action from group-id ack-msg-id)
      (joined-chat-msg group-id from ack-msg-id))))

(register-handler :participant-removed-from-group
  (u/side-effect!
    (fn [_ [action from group-id identity msg-id]]
      (log/debug action msg-id from group-id identity)
      (chat-remove-participants group-id [identity])
      (participant-removed-from-group-msg group-id identity from msg-id))))

(register-handler :you-removed-from-group
  (u/side-effect!
    (fn [_ [action from group-id msg-id]]
      (log/debug action msg-id from group-id)
      (you-removed-from-group-msg group-id from msg-id)
      (set-chat-active group-id false))))

(register-handler :participant-left-group
  (u/side-effect!
    (fn [_ [action from group-id msg-id]]
      (log/debug action msg-id from group-id)
      (when-not (= (api/my-identity) from)
        (participant-left-group-msg group-id from msg-id)))))

(register-handler :participant-invited-to-group
  (u/side-effect!
    (fn [_ [action from group-id identity msg-id]]
      (log/debug action msg-id from group-id identity)
      (participant-invited-to-group-msg group-id identity from msg-id))))

(register-handler :acked-msg
  (u/side-effect!
    (fn [_ [action from msg-id]]
      (log/debug action from msg-id)
      (update-message! {:msg-id          msg-id
                        :delivery-status :delivered}))))

(register-handler :msg-delivery-failed
  (u/side-effect!
    (fn [_ [action msg-id]]
      (log/debug action msg-id)
      (update-message! {:msg-id          msg-id
                        :delivery-status :failed}))))

(register-handler :leave-group-chat
  (u/side-effect!
    (fn [db [action]]
      (log/debug action)
      (let [chat-id (:current-chat-id db)]
        (api/leave-group-chat chat-id)
        (set-chat-active chat-id false)
        (left-chat-msg chat-id)
        (delete-chat chat-id)
        (dispatch [:navigate-back])))))

;; -- User data --------------------------------------------------------------
(register-handler :load-user-phone-number
  (fn [db [_]]
    ;; todo fetch phone number from db
    (assoc db :user-phone-number "123")))

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
  (fn [db [action]]
    (log/debug action)
    (let [identities (vec (:new-participants db))
          chat-id    (:current-chat-id db)]
      (chat-remove-participants chat-id identities)
      (dispatch [:navigate-back])
      (doseq [ident identities]
        (api/group-remove-participant chat-id ident)
        (removed-participant-msg chat-id ident)))))

(register-handler :add-new-participants
  (fn [db [action navigator]]
    (log/debug action)
    (let [identities (vec (:new-participants db))
          chat-id    (:current-chat-id db)]
      (chat-add-participants chat-id identities)
      (dispatch [:navigate-back])
      (doseq [ident identities]
        (api/group-add-participant chat-id ident))
      db)))

(defn chat-remove-member [db]
  (let [chat     (get-in db [:chats (:current-chat-id db)])
        identity (:group-settings-selected-member db)]
    (r/write
     (fn []
       (r/create :chats
                 (update chat :contacts
                         (fn [members]
                           (filter #(not= (:identity %) identity) members)))
                 true)))
    ;; TODO temp. Update chat in db atom
    (dispatch [:initialize-chats])
    db))


(register-handler :chat-remove-member
  (fn [db [action]]
    (let [chat-id  (:current-chat-id db)
          identity (:group-settings-selected-member db)
          db       (chat-remove-member db)]
      (dispatch [:set :group-settings-selected-member nil])
      ;; TODO fix and uncomment
      (api/group-remove-participant chat-id identity)
      (removed-participant-msg chat-id identity)
      db)))
