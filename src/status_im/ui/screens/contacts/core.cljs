(ns status-im.ui.screens.contacts.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.chat.models :as chat.models]
            [status-im.constants :as constants]))

(defn- update-contact [{:keys [whisper-identity] :as contact} {:keys [db]}]
  (when (get-in db [:contacts/contacts whisper-identity])
    {:db                      (update-in db [:contacts/contacts whisper-identity] merge contact)
     :data-store/save-contact contact}))

(defn receive-contact-update [chat-id public-key {:keys [name profile-image]} {:keys [db now] :as cofx}]
  (let [{:keys [chats current-public-key]} db]
    (when (not= current-public-key public-key)
      (let [prev-last-updated (get-in db [:contacts/contacts public-key :last-updated])]
        (when (<= prev-last-updated now)
          (let [contact {:whisper-identity public-key
                         :name             name
                         :photo-path       profile-image
                         :last-updated     now}]
            (if (chats public-key)
              (handlers-macro/merge-fx cofx
                                 (update-contact contact)
                                 (chat.models/update-chat {:chat-id chat-id
                                                           :name    name}))
              (update-contact contact cofx))))))))

(defn receive-contact-request
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
      (handlers-macro/merge-fx cofx
                               {:db                      (update-in db [:contacts/contacts public-key] merge contact-props)
                                :data-store/save-contact contact-props}
                               (chat.models/add-chat public-key chat-props)))))

(defn receive-contact-request-confirmation
  [public-key {:keys [name profile-image address fcm-token]}
   {{:contacts/keys [contacts] :as db} :db :as cofx}]
  (when-let [contact (get contacts public-key)]
    (let [contact-props {:whisper-identity public-key
                         :public-key       public-key
                         :address          address
                         :photo-path       profile-image
                         :name             name
                         :fcm-token        fcm-token}
          chat-props    {:name    name
                         :chat-id public-key}]
      (handlers-macro/merge-fx cofx
                               {:db                      (update-in db [:contacts/contacts public-key] merge contact-props)
                                :data-store/save-contact contact-props}
                               (chat.models/upsert-chat chat-props)))))
