(ns legacy.status-im.contact.db
  (:require
    [clojure.set :as set]
    [clojure.string :as string]
    [status-im2.constants :as constants]
    [utils.address :as address]))

(defn public-key-and-ens-name->new-contact
  [public-key ens-name]
  (let [contact {:public-key public-key}]
    (if ens-name
      (-> contact
          (assoc :ens-name ens-name)
          (assoc :ens-verified true)
          (assoc :name ens-name))
      contact)))

(defn public-key->contact
  [contacts public-key]
  (when public-key
    (get contacts public-key {:public-key public-key})))

(defn- contact-by-address
  [[addr contact] address]
  (when (address/address= addr address)
    contact))

(defn find-contact-by-address
  [contacts address]
  (some #(contact-by-address % address) contacts))

(defn sort-contacts
  [contacts]
  (sort (fn [c1 c2]
          (let [name1 (:primary-name c1)
                name2 (:primary-name c2)]
            (when (and name1 name2)
              (compare (string/lower-case name1)
                       (string/lower-case name2)))))
        (vals contacts)))

(defn query-chat-contacts
  [{:keys [contacts]} all-contacts query-fn]
  (let [participant-set (into #{} (filter identity) contacts)]
    (query-fn (comp participant-set :public-key) (vals all-contacts))))

(defn get-all-contacts-in-group-chat
  [members admins contacts {:keys [public-key preferred-name name] :as current-account}]
  (let [current-contact (some->
                          current-account
                          (select-keys [:name :preferred-name :public-key :images])
                          (set/rename-keys {:name :alias :preferred-name :name})
                          (assoc :primary-name (or preferred-name name)))
        all-contacts    (cond-> contacts
                          current-contact
                          (assoc public-key current-contact))]
    (->> members
         (map #(or (get all-contacts %)
                   {:public-key %}))
         (sort-by (comp string/lower-case
                        (fn [{:keys [primary-name name alias public-key]}]
                          (or primary-name
                              name
                              alias
                              public-key))))
         (map #(if (get admins (:public-key %))
                 (assoc % :admin? true)
                 %)))))

(defn enrich-contact
  ([contact] (enrich-contact contact nil nil))
  ([{:keys [public-key] :as contact} setting own-public-key]
   (cond-> contact
     (and setting
          (not= public-key own-public-key)
          (or (= setting constants/profile-pictures-visibility-none)
              (and (= setting constants/profile-pictures-visibility-contacts-only)
                   (not (:added? contact)))))
     (dissoc :images))))

(defn enrich-contacts
  [contacts profile-pictures-visibility own-public-key]
  (reduce-kv
   (fn [acc public-key contact]
     (assoc acc public-key (enrich-contact contact profile-pictures-visibility own-public-key)))
   {}
   contacts))
