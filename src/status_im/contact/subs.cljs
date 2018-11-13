(ns status-im.contact.subs
  (:require [re-frame.core :as re-frame]
            [status-im.contact.db :as contact.db]))

(re-frame/reg-sub ::public-key :contacts/identity)

(re-frame/reg-sub
 ::contacts
 (fn [db]
   (get db :contacts/contacts)))

(re-frame/reg-sub
 ::dapps
 (fn [db]
   (:contacts/dapps db)))

(re-frame/reg-sub
 :contacts/contacts
 :<- [::contacts]
 (fn [contacts]
   (contact.db/enrich-contacts contacts)))

(re-frame/reg-sub
 :contacts/blocked
 :<- [:contacts/contacts]
 (fn [contacts]
   (contact.db/blocked-contacts contacts)))

(defn sort-contacts [contacts]
  (sort (fn [c1 c2]
          (let [name1 (or (:name c1) (:address c1) (:public-key c1))
                name2 (or (:name c2) (:address c2) (:public-key c2))]
            (compare (clojure.string/lower-case name1)
                     (clojure.string/lower-case name2))))
        contacts))

(re-frame/reg-sub
 :contacts/added
 :<- [:contacts/contacts]
 (fn [contacts]
   (->> (vals contacts)
        (remove (fn [{:keys [dapp? pending? blocked? hide-contact? public-key]}]
                  (or blocked? dapp? pending? hide-contact?)))
        sort-contacts)))

(defn- filter-dapps [v dev-mode?]
  (remove #(when-not dev-mode? (true? (:developer? %))) v))

(re-frame/reg-sub
 :contacts/dapps
 :<- [::dapps]
 :<- [:account/account]
 (fn [[dapps {:keys [dev-mode?]}]]
   (map (fn [m] (update m :data #(filter-dapps % dev-mode?))) dapps)))

(re-frame/reg-sub
 :contacts/current
 :<- [:contacts/contacts]
 :<- [::public-key]
 (fn [[contacts public-key]]
   (contact.db/public-key->contact contacts public-key)))

(re-frame/reg-sub
 :contacts/dapps-by-name
 :<- [:contacts/dapps]
 (fn [dapps]
   (reduce (fn [dapps-by-name category]
             (merge dapps-by-name
                    (reduce (fn [acc {:keys [name] :as dapp}]
                              (assoc acc name dapp))
                            {}
                            (:data category))))
           {}
           dapps)))

(re-frame/reg-sub
 :contacts/not-in-current-chat
 :<- [:chats/current]
 :<- [:contacts/contacts]
 (fn [[{:keys [contacts]} all-contacts]]
   (let [participants-set (into #{} (filter identity contacts))]
     (->> (vals all-contacts)
          (remove (fn [{:keys [public-key blocked? dapp?]}]
                    (or (participants-set public-key)
                        blocked?
                        dapp?)))
          sort-contacts))))

(defn get-all-contacts-in-group-chat
  [chat-contacts current-account]
  (let [current-public-key (:public-key current-account)
        current-account-contact (-> current-account
                                    (select-keys [:name :photo-path :public-key])
                                    (assoc :current-account? true))
        chat-contacts (assoc chat-contacts
                             current-public-key
                             current-account-contact)]
    (->> (vals chat-contacts)
         (remove :dapp?)
         sort-contacts)))

(re-frame/reg-sub
 :contacts/current-chat-contacts
 :<- [:chats/current]
 :<- [:account/account]
 (fn [[{:keys [contacts]} current-account]]
   (get-all-contacts-in-group-chat contacts current-account)))

(re-frame/reg-sub
 :contacts/contact-by-address
 :<- [:contacts/contacts]
 (fn [contacts [_ address]]
   (contact.db/address->contact contacts address)))

(re-frame/reg-sub
 :contacts/contacts-by-address
 :<- [:contacts/contacts]
 (fn [contacts]
   (reduce (fn [acc [_ {:keys [address] :as contact}]]
             (if address
               (assoc acc address contact)
               acc))
           {}
           contacts)))
