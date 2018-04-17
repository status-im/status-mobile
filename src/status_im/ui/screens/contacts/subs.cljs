(ns status-im.ui.screens.contacts.subs
  (:require [clojure.string :as string]
            [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.identicon :as identicon]))

(reg-sub :get-current-contact-identity :contacts/identity)

(reg-sub :get-contacts :contacts/contacts)

(reg-sub :get-current-contact
  :<- [:get-contacts]
  :<- [:get-current-contact-identity]
  (fn [[contacts identity]]
    (contacts identity)))

(reg-sub :get-current-chat-contact
  :<- [:get-contacts]
  :<- [:get-current-chat-id] 
  (fn [[contacts chat-id]]
    (get contacts chat-id)))

(defn sort-contacts [contacts]
  (sort (fn [c1 c2]
          (let [name1 (or (:name c1) (:address c1) (:whisper-identity c1))
                name2 (or (:name c2) (:address c2) (:whisper-identity c2))]
            (compare (clojure.string/lower-case name1)
                     (clojure.string/lower-case name2))))
        (vals contacts)))

(reg-sub :all-added-contacts
  :<- [:get-contacts]
  (fn [contacts]
    (->> contacts
         (remove (fn [[_ {:keys [pending? hide-contact?]}]]
                   (or pending? hide-contact?)))
         (sort-contacts))))

(reg-sub :all-added-people-contacts
  :<- [:all-added-contacts]
  (fn [contacts]
    (remove :dapp? contacts)))

(reg-sub :all-dapp-with-url-contacts
  :<- [:all-added-contacts]
  (fn [contacts]
    (filter #(and (:dapp? %) (:dapp-url %)) contacts)))

(defn filter-group-contacts [group-contacts contacts]
  (let [group-contacts' (into #{} group-contacts)]
    (filter #(group-contacts' (:whisper-identity %)) contacts)))

(reg-sub :get-all-added-group-contacts
  :<- [:all-added-contacts]
  :<- [:get-contact-groups] 
  (fn [[contacts contact-groups] [_ group-id]]
    (filter-group-contacts (get-in contact-groups [group-id :contacts]) contacts)))

(reg-sub :get-contact-by-identity
  :<- [:get-contacts]
  (fn [contacts [_ identity]]
    (get contacts identity)))

(reg-sub :get-contact-name-by-identity
  :<- [:get-contacts]
  :<- [:get-current-account]
  (fn [[contacts current-account] [_ identity]]
    (let [me? (= (:public-key current-account) identity)]
      (if me?
        (:name current-account)
        (:name (contacts identity))))))

(defn- chat-contacts-set [{:keys [contacts group-admin]}]
  (into #{} (filter identity) (conj contacts group-admin)))

(defn- chat-contacts [[chat contacts]]
  (let [contacts-set (chat-contacts-set chat)]
    (filter (comp contacts-set :whisper-identity) (vals contacts))))

(reg-sub :get-current-chat-contacts
  :<- [:get-current-chat]
  :<- [:get-contacts]
  chat-contacts)

(reg-sub :get-all-people-contacts-not-in-current-chat
  :<- [:get-current-chat]
  :<- [:get-contacts]
  (fn [[chat contacts]]
    (let [contacts-set (chat-contacts-set chat)]
      (remove (fn [{:keys [whisper-identity dapp?]}]
                (or dapp? (contacts-set whisper-identity)))
              (vals contacts)))))

(reg-sub :get-chat-contacts
  (fn [[_ chat-id]]
    [(subscribe [:get-chat chat-id])
     (subscribe [:get-contacts])])
  chat-contacts)

(reg-sub :get-chat-photo
  (fn [[_ chat-id] _]
    [(subscribe [:get-chat chat-id])
     (subscribe [:get-chat-contacts chat-id])])
  (fn [[chat contacts] [_ chat-id]] 
    (when (and chat (not (:group-chat chat)))
      (or (:photo-path chat) (:photo-path (first contacts)) (identicon/identicon chat-id)))))

(defn- address= [{:keys [address] :as contact} s]
  (when (and address (= (ethereum/normalized-address s)
                        (ethereum/normalized-address address)))
    contact))

(defn- contact-by-address [[_ contact] s]
  (when (address= contact s)
    contact))

(reg-sub :get-contact-by-address
  :<- [:get-contacts]
  (fn [contacts [_ address]]
    (some #(contact-by-address % address) contacts)))

(reg-sub :get-contacts-by-address
  :<- [:get-contacts]
  (fn [contacts]
    (reduce (fn [acc [_ {:keys [address] :as contact}]]
              (if address
                (assoc acc address contact)
                acc))
            {}
            contacts)))
