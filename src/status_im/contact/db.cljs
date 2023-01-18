(ns status-im.contact.db
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [status-im2.setup.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]))

(defn public-key->new-contact
  [public-key]
  (let [alias (gfycat/generate-gfy public-key)]
    {:alias      alias
     :name       alias
     :identicon  (identicon/identicon public-key)
     :public-key public-key}))

(defn public-key-and-ens-name->new-contact
  [public-key ens-name]
  (let [contact (public-key->new-contact public-key)]
    (if ens-name
      (-> contact
          (assoc :ens-name ens-name)
          (assoc :ens-verified true)
          (assoc :name ens-name))
      contact)))

(defn public-key->contact
  [contacts public-key]
  (when public-key
    (or (get contacts public-key)
        (public-key->new-contact public-key))))

(defn- contact-by-address
  [[addr contact] address]
  (when (ethereum/address= addr address)
    contact))

(defn find-contact-by-address
  [contacts address]
  (some #(contact-by-address % address) contacts))

(defn sort-contacts
  [contacts]
  (sort (fn [c1 c2]
          (let [name1 (or (:name c1) (:address c1) (:public-key c1))
                name2 (or (:name c2) (:address c2) (:public-key c2))]
            (compare (string/lower-case name1)
                     (string/lower-case name2))))
        (vals contacts)))

(defn filter-dapps
  [v dev-mode?]
  (remove #(when-not dev-mode? (true? (:developer? %))) v))

(defn filter-group-contacts
  [group-contacts contacts]
  (let [group-contacts' (into #{} group-contacts)]
    (filter #(group-contacts' (:public-key %)) contacts)))

(defn query-chat-contacts
  [{:keys [contacts]} all-contacts query-fn]
  (let [participant-set (into #{} (filter identity) contacts)]
    (query-fn (comp participant-set :public-key) (vals all-contacts))))

(defn get-all-contacts-in-group-chat
  [members admins contacts {:keys [public-key] :as current-account}]
  (let [current-contact (some->
                         current-account
                         (select-keys [:name :preferred-name :public-key :identicon :images])
                         (set/rename-keys {:name           :alias
                                           :preferred-name :name}))
        all-contacts    (cond-> contacts
                          current-contact
                          (assoc public-key current-contact))]
    (->> members
         (map #(or (get all-contacts %)
                   (public-key->new-contact %)))
         (sort-by (comp string/lower-case #(or (:name %) (:alias %))))
         (map #(if (get admins (:public-key %))
                 (assoc % :admin? true)
                 %)))))

(defn contact-exists?
  [db public-key]
  (get-in db [:contacts/contacts public-key]))

(defn added?
  ([{:keys [contact-request-state]}]
   (or (= constants/contact-request-state-mutual contact-request-state)
       (= constants/contact-request-state-sent contact-request-state)))
  ([db public-key]
   (added? (get-in db [:contacts/contacts public-key]))))

(defn blocked?
  ([contact]
   (:blocked contact))
  ([db public-key]
   (blocked? (get-in db [:contacts/contacts public-key]))))

(defn active?
  "Checks that we are mutual contacts"
  ([contact]
   (and (= constants/contact-request-state-mutual
           (:contact-request-state contact))
        (not (:blocked contact))))
  ([db public-key]
   (active? (get-in db [:contacts/contacts public-key]))))

(defn enrich-contact
  ([contact] (enrich-contact contact nil nil))
  ([{:keys [public-key] :as contact} setting own-public-key]
   (cond-> (-> contact
               (dissoc :ens-verified-at :ens-verification-retries)
               (assoc :blocked? (:blocked contact)
                      :active?  (active? contact)
                      :added?   (added? contact))
               (multiaccounts/contact-with-names))
     (and setting
          (not= public-key own-public-key)
          (or (= setting constants/profile-pictures-visibility-none)
              (and (= setting constants/profile-pictures-visibility-contacts-only)
                   (not (added? contact)))))
     (dissoc :images))))

(defn enrich-contacts
  [contacts profile-pictures-visibility own-public-key]
  (reduce-kv
   (fn [acc public-key contact]
     (assoc acc public-key (enrich-contact contact profile-pictures-visibility own-public-key)))
   {}
   contacts))

(defn get-blocked-contacts
  [contacts]
  (reduce (fn [acc {:keys [public-key] :as contact}]
            (if (:blocked contact)
              (conj acc public-key)
              acc))
          #{}
          contacts))

(defn get-active-contacts
  [contacts]
  (->> contacts
       (filter (fn [[_ contact]]
                 (active? contact)))
       sort-contacts))
