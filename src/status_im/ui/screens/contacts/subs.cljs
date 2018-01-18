(ns status-im.ui.screens.contacts.subs
  (:require [clojure.string :as string]
            [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.identicon :as identicon]))

(reg-sub :current-contact
  (fn [db [_ k]]
    (get-in db [:contacts/contacts (:current-chat-id db) k])))

(reg-sub :get-contacts
  (fn [db _]
    (:contacts/contacts db)))

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
    (remove #(true? (:dapp? %)) contacts)))

(reg-sub :people-in-current-chat
  :<- [:current-chat-contacts]
  (fn [contacts]
    (remove #(true? (:dapp? %)) contacts)))

(defn filter-group-contacts [group-contacts contacts]
  (let [group-contacts' (into #{} (map #(:identity %) group-contacts))]
    (filter #(group-contacts' (:whisper-identity %)) contacts)))

(reg-sub :group-contacts
  (fn [db [_ group-id]]
    (get-in db [:group/contact-groups group-id :contacts])))

(reg-sub :all-added-group-contacts
  (fn [[_ group-id] _]
    [(subscribe [:all-added-contacts])
     (subscribe [:group-contacts group-id])])
  (fn [[contacts group-contacts] _]
    (filter-group-contacts group-contacts contacts)))

(defn filter-not-group-contacts [group-contacts contacts]
  (let [group-contacts' (into #{} (map #(:identity %) group-contacts))]
    (remove #(group-contacts' (:whisper-identity %)) contacts)))

(reg-sub :all-not-added-group-contacts
  (fn [[_ group-id] _]
    [(subscribe [:all-added-contacts])
     (subscribe [:group-contacts group-id])])
  (fn [[contacts group-contacts]]
    (filter-not-group-contacts group-contacts contacts)))

(reg-sub
  :all-added-group-contacts-with-limit
  (fn [[_ group-id limit] _]
    (subscribe [:all-added-group-contacts group-id]))
  (fn [contacts [_ group-id limit]]
    (take limit contacts)))

(reg-sub :all-added-group-contacts-count
  (fn [[_ group-id] _]
    (subscribe [:all-added-group-contacts group-id]))
  (fn [contacts _]
    (count contacts)))

(reg-sub :get-added-contacts-with-limit
  :<- [:all-added-contacts]
  (fn [contacts [_ limit]]
    (take limit contacts)))

(reg-sub :added-contacts-count
  :<- [:all-added-contacts]
  (fn [contacts]
    (count contacts)))

(reg-sub :all-added-groups
  :<- [:get-contact-groups]
  (fn [groups]
    (->> (remove :pending? (vals groups))
         (sort-by :order >))))

(defn search-filter [text item]
  (let [name (-> (or (:name item) "")
                 (string/lower-case))
        text (string/lower-case text)]
    (not= (string/index-of name text) nil)))

(defn search-filter-reaction [contacts text]
  (if text
    (filter #(search-filter text %) contacts)
    contacts))

(reg-sub :all-added-group-contacts-filtered
  (fn [[_ group-id] _]
    [(if group-id
       (subscribe [:all-added-group-contacts group-id])
       (subscribe [:all-added-contacts]))
     (subscribe [:get-in [:toolbar-search :text]])])
  (fn [[contacts text] _]
    (search-filter-reaction contacts text)))

(reg-sub :contact-group-contacts
  :<- [:get-contact-group]
  (fn [group]
    (:contacts group)))

(reg-sub :all-not-added-contact-group-contacts
  (fn [_ _]
    [(subscribe [:all-added-contacts])
     (subscribe [:contact-group-contacts])])
  (fn [[contacts group-contacts]]
    (filter-not-group-contacts group-contacts contacts)))

(reg-sub :all-group-not-added-contacts-filtered
  (fn [_ _]
    [(subscribe [:all-not-added-contact-group-contacts])
     (subscribe [:get-in [:toolbar-search :text]])])
  (fn [[contacts text] _]
    (search-filter-reaction contacts text)))

(reg-sub :contacts-filtered
  (fn [[_ subscription-id] _]
    [(subscribe [subscription-id])
     (subscribe [:get-in [:toolbar-search :text]])])
  (fn [[contacts text]]
    (search-filter-reaction contacts text)))

(reg-sub :contact
  (fn [db]
    (let [identity (:contacts/identity db)]
      (get-in db [:contacts/contacts identity]))))

(reg-sub :contact-by-identity
  (fn [db [_ identity]]
    (get-in db [:contacts/contacts identity])))

(reg-sub :contact-name-by-identity
  :<- [:get-contacts]
  (fn [contacts [_ identity]]
    (:name (contacts identity))))

(defn chat-contacts [[chat contacts] [_ fn]]
  (when chat
    (let [current-participants (->> chat
                                    :contacts
                                    (map :identity)
                                    set)]
      (fn #(current-participants (:whisper-identity %))
        (vals contacts)))))

(reg-sub :contacts-current-chat
  :<- [:get-current-chat]
  :<- [:get-contacts]
  chat-contacts)

(reg-sub :all-new-contacts
  :<- [:contacts-current-chat remove]
  (fn [contacts]
    contacts))

(reg-sub :current-chat-contacts
  :<- [:contacts-current-chat filter]
  (fn [contacts]
    contacts))

(reg-sub :contacts-by-chat
  (fn [[_ _ chat-id] _]
    [(subscribe [:get-chat chat-id])
     (subscribe [:get-contacts])])
  chat-contacts)

(reg-sub :get-chat-photo
  (fn [[_ chat-id] _]
    [(subscribe [:get-chat chat-id])
     (subscribe [:contacts-by-chat filter chat-id])])
  (fn [[chat contacts] [_ chat-id]]
    (when (and chat (not (:group-chat chat)))
      (cond
        (:photo-path chat)
        (:photo-path chat)

        (pos? (count contacts))
        (:photo-path (first contacts))

        :else
        (identicon/identicon chat-id)))))

(defn- address= [{:keys [address] :as contact} s]
  (when (and address (= (ethereum/normalized-address s)
                        (ethereum/normalized-address address)))
    contact))

(defn- contact-by-address [[_ contact] s]
  (when (address= contact s)
    contact))

(reg-sub :contact/by-address
  :<- [:get-contacts]
  (fn [contacts [_ address]]
    (some #(contact-by-address % address) contacts)))

(reg-sub :contacts/by-address
  :<- [:get-contacts]
  (fn [contacts]
    (reduce (fn [acc [_ {:keys [address] :as contact}]]
              (if address
                (assoc acc address contact)
                acc))
            {}
            contacts)))
