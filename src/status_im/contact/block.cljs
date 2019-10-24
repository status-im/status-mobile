(ns status-im.contact.block
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models :as chat.models]
            [status-im.chat.models.loading :as chat.models.loading]
            [status-im.chat.models.message-list :as message-list]

            [status-im.chat.models.message :as chat.models.message]
            [status-im.contact.db :as contact.db]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]))

(fx/defn remove-current-chat-id
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :current-chat-id)}
            (navigation/navigate-to-cofx :home {})))

(fx/defn clean-up-chat
  [{:keys [db] :as cofx}
   public-key
   {:keys [chat-id
           unviewed-messages-count
           last-message-content
           last-message-timestamp
           last-message-content-type]}]
  (let [removed-messages-ids (keep
                              (fn [[message-id {:keys [from]}]]
                                (when (= from public-key)
                                  message-id))
                              (get-in db [:chats chat-id :messages]))
        db (-> db
               ;; remove messages
               (update-in [:chats chat-id :messages]
                          #(apply dissoc % removed-messages-ids))
               ;; remove message groups
               (update-in [:chats chat-id]
                          dissoc :message-list)
               (update-in [:chats chat-id]
                          assoc
                          :unviewed-messages-count unviewed-messages-count
                          :last-message-content last-message-content
                          :last-message-timestamp last-message-timestamp
                          :last-message-content-type last-message-content-type))]
    {:db (update-in db [:chats chat-id :message-list] message-list/add-many (vals (get-in db [:chats chat-id :messages])))}))

(fx/defn contact-blocked
  {:events [::contact-blocked]}
  [{:keys [db] :as cofx} {:keys [public-key]} chats]
  (let [fxs (map #(clean-up-chat public-key %) chats)]
    (apply fx/merge cofx fxs)))

(fx/defn block-contact
  [{:keys [db now] :as cofx} public-key]
  (let [contact (-> (contact.db/public-key->contact
                     (:contacts/contacts db)
                     public-key)
                    (assoc :last-updated now)
                    (update :system-tags (fnil conj #{}) :contact/blocked))
        from-one-to-one-chat? (not (get-in db [:chats (:current-chat-id db) :group-chat]))]
    (fx/merge cofx
              {:db (-> db
                       ;; add the contact to blocked contacts
                       (update :contacts/blocked (fnil conj #{}) public-key)
                       ;; update the contact in contacts list
                       (assoc-in [:contacts/contacts public-key] contact)
                       ;; remove the 1-1 chat if it exists
                       (update-in [:chats] dissoc public-key))}
              (contacts-store/block contact #(re-frame/dispatch [::contact-blocked contact (map chats-store/<-rpc %)]))
              ;; reset navigation to avoid going back to non existing one to one chat
              (if from-one-to-one-chat?
                remove-current-chat-id
                (navigation/navigate-back)))))

(fx/defn unblock-contact
  [{:keys [db now] :as cofx} public-key]
  (let [contact (-> (get-in db [:contacts/contacts public-key])
                    (assoc :last-updated now)
                    (update :system-tags disj :contact/blocked))]
    (fx/merge cofx
              {:db (-> db
                       (update :contacts/blocked disj public-key)
                       (assoc-in [:contacts/contacts public-key] contact))}
              (contacts-store/save-contact contact))))
