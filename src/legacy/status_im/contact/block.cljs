(ns legacy.status-im.contact.block
  (:require
    [legacy.status-im.contact.db :as contact.db]
    [legacy.status-im.data-store.chats :as chats-store]
    [legacy.status-im.utils.deprecated-types :as types]
    [re-frame.core :as re-frame]
    [status-im.contexts.chat.contacts.events :as contacts-store]
    [status-im.contexts.chat.messages.list.events :as message-list]
    [status-im.contexts.shell.activity-center.events :as activity-center]
    [status-im.navigation.events :as navigation]
    [utils.re-frame :as rf]))

(rf/defn clean-up-chat
  [{:keys [db]}
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
  {:events [:contacts/blocked]}
  [{:keys [db] :as cofx} {:keys [public-key]} chats-js]
  (let [fxs (when chats-js
              (map #(->> (chats-store/<-rpc %)
                         (clean-up-chat public-key))
                   (types/js->clj chats-js)))]
    (apply
     rf/merge
     cofx
     {:db                                                     (->
                                                                db
                                                                (update :chats dissoc public-key)
                                                                (update :chats-home-list disj public-key)
                                                                (assoc-in [:contacts/contacts public-key
                                                                           :added?]
                                                                          false))
      :dispatch                                               [:shell/close-switcher-card public-key]
      :effects/push-notifications-clear-message-notifications [public-key]}
     (activity-center/notifications-fetch-unread-count)
     fxs)))

(rf/defn block-contact
  {:events [:contact.ui/block-contact-confirmed]}
  [{:keys [db] :as cofx} public-key]
  (let [contact               (-> (contact.db/public-key->contact
                                   (:contacts/contacts db)
                                   public-key)
                                  (assoc :blocked? true
                                         :added?   false
                                         :active?  false))
        from-one-to-one-chat? (not (get-in db [:chats (:current-chat-id db) :group-chat]))]
    (rf/merge cofx
              {:db (assoc-in db [:contacts/contacts public-key] contact)}
              (contacts-store/block
               public-key
               (fn [^js block-contact]
                 (re-frame/dispatch [:contacts/blocked contact (.-chats block-contact)])
                 (re-frame/dispatch [:sanitize-messages-and-process-response block-contact])
                 (re-frame/dispatch [:hide-popover])))
              ;; reset navigation to avoid going back to non existing one to one chat
              (if from-one-to-one-chat?
                (navigation/pop-to-root :shell-stack)
                (navigation/navigate-back)))))

(rf/defn contact-unblocked
  {:events [:contacts/unblocked]}
  [{:keys [db]} contact-id]
  (let [contact (-> (get-in db [:contacts/contacts contact-id])
                    (assoc :blocked? false))]
    {:db (assoc-in db [:contacts/contacts contact-id] contact)}))

(rf/defn unblock-contact
  {:events [:contact.ui/unblock-contact-pressed]}
  [cofx contact-id]
  (contacts-store/unblock
   cofx
   contact-id
   #(re-frame/dispatch [:contacts/unblocked contact-id])))
