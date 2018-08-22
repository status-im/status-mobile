(ns status-im.ui.screens.contacts.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.contacts :as utils.contacts]))

(reg-sub :get-current-contact-identity :contacts/identity)

(reg-sub :get-contacts :contacts/contacts)

(reg-sub :get-dapps :contacts/dapps)

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

(defn- filter-dapps [v dev-mode?]
  (remove #(when-not dev-mode? (true? (:developer? %))) v))

(reg-sub :all-dapps
         :<- [:get-dapps]
         :<- [:get-current-account]
         (fn [[dapps {:keys [dev-mode?]}]]
           (map (fn [m] (update m :data #(filter-dapps % dev-mode?))) dapps)))

(reg-sub :get-people-in-current-chat
         :<- [:get-current-chat-contacts]
         (fn [contacts]
           (remove #(true? (:dapp? %)) contacts)))

(defn filter-group-contacts [group-contacts contacts]
  (let [group-contacts' (into #{} group-contacts)]
    (filter #(group-contacts' (:whisper-identity %)) contacts)))

(reg-sub :get-contact-by-identity
         :<- [:get-contacts]
         :<- [:get-current-chat]
         (fn [[all-contacts {:keys [contacts]}] [_ identity]]
           (let [identity' (or identity (first contacts))]
             (or
              (get all-contacts identity')
              (utils.contacts/whisper-id->new-contact identity')))))

(reg-sub :get-dapp-by-name
         :<- [:get-dapps]
         (fn [dapps [_ name]]
           (first (filter #(= (:name %) name) (apply concat (map :data dapps))))))

(reg-sub :get-contact-name-by-identity
         :<- [:get-contacts]
         :<- [:get-current-account]
         (fn [[contacts current-account] [_ identity]]
           (let [me? (= (:public-key current-account) identity)]
             (if me?
               (:name current-account)
               (:name (contacts identity))))))

(defn query-chat-contacts [[{:keys [contacts group-admin]} all-contacts] [_ query-fn]]
  (let [participant-set (into #{} (filter identity) (conj contacts group-admin))]
    (query-fn (comp participant-set :whisper-identity) (vals all-contacts))))

(reg-sub :query-current-chat-contacts
         :<- [:get-current-chat]
         :<- [:get-contacts]
         query-chat-contacts)

(reg-sub :get-all-contacts-not-in-current-chat
         :<- [:query-current-chat-contacts remove]
         identity)

(defn get-all-contacts-in-group-chat [chat-contact-ids group-admin-id contacts current-account]
  (let [participant-set         (into #{} (filter identity) (conj chat-contact-ids group-admin-id))
        current-account-contact (-> current-account
                                    (select-keys [:name :photo-path :public-key])
                                    (clojure.set/rename-keys {:public-key :whisper-identity}))
        all-contacts            (assoc contacts (:whisper-identity current-account-contact) current-account-contact)]
    (map #(or (get all-contacts %)
              (utils.contacts/whisper-id->new-contact %))
         participant-set)))

(reg-sub :get-current-chat-contacts
         :<- [:get-current-chat]
         :<- [:get-contacts]
         :<- [:get-current-account]
         (fn [[{:keys [contacts group-admin]} all-contacts current-account]]
           (get-all-contacts-in-group-chat contacts group-admin all-contacts current-account)))

(reg-sub :get-contacts-by-chat
         (fn [[_ _ chat-id] _]
           [(subscribe [:get-chat chat-id])
            (subscribe [:get-contacts])])
         query-chat-contacts)

(reg-sub :get-chat-photo
         (fn [[_ chat-id] _]
           [(subscribe [:get-chat chat-id])
            (subscribe [:get-contacts-by-chat filter chat-id])])
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
