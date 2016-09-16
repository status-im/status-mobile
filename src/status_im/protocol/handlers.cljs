; todo everything inside this namespace must be revievew in common with future
; changes in protocol lib
(ns status-im.protocol.handlers
  (:require [status-im.utils.handlers :as u]
            [status-im.utils.logging :as log]
            [status-im.protocol.api :as api]
            [re-frame.core :refer [dispatch after]]
            [status-im.utils.handlers :refer [register-handler]]
            [status-im.models.contacts :as contacts]
            [status-im.models.messages :as messages]
            [status-im.models.pending-messages :as pending-messages]
            [status-im.models.chats :as chats]
            [status-im.protocol.api :refer [init-protocol]]
            [status-im.protocol.protocol-handler :refer [make-handler]]
            [status-im.models.protocol :refer [update-identity
                                               set-initialized]]
            [status-im.constants :refer [text-content-type]]
            [status-im.i18n :refer [label]]
            [status-im.utils.random :as random]))

(register-handler :initialize-protocol
  (u/side-effect!
    (fn [db [_ current-account-id]]
      (let [current-account (get-in db [:accounts current-account-id])]
        (init-protocol current-account (make-handler db))))))

(register-handler :protocol-initialized
  (fn [db [_ identity]]
    (-> db
        (update-identity identity)
        (set-initialized true))))

(defn system-message [message-id content]
  {:from         "system"
   :message-id   message-id
   :content      content
   :content-type text-content-type})

(defn joined-chat-message [chat-id from message-id]
  (let [contact-name (:name (contacts/contact-by-identity from))]
    (messages/save-message chat-id {:from         "system"
                                    :message-id   (str message-id "_" from)
                                    :content      (str (or contact-name from) " " (label :t/received-invitation))
                                    :content-type text-content-type})))

(defn participant-invited-to-group-message [chat-id identity from message-id]
  (let [inviter-name (:name (contacts/contact-by-identity from))
        invitee-name (if (= identity (api/my-identity))
                       (label :t/You)
                       (:name (contacts/contact-by-identity identity)))]
    (messages/save-message chat-id {:from         "system"
                                    :message-id   message-id
                                    :content      (str (or inviter-name from) " " (label :t/invited) " " (or invitee-name identity))
                                    :content-type text-content-type})))

(defn participant-removed-from-group-message [chat-id identity from message-id]
  (let [remover-name (:name (contacts/contact-by-identity from))
        removed-name (:name (contacts/contact-by-identity identity))]
    (->> (str (or remover-name from) " " (label :t/removed) " " (or removed-name identity))
         (system-message message-id)
         (messages/save-message chat-id))))

(defn you-removed-from-group-message [chat-id from message-id]
  (let [remover-name (:name (contacts/contact-by-identity from))]
    (->> (str (or remover-name from) " " (label :t/removed-from-chat))
         (system-message message-id)
         (messages/save-message chat-id))))

(defn participant-left-group-message [chat-id from message-id]
  (let [left-name (:name (contacts/contact-by-identity from))]
    (->> (str (or left-name from) " " (label :t/left))
         (system-message message-id)
         (messages/save-message chat-id))))

(register-handler :group-chat-invite-acked
  (u/side-effect!
    (fn [_ [action from group-id ack-message-id]]
      (log/debug action from group-id ack-message-id)
      #_(joined-chat-message group-id from ack-message-id))))

(register-handler :participant-removed-from-group
  (u/side-effect!
    (fn [_ [action from group-id identity message-id]]
      (log/debug action message-id from group-id identity)
      (chats/chat-remove-participants group-id [identity])
      (participant-removed-from-group-message group-id identity from message-id))))

(register-handler :you-removed-from-group
  (u/side-effect!
    (fn [_ [action from group-id message-id]]
      (log/debug action message-id from group-id)
      (you-removed-from-group-message group-id from message-id)
      (chats/set-chat-active group-id false))))

(register-handler :participant-left-group
  (u/side-effect!
    (fn [_ [action from group-id message-id]]
      (log/debug action message-id from group-id)
      (when-not (= (api/my-identity) from)
        (participant-left-group-message group-id from message-id)))))

(register-handler :participant-invited-to-group
  (u/side-effect!
    (fn [_ [action from group-id identity message-id]]
      (log/debug action message-id from group-id identity)
      (participant-invited-to-group-message group-id identity from message-id))))

(defn save-message-status! [status]
  (fn [_ [_ {:keys [message-id whisper-identity]}]]
    (when-let [message (messages/get-message message-id)]
      (let [message (if whisper-identity
                      (update-in message
                                 [:user-statuses whisper-identity]
                                 (fn [{old-status :status}]
                                   {:id               (random/id)
                                    :whisper-identity whisper-identity
                                    :status           (if (= (keyword old-status) :seen)
                                                        old-status
                                                        status)}))
                      (assoc message :message-status status))]
        (messages/update-message! message)))))

(defn update-message-status [status]
  (fn [db [_ {:keys [message-id whisper-identity]}]]
    (let [db-key         (if whisper-identity
                           [:message-user-statuses message-id whisper-identity]
                           [:message-statuses message-id])
          current-status (get-in db db-key)]
      (if-not (= :seen current-status)
        (assoc-in db db-key {:whisper-identity whisper-identity
                             :status           status})
        db))))

(register-handler :message-failed
  (after (save-message-status! :failed))
  (update-message-status :failed))

(register-handler :message-sent
  (after (save-message-status! :sent))
  (update-message-status :sent))

(register-handler :message-delivered
  (after (save-message-status! :delivered))
  (update-message-status :delivered))

(register-handler :message-seen
  [(after (save-message-status! :seen))
   (after (fn [_ [_ {:keys [chat-id]}]]
            (dispatch [:remove-unviewed-messages chat-id])))]
  (update-message-status :seen))

(register-handler :pending-message-upsert
  (u/side-effect!
    (fn [_ [_ pending-message]]
      (pending-messages/upsert-pending-message! pending-message))))

(register-handler :pending-message-remove
  (u/side-effect!
    (fn [_ [_ message-id]]
      (pending-messages/remove-pending-message! message-id))))

(register-handler :send-transaction!
  (u/side-effect!
    (fn [_ [_ amount message]]
      (println :send-transacion! amount message))))
