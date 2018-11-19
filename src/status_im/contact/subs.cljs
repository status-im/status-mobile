(ns status-im.contact.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.identicon :as identicon]
            [status-im.contact.db :as contact.db]))

(re-frame/reg-sub :get-current-contact-identity :contacts/identity)

(re-frame/reg-sub :get-contacts :contacts/contacts)

(re-frame/reg-sub :get-dapps
                  (fn [db]
                    (:contacts/dapps db)))

(re-frame/reg-sub
 :get-current-contact
 :<- [:get-contacts]
 :<- [:get-current-contact-identity]
 (fn [[contacts identity]]
   (contacts identity)))

(re-frame/reg-sub
 :get-current-chat-contact
 :<- [:get-contacts]
 :<- [:get-current-chat-id]
 (fn [[contacts chat-id]]
   (get contacts chat-id)))

(re-frame/reg-sub
 :all-added-contacts
 :<- [:get-contacts]
 (fn [contacts]
   (->> contacts
        (remove (fn [[_ {:keys [pending? hide-contact?]}]]
                  (or pending? hide-contact?)))
        (contact.db/sort-contacts))))

(re-frame/reg-sub
 :all-added-people-contacts
 :<- [:all-added-contacts]
 (fn [contacts]
   (remove :dapp? contacts)))

(re-frame/reg-sub
 :all-dapps
 :<- [:get-dapps]
 :<- [:account/account]
 (fn [[dapps {:keys [dev-mode?]}]]
   (map (fn [m] (update m :data
                        #(contact.db/filter-dapps % dev-mode?)))
        dapps)))

(re-frame/reg-sub
 :get-people-in-current-chat
 :<- [:get-current-chat-contacts]
 (fn [contacts]
   (remove #(true? (:dapp? %)) contacts)))

(re-frame/reg-sub
 :get-contact-by-identity
 :<- [:get-contacts]
 :<- [:get-current-chat]
 (fn [[all-contacts {:keys [contacts]}] [_ identity]]
   (let [identity' (or identity (first contacts))]
     (or
      (get all-contacts identity')
      (contact.db/public-key->new-contact identity')))))

(re-frame/reg-sub
 :contacts/dapps-by-name
 :<- [:all-dapps]
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
 :get-contact-name-by-identity
 :<- [:get-contacts]
 :<- [:account/account]
 (fn [[contacts current-account] [_ identity]]
   (let [me? (= (:public-key current-account) identity)]
     (if me?
       (:name current-account)
       (:name (contacts identity))))))

(re-frame/reg-sub
 :query-current-chat-contacts
 :<- [:get-current-chat]
 :<- [:get-contacts]
 (fn [[chat contacts [_ query-fn]]]
   (contact.db/query-chat-contacts chat contacts query-fn)))

(re-frame/reg-sub
 :get-all-contacts-not-in-current-chat
 :<- [:query-current-chat-contacts remove]
 (fn [contacts]
   (->> contacts
        (remove :dapp?)
        (sort-by (comp clojure.string/lower-case :name)))))

(re-frame/reg-sub
 :get-current-chat-contacts
 :<- [:get-current-chat]
 :<- [:get-contacts]
 :<- [:account/account]
 (fn [[{:keys [contacts]} all-contacts current-account]]
   (contact.db/get-all-contacts-in-group-chat contacts all-contacts current-account)))

(re-frame/reg-sub
 :get-contacts-by-chat
 (fn [[_ _ chat-id] _]
   [(re-frame/subscribe [:get-chat chat-id])
    (re-frame/subscribe [:get-contacts])])
 (fn [[chat all-contacts [_ query-fn]]]
   (contact.db/query-chat-contacts chat all-contacts query-fn)))

(re-frame/reg-sub
 :get-chat-photo
 (fn [[_ chat-id] _]
   [(re-frame/subscribe [:get-chat chat-id])
    (re-frame/subscribe [:get-contacts-by-chat filter chat-id])])
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
 :get-contact-by-address
 :<- [:get-contacts]
 (fn [contacts [_ address]]
   (contact.db/find-contact-by-address contacts address)))

(re-frame/reg-sub
 :get-contacts-by-address
 :<- [:get-contacts]
 (fn [contacts]
   (reduce (fn [acc [_ {:keys [address] :as contact}]]
             (if address
               (assoc acc address contact)
               acc))
           {}
           contacts)))
