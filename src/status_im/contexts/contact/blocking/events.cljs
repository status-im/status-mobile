(ns status-im.contexts.contact.blocking.events
  (:require
    [legacy.status-im.data-store.chats :as chats-store]
    [re-frame.core :as re-frame]
    [status-im.contexts.chat.messenger.messages.list.events :as message-list]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [utils.transforms :as transforms]))

(rf/defn clean-up-chat
  [{:keys [db]} public-key
   {:keys [chat-id unviewed-messages-count unviewed-mentions-count last-message]}]
  (let [ids (keep (fn [[message-id {:keys [from]}]]
                    (when (= from public-key) message-id))
                  (get-in db [:messages chat-id]))
        db  (-> db
                ;; remove messages
                (update-in [:messages chat-id] #(apply dissoc % ids))
                (update-in [:chats chat-id]
                           assoc
                           :unviewed-messages-count unviewed-messages-count
                           :unviewed-mentions-count unviewed-mentions-count
                           :last-message            last-message))]
    {:db (assoc-in db
          [:message-lists chat-id]
          (message-list/add-many nil (vals (get-in db [:messages chat-id]))))}))

(rf/defn contact-blocked
  {:events [:contact/blocked]}
  [{:keys [db] :as cofx} {:keys [public-key]} chats-js]
  (let [fxs (when chats-js
              (map #(->> (chats-store/<-rpc %)
                         (clean-up-chat public-key))
                   (transforms/js->clj chats-js)))]
    (apply
     rf/merge
     cofx
     {:db (-> db
              (update :chats dissoc public-key)
              (update :chats-home-list disj public-key)
              (assoc-in [:contacts/contacts public-key :added?] false))
      :fx [[:activity-center.notifications/fetch-unread-count]
           [:effects/push-notifications-clear-message-notifications [public-key]]
           [:dispatch-later
            [{:ms       500
              :dispatch [:chat.ui/close-and-remove-chat public-key]}]]]}
     fxs)))

(rf/reg-event-fx :contact/block-contact
 (fn [{:keys [db]} [public-key]]
   (let [contact               (-> (get (:contacts/contacts db) public-key {:public-key public-key})
                                   (assoc :blocked? true
                                          :added?   false
                                          :active?  false))
         current-chat-id       (:current-chat-id db)
         from-one-to-one-chat? (not (get-in db [:chats current-chat-id :group-chat]))]
     {:db (assoc-in db [:contacts/contacts public-key] contact)
      :fx [[:json-rpc/call
            [{:method      "wakuext_blockContact"
              :params      [public-key]
              :js-response true
              :on-success  (fn [^js response]
                             (re-frame/dispatch [:contact/blocked contact (.-chats response)])
                             (re-frame/dispatch [:sanitize-messages-and-process-response response]))
              :on-error    #(log/error "failed to block contact" % public-key)}]]
           [:dispatch [:hide-popover]]
           ;; reset navigation to avoid going back to non existing one to one chat
           (when current-chat-id
             (if from-one-to-one-chat?
               [:dispatch [:pop-to-root :shell-stack]]
               [:dispatch [:navigate-back]]))]})))

(rf/reg-event-fx :contact/unblocked
 (fn [{:keys [db]} [contact-id]]
   {:db (update-in db
                   [:contacts/contacts contact-id]
                   #(assoc % :blocked? false))}))

(rf/reg-event-fx :contact/unblock-contact
 (fn [_ [contact-id]]
   {:json-rpc/call
    [{:method     "wakuext_unblockContact"
      :params     [contact-id]
      :on-success #(re-frame/dispatch [:contact/unblocked contact-id])
      :on-error   #(log/error "failed to unblock contact" % contact-id)}]}))
