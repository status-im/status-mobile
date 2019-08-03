(ns status-im.contact.block
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models :as chat.models]
            [status-im.chat.models.loading :as chat.models.loading]
            [status-im.chat.models.message :as chat.models.message]
            [status-im.contact.db :as contact.db]
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
  [{:keys [db] :as cofx} chat-id removed-chat-messages]
  (let [removed-messages-ids (map :message-id removed-chat-messages)
        removed-unseen-count (count (remove :seen removed-chat-messages))
        unviewed-messages-count (- (get-in db [:chats chat-id :unviewed-messages-count])
                                   removed-unseen-count)
        db (-> db
               ;; remove messages
               (update-in [:chats chat-id :messages]
                          #(apply dissoc % removed-messages-ids))
               ;; remove message groups
               (update-in [:chats chat-id]
                          dissoc :message-groups))]
    (fx/merge cofx
              {:db db}
              ;; update unviewed messages count
              (chat.models/upsert-chat
               {:chat-id                      chat-id
                :unviewed-messages-count
                (if (pos? unviewed-messages-count)
                  unviewed-messages-count
                  0)})
              ;; recompute message group
              (chat.models.loading/group-chat-messages
               chat-id
               (vals (get-in db [:chats chat-id :messages]))))))

(fx/defn clean-up-chats
  [cofx removed-messages-by-chat]
  (apply fx/merge cofx
         (map (fn [[chat-id messages]]
                (clean-up-chat chat-id messages))
              removed-messages-by-chat)))

(fx/defn block-contact
  [{:keys [db get-user-messages now] :as cofx} public-key]
  (let [contact (-> (contact.db/public-key->contact
                     (:contacts/contacts db)
                     public-key)
                    (assoc :last-updated now)
                    (update :system-tags conj :contact/blocked))
        user-messages (get-user-messages public-key)
        user-messages-ids (map :message-id user-messages)
        ;; we make sure to remove the 1-1 chat which we delete entirely
        removed-messages-by-chat (-> (group-by :chat-id user-messages)
                                     (dissoc public-key))
        from-one-to-one-chat? (not (get-in db [:chats (:current-chat-id db) :group-chat]))]
    (fx/merge cofx
              {:db (-> db
                       ;; add the contact to blocked contacts
                       (update :contacts/blocked conj public-key)
                       ;; update the contact in contacts list
                       (assoc-in [:contacts/contacts public-key] contact)
                       ;; remove the 1-1 chat if it exists
                       (update-in [:chats] dissoc public-key))
               :data-store/tx [(contacts-store/block-user-tx contact
                                                             user-messages-ids)]}
              ;;remove the messages from chat
              (clean-up-chats removed-messages-by-chat)
              (chat.models.message/update-last-messages
               (keys removed-messages-by-chat))
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
              (contacts-store/save-contact-tx contact))))

(fx/defn block-contact-confirmation
  [cofx public-key]
  {:utils/show-confirmation
   {:title (i18n/label :t/block-contact)
    :content (i18n/label :t/block-contact-details)
    :confirm-button-text (i18n/label :t/to-block)
    :on-accept #(re-frame/dispatch [:contact.ui/block-contact-confirmed public-key])}})
