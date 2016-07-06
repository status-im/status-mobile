; todo everything inside this namespace must be revievew in common with future
; changes in protocol lib
(ns status-im.protocol.handlers
  (:require [status-im.utils.handlers :as u]
            [status-im.utils.logging :as log]
            [status-im.protocol.api :as api]
            [re-frame.core :refer [dispatch after]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.models.contacts :as contacts]
            [status-im.protocol.api :refer [init-protocol]]
            [status-im.protocol.protocol-handler :refer [make-handler]]
            [status-im.models.protocol :refer [update-identity
                                               set-initialized]]
            [status-im.constants :refer [text-content-type]]
            [status-im.models.messages :as messages]
            [status-im.models.chats :as chats]
            [status-im.i18n :refer [label]]))

(register-handler :initialize-protocol
  (u/side-effect!
    (fn [db [_ account]]
      (init-protocol account (make-handler db)))))

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
    (messages/save-message chat-id {:from         "system"
                                    :msg-id       (str msg-id "_" from)
                                    :content      (str (or contact-name from) " " (label :t/received-invitation))
                                    :content-type text-content-type})))

(defn participant-invited-to-group-msg [chat-id identity from msg-id]
  (let [inviter-name (:name (contacts/contact-by-identity from))
        invitee-name (if (= identity (api/my-identity))
                       (label :t/You)
                       (:name (contacts/contact-by-identity identity)))]
    (messages/save-message chat-id {:from         "system"
                                    :msg-id       msg-id
                                    :content      (str (or inviter-name from) " " (label :t/invited) " " (or invitee-name identity))
                                    :content-type text-content-type})))

(defn participant-removed-from-group-msg [chat-id identity from msg-id]
  (let [remover-name (:name (contacts/contact-by-identity from))
        removed-name (:name (contacts/contact-by-identity identity))]
    (->> (str (or remover-name from) " " (label :t/removed) " " (or removed-name identity))
         (system-message msg-id)
         (messages/save-message chat-id))))

(defn you-removed-from-group-msg [chat-id from msg-id]
  (let [remover-name (:name (contacts/contact-by-identity from))]
    (->> (str (or remover-name from) " " (label :t/removed-from-chat))
         (system-message msg-id)
         (messages/save-message chat-id))))

(defn participant-left-group-msg [chat-id from msg-id]
  (let [left-name (:name (contacts/contact-by-identity from))]
    (->> (str (or left-name from) " " (label :t/left))
         (system-message msg-id)
         (messages/save-message chat-id))))

(register-handler :group-chat-invite-acked
  (u/side-effect!
    (fn [_ [action from group-id ack-msg-id]]
      (log/debug action from group-id ack-msg-id)
      (joined-chat-msg group-id from ack-msg-id))))

(register-handler :participant-removed-from-group
  (u/side-effect!
    (fn [_ [action from group-id identity msg-id]]
      (log/debug action msg-id from group-id identity)
      (chats/chat-remove-participants group-id [identity])
      (participant-removed-from-group-msg group-id identity from msg-id))))

(register-handler :you-removed-from-group
  (u/side-effect!
    (fn [_ [action from group-id msg-id]]
      (log/debug action msg-id from group-id)
      (you-removed-from-group-msg group-id from msg-id)
      (chats/set-chat-active group-id false))))

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

(defn update-message! [status]
  (fn [_ [_ _ msg-id]]
    (messages/update-message! {:msg-id          msg-id
                               :delivery-status status})))

(defn update-message-status [status]
  (fn [db [_ from msg-id]]
    (let [current-status (get-in db [:message-status from msg-id])]
      (if-not (= :seen current-status)
        (assoc-in db [:message-status from msg-id] status)
        db))))

(register-handler :acked-msg
  (after (update-message! :delivered))
  (update-message-status :delivered))

(register-handler :msg-delivery-failed
  (after (update-message! :failed))
  (update-message-status :failed))

;; todo maybe it is fine to treat as "seen" all messages that are older
;; than current
(register-handler :msg-seen
  (after (update-message! :seen))
  (update-message-status :seen))
