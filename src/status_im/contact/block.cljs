(ns status-im.contact.block
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models.message-list :as message-list]
            [status-im.contact.db :as contact.db]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]))

(fx/defn clean-up-chat
  [{:keys [db] :as cofx}
   public-key
   {:keys [chat-id
           unviewed-messages-count
           last-message]}]
  (let [removed-messages-ids (keep
                              (fn [[message-id {:keys [from]}]]
                                (when (= from public-key)
                                  message-id))
                              (get-in db [:messages chat-id]))
        db (-> db
               ;; remove messages
               (update-in [:messages chat-id]
                          #(apply dissoc % removed-messages-ids))
               (update-in [:chats chat-id]
                          assoc
                          :unviewed-messages-count unviewed-messages-count
                          :last-message last-message))]
    {:db (assoc-in db [:message-lists chat-id]
                   (message-list/add-many nil (vals (get-in db [:messages chat-id]))))}))

(fx/defn contact-blocked
  {:events [::contact-blocked]}
  [{:keys [db] :as cofx} {:keys [public-key]} chats]
  (let [fxs (map #(clean-up-chat public-key %) chats)]
    (apply fx/merge cofx fxs)))

(fx/defn block-contact
  {:events [:contact.ui/block-contact-confirmed]}
  [{:keys [db now] :as cofx} public-key]
  (let [contact (-> (contact.db/public-key->contact
                     (:contacts/contacts db)
                     public-key)
                    (assoc :last-updated now)
                    (update :system-tags (fnil conj #{}) :contact/blocked))
        from-one-to-one-chat? (not (get-in db [:chats (:inactive-chat-id db) :group-chat]))]
    (fx/merge cofx
              {:db (-> db
                       ;; add the contact to blocked contacts
                       (update :contacts/blocked (fnil conj #{}) public-key)
                       ;; update the contact in contacts list
                       (assoc-in [:contacts/contacts public-key] contact)
                       ;; remove the 1-1 chat if it exists
                       (update-in [:chats] dissoc public-key))}
              (contacts-store/block contact #(do (re-frame/dispatch [::contact-blocked contact (map chats-store/<-rpc %)])
                                                 (re-frame/dispatch [:hide-popover])))
              ;; reset navigation to avoid going back to non existing one to one chat
              (if from-one-to-one-chat?
                (navigation/navigate-to-cofx :home {})
                (navigation/navigate-back)))))

(fx/defn unblock-contact
  {:events [:contact.ui/unblock-contact-pressed]}
  [{:keys [db now] :as cofx} public-key]
  (let [contact (-> (get-in db [:contacts/contacts public-key])
                    (assoc :last-updated now)
                    (update :system-tags disj :contact/blocked))]
    (fx/merge cofx
              {:db (-> db
                       (update :contacts/blocked disj public-key)
                       (assoc-in [:contacts/contacts public-key] contact))}
              (contacts-store/save-contact contact))))
