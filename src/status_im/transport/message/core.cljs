(ns status-im.transport.message.core
  (:require [status-im.utils.handlers :as handlers]
            [status-im.chat.models :as models.chat]))

(defprotocol StatusMessage
  "Protocol for transport layed status messages"
  (send [this chat-id cofx])
  (receive [this chat-id signature cofx]))

;; TODO (yenda) implement
;; :group-message
;; :public-group-message
;; :pending
;; :group-invitation
;; :update-group
;; :add-group-identity
;; :remove-group-identity
;; :leave-group
;; :contact-request
;; :discover
;; :discoveries-request
;; :discoveries-response
;; :profile
;; :online


;;TODO (yenda) this is probably not the place to have these
(defn- receive-contact-request
  [public-key
   {:keys [name profile-image address fcm-token]}
   {{:contacts/keys [contacts] :as db} :db :as cofx}]
  (when-not (get contacts public-key)
    (let [contact-props {:whisper-identity public-key
                         :public-key       public-key
                         :address          address
                         :photo-path       profile-image
                         :name             name
                         :fcm-token        fcm-token
                         :pending?         true}
          chat-props    {:name         name
                         :chat-id      public-key
                         :contact-info (prn-str contact-props)}]
      (handlers/merge-fx cofx
                         {:db           (update-in db [:contacts/contacts public-key] merge contact-props)
                          :save-contact contact-props}
                         (models.chat/add-chat public-key chat-props)))))

(defn- receive-contact-request-confirmation
  [public-key {:keys [name profile-image address fcm-token]}
   {{:contacts/keys [contacts] :as db} :db :as cofx}]
  (when-let [contact (get contacts public-key)]
    (let [contact-props {:whisper-identity public-key
                         :address          address
                         :photo-path       profile-image
                         :name             name
                         :fcm-token        fcm-token}
          chat-props    {:name    name
                         :chat-id public-key}]
      (handlers/merge-fx cofx
                         {:db           (update-in db [:contacts/contacts public-key] merge contact-props)
                          :save-contact contact-props}
                         (models.chat/upsert-chat chat-props)))))
