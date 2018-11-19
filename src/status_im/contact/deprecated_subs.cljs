(ns status-im.contact.deprecated-subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.identicon :as identicon]
            [status-im.contact.db :as contact.db]
            [status-im.contact.subs :as contact.subs]))

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
 :contacts/all-added-people-contacts
 :<- [:contacts/added]
 (fn [contacts]
   contacts))

(re-frame/reg-sub
 :contacts/all-dapps
 :<- [:contacts/dapps]
 (fn [dapps]
   dapps))

(re-frame/reg-sub
 :contacts/contact-by-identity
 :<- [:contacts/contacts]
 :<- [:chats/current-chat]
 (fn [[all-contacts {:keys [contacts]}] [_ identity]]
   (let [identity' (or identity (first contacts))]
     (or
      (get all-contacts identity')
      (contact.db/public-key->new-contact identity')))))

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
 :<- [:contacts/not-in-current-chat]
 (fn [contacts]
   contacts))

(re-frame/reg-sub
 :contacts/contacts-by-chat
 (fn [[_ _ chat-id] _]
   [(re-frame/subscribe [:chats/chat chat-id])
    (re-frame/subscribe [:contacts/contacts])])
 (fn [[chat all-contacts] [_ query-fn]]
   (contact.db/query-chat-contacts chat all-contacts query-fn)))

(re-frame/reg-sub
 :contacts/chat-photo
 (fn [[_ chat-id]]
   (re-frame/subscribe [:chats/chat chat-id]))
 (fn [{:keys [photo-path]}]
   photo-path))
