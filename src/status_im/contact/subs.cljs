(ns status-im.contact.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.identicon :as identicon]
            [status-im.contact.db :as contact.db]))

(re-frame/reg-sub
 ::dapps
 (fn [db]
   (:contacts/dapps db)))

(re-frame/reg-sub
 ::query-current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 (fn [[chat contacts] [_ query-fn]]
   (contact.db/query-chat-contacts chat contacts query-fn)))

(re-frame/reg-sub
 :contacts/contacts
 (fn [db]
   (get db :contacts/contacts)))

(re-frame/reg-sub
 :contacts/active
 :<- [:contacts/contacts]
 (fn [contacts]
   (contact.db/active contacts)))

(re-frame/reg-sub
 :contacts/active-count
 :<- [:contacts/active]
 (fn [active-contacts]
   (count active-contacts)))

(re-frame/reg-sub
 :contacts/blocked
 :<- [:contacts/contacts]
 (fn [contacts]
   (->> contacts
        (filter (fn [[_ {:keys [blocked?]}]]
                  blocked?))
        (contact.db/sort-contacts))))

(re-frame/reg-sub
 :contacts/blocked-count
 :<- [:contacts/blocked]
 (fn [blocked-contacts]
   (count blocked-contacts)))

(re-frame/reg-sub
 :contacts/current-contact-identity
 (fn [db]
   (get db :contacts/identity)))

(re-frame/reg-sub
 :contacts/current-contact
 :<- [:contacts/contacts]
 :<- [:contacts/current-contact-identity]
 (fn [[contacts identity]]
   (contacts identity)))

(re-frame/reg-sub
 :contacts/all-dapps
 :<- [::dapps]
 :<- [:account/account]
 (fn [[dapps {:keys [dev-mode?]}]]
   (map (fn [m] (update m :data
                        #(contact.db/filter-dapps % dev-mode?)))
        dapps)))

(re-frame/reg-sub
 :contacts/dapps-by-name
 :<- [:contacts/all-dapps]
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
 :contacts/contact-name-by-identity
 :<- [:contacts/contacts]
 :<- [:account/account]
 (fn [[contacts current-account] [_ identity]]
   (let [me? (= (:public-key current-account) identity)]
     (if me?
       (:name current-account)
       (:name (contacts identity))))))

(re-frame/reg-sub
 :contacts/all-contacts-not-in-current-chat
 :<- [::query-current-chat-contacts remove]
 (fn [contacts]
   (->> contacts
        (remove :dapp?)
        (sort-by (comp clojure.string/lower-case :name)))))

(re-frame/reg-sub
 :contacts/current-chat-contacts
 :<- [:chats/current-chat]
 :<- [:contacts/contacts]
 :<- [:account/account]
 (fn [[{:keys [contacts admins]} all-contacts current-account]]
   (contact.db/get-all-contacts-in-group-chat contacts admins all-contacts current-account)))

(re-frame/reg-sub
 :contacts/contacts-by-chat
 (fn [[_ _ chat-id] _]
   [(re-frame/subscribe [:chats/chat chat-id])
    (re-frame/subscribe [:contacts/contacts])])
 (fn [[chat all-contacts] [_ query-fn]]
   (contact.db/query-chat-contacts chat all-contacts query-fn)))

(re-frame/reg-sub
 :contacts/chat-photo
 (fn [[_ chat-id] _]
   [(re-frame/subscribe [:chats/chat chat-id])
    (re-frame/subscribe [:contacts/contacts-by-chat filter chat-id])])
 (fn [[chat contacts] [_ chat-id]]
   (when (and chat (not (:group-chat chat)))
     (cond
       (:photo-path chat)
       (:photo-path chat)

       (pos? (count contacts))
       (:photo-path (first contacts))

       :else
       (identicon/identicon chat-id)))))

(re-frame/reg-sub
 :contacts/contact-by-address
 :<- [:contacts/contacts]
 (fn [contacts [_ address]]
   (contact.db/find-contact-by-address contacts address)))

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
