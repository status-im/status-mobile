(ns status-im.contact.block
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models.message-list :as message-list]
            [status-im.contact.db :as contact.db]
            [status-im.data-store.chats :as chats-store]
            [status-im.data-store.contacts :as contacts-store]
            [utils.re-frame :as rf]
            [status-im.utils.types :as types]
            [status-im2.contexts.activity-center.events :as activity-center]
            [status-im2.navigation.events :as navigation]))

(rf/defn clean-up-chat
  [{:keys [db] :as cofx}
   public-key
   {:keys [chat-id
           unviewed-messages-count
           unviewed-mentions-count
           last-message]}]
  (let [removed-messages-ids (keep
                              (fn [[message-id {:keys [from]}]]
                                (when (= from public-key)
                                  message-id))
                              (get-in db [:messages chat-id]))
        db                   (-> db
                                 ;; remove messages
                                 (update-in [:messages chat-id]
                                            #(apply dissoc % removed-messages-ids))
                                 (update-in [:chats chat-id]
                                            assoc
                                            :unviewed-messages-count unviewed-messages-count
                                            :unviewed-mentions-count unviewed-mentions-count
                                            :last-message            last-message))]
    {:db (assoc-in db
          [:message-lists chat-id]
          (message-list/add-many nil (vals (get-in db [:messages chat-id]))))}))

(rf/defn contact-blocked
  {:events [::contact-blocked]}
  [{:keys [db] :as cofx} {:keys [public-key]} chats]
  (let [fxs (when chats
              (map #(->> (chats-store/<-rpc %)
                         (clean-up-chat public-key))
                   (types/js->clj chats)))]
    (apply rf/merge
           cofx
           {:db                          (-> db
                                             (update :chats dissoc public-key)
                                             (update :chats-home-list disj public-key)
                                             (assoc-in [:contacts/contacts public-key :added] false))
            :dispatch                    [:shell/close-switcher-card public-key]
            :clear-message-notifications
            [[public-key] (get-in db [:multiaccount :remote-push-notifications-enabled?])]}
           (activity-center/notifications-fetch-unread-count)
           fxs)))

(rf/defn block-contact
  {:events [:contact.ui/block-contact-confirmed]}
  [{:keys [db] :as cofx} public-key]
  (let [contact               (-> (contact.db/public-key->contact
                                   (:contacts/contacts db)
                                   public-key)
                                  (assoc :blocked true
                                         :added   false))
        from-one-to-one-chat? (not (get-in db [:chats (:current-chat-id db) :group-chat]))]
    (rf/merge cofx
              {:db (-> db
                       ;; add the contact to blocked contacts
                       (update :contacts/blocked (fnil conj #{}) public-key)
                       ;; update the contact in contacts list
                       (assoc-in [:contacts/contacts public-key] contact))}
              (contacts-store/block
               public-key
               #(do (re-frame/dispatch [::contact-blocked contact (.-chats %)])
                    (re-frame/dispatch [:sanitize-messages-and-process-response %])
                    (re-frame/dispatch [:hide-popover])))
              ;; reset navigation to avoid going back to non existing one to one chat
              (if from-one-to-one-chat?
                (navigation/pop-to-root-tab :chat-stack)
                (navigation/navigate-back)))))

(rf/defn contact-unblocked
  {:events [::contact-unblocked]}
  [{:keys [db]} contact-id]
  (let [contact (-> (get-in db [:contacts/contacts contact-id])
                    (assoc :blocked false))]
    {:db (-> db
             (update :contacts/blocked disj contact-id)
             (assoc-in [:contacts/contacts contact-id] contact))}))

(rf/defn unblock-contact
  {:events [:contact.ui/unblock-contact-pressed]}
  [cofx contact-id]
  (contacts-store/unblock
   cofx
   contact-id
   #(re-frame/dispatch [::contact-unblocked contact-id])))
