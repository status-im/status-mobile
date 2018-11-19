(ns status-im.contact.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.identicon :as identicon]
            [status-im.contact.db :as contact.db]))

(re-frame/reg-sub
 ::dapps
 (fn [db]
   (:contacts/dapps db)))

(re-frame/reg-sub
 :contacts/contacts
 (fn [db]
   (get db :contacts/contacts)))

(re-frame/reg-sub
 :contacts/added
 :<- [:contacts/contacts]
 (fn [contacts]
   (->> (vals contacts)
        (remove (fn [{:keys [dapp? pending? hide-contact? public-key]}]
                  (or dapp? pending? hide-contact?)))
        contact.db/sort-contacts)))

(re-frame/reg-sub
 :contacts/dapps
 :<- [::dapps]
 :<- [:account/account]
 (fn [[dapps {:keys [dev-mode?]}]]
   (map (fn [m] (update m :data
                        #(contact.db/filter-dapps % dev-mode?)))
        dapps)))

(re-frame/reg-sub
 :contacts/not-in-current-chat
 :<- [:chats/current]
 :<- [:contacts/contacts]
 (fn [[{:keys [contacts]} all-contacts]]
   (let [participants-set (into #{} (filter identity contacts))]
     (->> (vals all-contacts)
          (remove (fn [{:keys [public-key dapp?]}]
                    (or (participants-set public-key)
                        dapp?)))
          contact.db/sort-contacts))))

(re-frame/reg-sub
 :contacts/current-chat-contacts
 :<- [:chats/current]
 :<- [:account/account]
 (fn [[{:keys [contacts]} current-account]]
   (contact.db/get-all-contacts-in-group-chat contacts current-account)))

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
