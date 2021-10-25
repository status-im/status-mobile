(ns status-im.contact.core
  (:require [re-frame.core :as re-frame]
            [status-im.contact.db :as contact.db]
            [status-im.data-store.contacts :as contacts-store]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.constants :as constants]
            [status-im.contact.block :as contact.block]))

(fx/defn load-contacts
  {:events [::contacts-loaded]}
  [{:keys [db] :as cofx} all-contacts]
  (let [contacts-list (map #(vector (:public-key %) (if (empty? (:address %))
                                                      (dissoc % :address)
                                                      %))
                           all-contacts)
        contacts (into {} contacts-list)]
    {:db (cond-> (-> db
                     (update :contacts/contacts #(merge contacts %))
                     (assoc :contacts/blocked (contact.db/get-blocked-contacts all-contacts))))}))

(defn build-contact
  [{{:keys [multiaccount]
     :contacts/keys [contacts]} :db} public-key]
  (cond-> (contact.db/public-key->contact contacts public-key)
    (= public-key (:public-key multiaccount))
    (assoc :name (:name multiaccount))))

(defn- own-info
  [db]
  (let [{:keys [name preferred-name identicon address]} (:multiaccount db)]
    {:name          (or preferred-name name)
     :profile-image identicon
     :address       address}))

(fx/defn ensure-contacts
  [{:keys [db]} contacts chats]
  (let [events
        (reduce
         (fn [acc {:keys [public-key] :as contact}]
           (let [added (:added contact)
                 was-added (contact.db/added? db public-key)
                 blocked (:blocked contact)
                 was-blocked (contact.db/blocked? db public-key)]
             (cond-> acc
               (and added (not was-added))
               (conj [:start-profile-chat public-key])

               (and was-added (not added))
               (conj nil)

               (and blocked (not was-blocked))
               (conj [::contact.block/contact-blocked contact chats]))))
         [[:offload-messages constants/timeline-chat-id]]
         contacts)]
    (merge
     {:db (update db :contacts/contacts
                  #(reduce (fn [acc {:keys [public-key] :as contact}]
                             (-> acc
                                 (update public-key merge contact)
                                 (assoc-in [public-key :nickname] (:nickname contact))))
                           %
                           contacts))}
     (when (> (count events) 1)
       {:dispatch-n events}))))

(fx/defn send-contact-request
  {:events [::send-contact-request]}
  [{:keys [db] :as cofx} public-key]
  (let [{:keys [name profile-image]} (own-info db)]
    {::json-rpc/call [{:method (json-rpc/call-ext-method "sendContactUpdate")
                       :params [public-key name profile-image]
                       :on-success #(log/debug "contact request sent" public-key)}]}))

(fx/defn add-contact
  "Add a contact and set pending to false"
  {:events [:contact.ui/add-to-contact-pressed]}
  [{:keys [db] :as cofx} public-key nickname ens-name]
  (when (not= (get-in db [:multiaccount :public-key]) public-key)
    (contacts-store/add
     cofx
     public-key
     nickname
     ens-name
     #(do
        (re-frame/dispatch [:sanitize-messages-and-process-response %])
        (re-frame/dispatch [:offload-messages constants/timeline-chat-id])))))

(fx/defn remove-contact
  "Remove a contact from current account's contact list"
  {:events [:contact.ui/remove-contact-pressed]}
  [{:keys [db]} {:keys [public-key]}]
  {:db (assoc-in db [:contacts/contacts public-key :added] false)
   ::json-rpc/call [{:method "wakuext_removeContact"
                     :params [public-key]
                     :on-success #(log/debug "contact removed successfully")}]
   :dispatch [:offload-messages constants/timeline-chat-id]})

(fx/defn initialize-contacts [cofx]
  (contacts-store/fetch-contacts-rpc cofx #(re-frame/dispatch [::contacts-loaded %])))

(fx/defn open-contact-toggle-list
  {:events [:contact.ui/start-group-chat-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc db
                        :group/selected-contacts #{}
                        :new-chat-name "")}
            (navigation/navigate-to-cofx :contact-toggle-list nil)))

(fx/defn update-nickname
  {:events [:contacts/update-nickname]}
  [{:keys [db] :as cofx} public-key nickname]
  (fx/merge cofx
            (contacts-store/set-nickname
             public-key
             nickname
             #(re-frame/dispatch [:sanitize-messages-and-process-response %]))
            (navigation/navigate-back)))
