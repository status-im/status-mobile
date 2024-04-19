(ns legacy.status-im.group-chats.db)

(def members-added-type 3)

(defn member?
  [public-key {:keys [members contacts users]}]
  (let [members-list (into #{} (concat (keys users) contacts (map #(:id %) members)))]
    (contains? members-list public-key)))

(defn invited?
  [my-public-key {:keys [contacts]}]
  (contains? contacts my-public-key))

(defn get-inviter-pk
  [my-public-key {:keys [membership-update-events]}]
  (->> membership-update-events
       reverse
       (keep (fn [{:keys [from type members]}]
               (when (and (= type members-added-type)
                          ((set members) my-public-key))
                 from)))
       first))

(defn group-chat?
  [chat]
  (and (:group-chat chat) (not (:public? chat))))
