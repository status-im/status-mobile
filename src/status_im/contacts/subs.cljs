(ns status-im.contacts.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]
            [status-im.utils.identicon :refer [identicon]]
            [clojure.string :as str]))

(register-sub :current-contact
  (fn [db [_ k]]
    (-> @db
        (get-in [:contacts (:current-chat-id @db) k])
        (reaction))))

(register-sub :get-contacts
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (reaction @contacts))))

(defn sort-contacts [contacts]
  (sort (fn [c1 c2]
          (let [name1 (or (:name c1) (:address c1) (:whisper-identity c1))
                name2 (or (:name c2) (:address c2) (:whisper-identity c2))]
            (compare (clojure.string/lower-case name1)
                     (clojure.string/lower-case name2))))
        (vals contacts)))

(register-sub :all-added-contacts
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (->> (remove #(true? (:pending? (second %))) @contacts)
           (sort-contacts)
           (reaction)))))

(defn filter-group-contacts [group-contacts contacts]
  (filter #(group-contacts (:whisper-identity %)) contacts))

(register-sub :all-added-group-contacts
  (fn [db [_ group-id]]
    (let [contacts (subscribe [:all-added-contacts])
          group-contacts (reaction (into #{} (map #(:identity %)
                                                  (get-in @db [:contact-groups group-id :contacts]))))]
      (reaction (filter-group-contacts @group-contacts @contacts)))))

(defn filter-not-group-contacts [group-contacts contacts]
  (remove #(group-contacts (:whisper-identity %)) contacts))

(register-sub :all-not-added-group-contacts
  (fn [db [_ group-id]]
    (let [contacts (subscribe [:all-added-contacts])
          group-contacts (reaction (into #{} (map #(:identity %)
                                                  (get-in @db [:contact-groups group-id :contacts]))))]
      (reaction (filter-not-group-contacts @group-contacts @contacts)))))

(register-sub :all-added-group-contacts-with-limit
  (fn [db [_ group-id limit]]
    (let [contacts (subscribe [:all-added-group-contacts group-id])]
      (reaction (take limit @contacts)))))

(register-sub :all-added-group-contacts-count
  (fn [_ [_ group-id]]
    (let [contacts (subscribe [:all-added-group-contacts group-id])]
      (reaction (count @contacts)))))

(register-sub :get-added-contacts-with-limit
  (fn [_ [_ limit]]
    (let [contacts (subscribe [:all-added-contacts])]
      (reaction (take limit @contacts)))))

(register-sub :added-contacts-count
  (fn [_ _]
    (let [contacts (subscribe [:all-added-contacts])]
      (reaction (count @contacts)))))

(register-sub :all-added-groups
  (fn [db _]
    (let [groups (reaction (vals (:contact-groups @db)))]
      (->> (remove :pending? @groups)
           (sort-by :order >)
           (reaction)))))

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
    (reaction
      (if @text
        (filter #(search-filter @text %) @contacts)
        @contacts))))

(register-sub :all-added-group-contacts-filtered
  (fn [_ [_ group-id]]
    (let [contacts (if group-id
                     (subscribe [:all-added-group-contacts group-id])
                     (subscribe [:all-added-contacts]))]
      (search-filter-reaction contacts))))

(register-sub :all-group-not-added-contacts-filtered
  (fn [db _]
    (let [contact-group-id (:contact-group-id @db)
          contacts (subscribe [:all-not-added-group-contacts contact-group-id])]
      (search-filter-reaction contacts))))

(register-sub :contacts-filtered
  (fn [db [_ subscription-id]]
    (let [contacts (subscribe [subscription-id])]
      (search-filter-reaction contacts))))

(register-sub :contacts-with-letters
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (reaction
        (let [ordered (sort-contacts @contacts)]
          (reduce (fn [prev cur]
                    (let [prev-letter (get-contact-letter (last prev))
                          cur-letter  (get-contact-letter cur)]
                      (conj prev
                            (if (not= prev-letter cur-letter)
                              (assoc cur :letter cur-letter)
                              cur))))
                  [] ordered))))))

(defn contacts-by-chat [fn db chat-id]
  (let [chat     (reaction (get-in @db [:chats chat-id]))
        contacts (reaction (:contacts @db))]
    (reaction
      (when @chat
        (let [current-participants (->> @chat
                                        :contacts
                                        (map :identity)
                                        set)]
          (fn #(current-participants (:whisper-identity %))
              (vals @contacts)))))))

(defn contacts-by-current-chat [fn db]
  (let [current-chat-id (:current-chat-id @db)]
    (contacts-by-chat fn db current-chat-id)))

(register-sub :contact
  (fn [db _]
    (let [identity (:contact-identity @db)]
      (reaction (get-in @db [:contacts identity])))))

(register-sub :contact-by-identity
  (fn [db [_ identity]]
    (reaction (get-in @db [:contacts identity]))))

(register-sub :all-new-contacts
  (fn [db _]
    (contacts-by-current-chat remove db)))

(register-sub :current-chat-contacts
  (fn [db _]
    (contacts-by-current-chat filter db)))

(register-sub :chat-photo
  (fn [db [_ chat-id]]
    (let [chat-id  (or chat-id (:current-chat-id @db))
          chat     (reaction (get-in @db [:chats chat-id]))
          contacts (contacts-by-chat filter db chat-id)]
      (reaction
        (when (and @chat (not (:group-chat @chat)))
          (cond
            (:photo-path @chat)
            (:photo-path @chat)

            (pos? (count @contacts))
            (:photo-path (first @contacts))

            :else
            (identicon chat-id)))))))
