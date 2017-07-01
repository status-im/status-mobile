(ns status-im.contacts.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.identicon :refer [identicon]]
            [clojure.string :as str]
            [status-im.bots.constants :as bots-constants]))

(reg-sub :current-contact
  (fn [db [_ k]]
    (get-in db [:contacts (:current-chat-id db) k])))

(reg-sub :get-contacts
  (fn [db _]
    (:contacts db)))

(defn sort-contacts [contacts]
  (sort (fn [c1 c2]
          (let [name1 (or (:name c1) (:address c1) (:whisper-identity c1))
                name2 (or (:name c2) (:address c2) (:whisper-identity c2))]
            (compare (clojure.string/lower-case name1)
                     (clojure.string/lower-case name2))))
        (vals contacts)))


(reg-sub :all-added-contacts
  (fn [db]
    (let [contacts (:contacts db)]
      (->> (remove (fn [[_ {:keys [pending? whisper-identity]}]]
                     (or (true? pending?)
                         (bots-constants/hidden-bots whisper-identity))) contacts)
           (sort-contacts)))))

(reg-sub :all-added-people-contacts
  :<- [:all-added-contacts]
  (fn [contacts]
    (remove #(true? (:dapp? %)) contacts)))

(reg-sub :people-in-current-chat
  (fn [{:keys [current-chat-id]} _]
    (let [contacts (subscribe [:current-chat-contacts])]
      (remove #(true? (:dapp? %)) @contacts))))

(defn filter-group-contacts [group-contacts contacts]
  (filter #(group-contacts (:whisper-identity %)) contacts))

(reg-sub :all-added-group-contacts
  (fn [db [_ group-id]]
    (let [contacts (subscribe [:all-added-contacts])
          group-contacts (into #{} (map #(:identity %)
                                        (get-in db [:contact-groups group-id :contacts])))]
      (filter-group-contacts group-contacts @contacts))))

(defn filter-not-group-contacts [group-contacts contacts]
  (remove #(group-contacts (:whisper-identity %)) contacts))

(reg-sub :all-not-added-group-contacts
  (fn [db [_ group-id]]
    (let [contacts (subscribe [:all-added-contacts])
          group-contacts (into #{} (map #(:identity %)
                                        (get-in db [:contact-groups group-id :contacts])))]
      (filter-not-group-contacts group-contacts @contacts))))

(reg-sub :all-added-group-contacts-with-limit
  (fn [db [_ group-id limit]]
    (let [contacts (subscribe [:all-added-group-contacts group-id])]
      (take limit @contacts))))

(reg-sub :all-added-group-contacts-count
  (fn [_ [_ group-id]]
    (let [contacts (subscribe [:all-added-group-contacts group-id])]
      (count @contacts))))

(reg-sub :get-added-contacts-with-limit
  :<- [:all-added-contacts]
  (fn [contacts [_ limit]]
    (take limit contacts)))

(reg-sub :added-contacts-count
  :<- [:all-added-contacts]
  (fn [contacts]
    (count contacts)))

(reg-sub :all-added-groups
  (fn [db]
    (let [groups (vals (:contact-groups db))]
      (->> (remove :pending? groups)
           (sort-by :order >)))))

(defn get-contact-letter [contact]
  (when-let [letter (first (:name contact))]
    (clojure.string/upper-case letter)))

(defn search-filter [text item]
  (let [name (-> (or (:name item) "")
                 (str/lower-case))
        text (str/lower-case text)]
    (not= (str/index-of name text) nil)))

(defn search-filter-reaction [contacts]
  (let [text (subscribe [:get-in [:toolbar-search :text]])]
    (if @text
      (filter #(search-filter @text %) @contacts)
      @contacts)))

(reg-sub :all-added-group-contacts-filtered
  (fn [_ [_ group-id]]
    (let [contacts (if group-id
                     (subscribe [:all-added-group-contacts group-id])
                     (subscribe [:all-added-contacts]))]
      (search-filter-reaction contacts))))

(reg-sub :all-group-not-added-contacts-filtered
  (fn [db]
    (let [contact-group-id (:contact-group-id db)
          contacts (subscribe [:all-not-added-group-contacts contact-group-id])]
      (search-filter-reaction contacts))))

(reg-sub :contacts-filtered
  (fn [_ [_ subscription-id]]
    (let [contacts (subscribe [subscription-id])]
      (search-filter-reaction contacts))))

(reg-sub :contacts-with-letters
  (fn [db]
    (let [contacts (:contacts db)]
      (let [ordered (sort-contacts contacts)]
        (reduce (fn [prev cur]
                  (let [prev-letter (get-contact-letter (last prev))
                        cur-letter  (get-contact-letter cur)]
                    (conj prev
                          (if (not= prev-letter cur-letter)
                            (assoc cur :letter cur-letter)
                            cur))))
                [] ordered)))))

(defn contacts-by-chat [fn db chat-id]
  (let [chat     (get-in db [:chats chat-id])
        contacts (:contacts db)]
    (when chat
      (let [current-participants (->> chat
                                      :contacts
                                      (map :identity)
                                      set)]
        (fn #(current-participants (:whisper-identity %))
            (vals contacts))))))

(defn contacts-by-current-chat [fn db]
  (let [current-chat-id (:current-chat-id db)]
    (contacts-by-chat fn db current-chat-id)))

(reg-sub :contact
  (fn [db]
    (let [identity (:contact-identity db)]
      (get-in db [:contacts identity]))))

(reg-sub :contact-by-identity
  (fn [db [_ identity]]
    (get-in db [:contacts identity])))

(reg-sub :contact-name-by-identity
  :<- [:get-contacts]
  (fn [contacts [_ identity]]
    (:name (contacts identity))))

(reg-sub :all-new-contacts
  (fn [db]
    (contacts-by-current-chat remove db)))

(reg-sub :current-chat-contacts
  (fn [db]
    (contacts-by-current-chat filter db)))

(reg-sub :chat-photo
  (fn [db [_ chat-id]]
    (let [chat-id  (or chat-id (:current-chat-id db))
          chat     (get-in db [:chats chat-id])
          contacts (contacts-by-chat filter db chat-id)]
      (when (and chat (not (:group-chat chat)))
        (cond
          (:photo-path chat)
          (:photo-path chat)

          (pos? (count contacts))
          (:photo-path (first contacts))

          :else
          (identicon chat-id))))))
