(ns status-im.contact.core
  (:require [re-frame.core :as re-frame]
            [status-im2.constants :as constants]
            [status-im.contact.block :as contact.block]
            [status-im.contact.db :as contact.db]
            [status-im.data-store.contacts :as contacts-store]
            [utils.re-frame :as rf]
            [status-im2.navigation.events :as navigation]
            [taoensso.timbre :as log]))

(defn build-contact
  [{{:keys          [multiaccount]
     :contacts/keys [contacts]}
    :db} public-key]
  (cond-> (contact.db/public-key->contact contacts public-key)
    (= public-key (:public-key multiaccount))
    (assoc :name (:name multiaccount))))

(rf/defn ensure-contacts
  [{:keys [db]} contacts chats]
  (let [events
        (reduce
         (fn [acc {:keys [public-key] :as contact}]
           (let [added       (:added contact)
                 was-added   (contact.db/added? db public-key)
                 blocked     (:blocked contact)
                 was-blocked (contact.db/blocked? db public-key)]
             (cond-> acc

               (and (not (:has-added-us contact))
                    (= constants/contact-request-state-none (:contact-request-state contact)))
               (conj [:activity-center/remove-pending-contact-request (:public-key contact)])

               (and was-added (not added))
               (conj nil)

               (and blocked (not was-blocked))
               (conj [::contact.block/contact-blocked contact chats]))))
         [[:chat/offload-messages constants/timeline-chat-id]
          [:activity-center.notifications/fetch-unread-count]]
         contacts)]
    (merge
     {:db (update db
                  :contacts/contacts
                  #(reduce (fn [acc {:keys [public-key] :as contact}]
                             (-> acc
                                 (update public-key merge contact)
                                 (assoc-in [public-key :nickname] (:nickname contact))))
                           %
                           contacts))}
     (when (> (count events) 1)
       {:dispatch-n events}))))

(defn- own-info
  [db]
  (let [{:keys [name preferred-name identicon address]} (:multiaccount db)]
    {:name          (or preferred-name name)
     :profile-image identicon
     :address       address}))

(rf/defn add-contact
  "Add a contact and set pending to false"
  {:events [:contact.ui/add-contact-pressed]}
  [{:keys [db] :as cofx} public-key nickname ens-name]
  (when (not= (get-in db [:multiaccount :public-key]) public-key)
    (contacts-store/add
     cofx
     public-key
     nickname
     ens-name
     #(do
        (re-frame/dispatch [:sanitize-messages-and-process-response %])
        (re-frame/dispatch [:chat/offload-messages constants/timeline-chat-id])))))

(rf/defn remove-contact
  "Remove a contact from current account's contact list"
  {:events [:contact.ui/remove-contact-pressed]}
  [{:keys [db]} {:keys [public-key]}]
  {:db            (-> db
                      (assoc-in [:contacts/contacts public-key :added] false)
                      (assoc-in [:contacts/contacts public-key :contact-request-state]
                                constants/contact-request-state-none))
   :json-rpc/call [{:method     "wakuext_retractContactRequest"
                    :params     [{:contactId public-key}]
                    :on-success #(log/debug "contact removed successfully")}]
   :dispatch      [:chat/offload-messages constants/timeline-chat-id]})

(rf/defn initialize-contacts
  [cofx]
  (contacts-store/fetch-contacts-rpc cofx #(re-frame/dispatch [::contacts-loaded %])))

(rf/defn open-contact-toggle-list
  {:events [:contact.ui/start-group-chat-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc db
                        :group/selected-contacts #{}
                        :new-chat-name           "")}
            (navigation/navigate-to-cofx :contact-toggle-list nil)))

(rf/defn update-nickname
  {:events [:contacts/update-nickname]}
  [{:keys [db] :as cofx} public-key nickname]
  (rf/merge cofx
            (contacts-store/set-nickname
             public-key
             nickname
             #(re-frame/dispatch [:sanitize-messages-and-process-response %]))
            (navigation/navigate-back)))

(rf/defn set-search-query
  {:events [:contacts/set-search-query]}
  [{:keys [db] :as cofx} value]
  {:db (assoc db :contacts/search-query value)})
