(ns syng-im.models.chats
  (:require [clojure.set :refer [difference]]
            [re-frame.core :refer [dispatch]]
            [syng-im.persistence.realm :as r]
            [syng-im.utils.random :as random :refer [timestamp]]
            [clojure.string :refer [join blank?]]
            [syng-im.db :as db]
            [syng-im.utils.logging :as log]
            [syng-im.constants :refer [content-type-status]]
            [syng-im.models.messages :refer [save-message]]
            [syng-im.persistence.realm-queries :refer [include-query]]
            [syng-im.models.chat :refer [signal-chat-updated
                                         get-group-settings]]))

(defn signal-chats-updated [db]
  (update-in db db/updated-chats-signal-path (fn [current]
                                               (if current
                                                 (inc current)
                                                 0))))

(defn chats-updated? [db]
  (get-in db db/updated-chats-signal-path))

(defn chat-name-from-contacts [identities]
  (let [chat-name (->> identities
                       (map (fn [identity]
                              (-> (r/get-by-field :contacts :whisper-identity identity)
                                  (r/single-cljs)
                                  :name)))
                       (filter identity)
                       (join ","))]
    (when-not (blank? chat-name)
      chat-name)))

(defn get-chat-name [chat-id identities]
  (or (chat-name-from-contacts identities)
      chat-id))

(defn chat-exists? [chat-id]
  (r/exists? :chats :chat-id chat-id))

(defn add-status-message [chat-id]
  ;; TODO Get real status
  (save-message chat-id
                {:from         "Status"
                 :to           nil
                 :msg-id       (random/id)
                 :content      (str "The brash businessman’s braggadocio "
                                    "and public exchange with candidates "
                                    "in the US presidential election")
                 :content-type content-type-status
                 :outgoing     false}))

(defn create-chat
  ([{:keys [last-msg-id] :as chat}]
   (let [chat (assoc chat :last-msg-id (or last-msg-id ""))]
     (r/write #(r/create :chats chat))))
  ([db chat-id identities group-chat?]
   (create-chat db chat-id identities group-chat? nil))
  ([db chat-id identities group-chat? chat-name]
   (if (chat-exists? chat-id)
     db
     (let [chat-name (or chat-name
                         (get-chat-name chat-id identities))
           _         (log/debug "creating chat" chat-name)]
       (r/write
         (fn []
           (let [contacts (mapv (fn [ident]
                                  {:identity ident}) identities)]
             (r/create :chats {:chat-id     chat-id
                               :is-active   true
                               :name        chat-name
                               :group-chat  group-chat?
                               :timestamp   (timestamp)
                               :contacts    contacts
                               :last-msg-id ""}))))
       (add-status-message chat-id)
       (signal-chats-updated db)))))

(defn save-chat [db]
  (let [chat-settings (get-group-settings db)
        chat-id (:chat-id chat-settings)]
    (r/write
     (fn []
       ;; TODO UNDONE contacts
       (r/create :chats (select-keys chat-settings [:chat-id :name]) true)))
    ;; TODO update chat in db atom
    (dispatch [:initialize-chats])
    (-> db
        (signal-chats-updated)
        (signal-chat-updated chat-id))))

(defn chat-contacts [chat-id]
  (-> (r/get-by-field :chats :chat-id chat-id)
      (r/single)
      (aget "contacts")))

(defn re-join-group-chat [db group-id identities group-name]
  (r/write
    (fn []
      (let [new-identities    (set identities)
            only-old-contacts (->> (chat-contacts group-id)
                                   (r/cljs-list)
                                   (remove (fn [{:keys [identity]}]
                                             (new-identities identity))))
            contacts          (->> new-identities
                                   (mapv (fn [ident]
                                           {:identity ident}))
                                   (concat only-old-contacts))]
        (r/create :chats {:chat-id   group-id
                          :is-active true
                          :name      group-name
                          :contacts  contacts} true))))
  (-> (signal-chats-updated db)
      (signal-chat-updated group-id)))

(defn normalize-contacts
  [chats]
  (map #(update % :contacts vals) chats))

(defn chats-list []
  (-> (r/get-all :chats)
      (r/sorted :timestamp :desc)
      r/collection->map
      normalize-contacts))

(defn chat-by-id [chat-id]
  (-> (r/get-by-field :chats :chat-id chat-id)
      (r/single-cljs)
      (r/list-to-array :contacts)))

(defn chat-by-id2 [chat-id]
  (-> (r/get-by-field :chats :chat-id chat-id)
      r/collection->map
      first))

(defn chat-add-participants [chat-id identities]
  (r/write
    (fn []
      (let [contacts (chat-contacts chat-id)]
        (doseq [contact-identity identities]
          (if-let [contact-exists (.find contacts (fn [object index collection]
                                                    (= contact-identity (aget object "identity"))))]
            (aset contact-exists "is-in-chat" true)
            (.push contacts (clj->js {:identity contact-identity}))))))))

(defn chat-remove-participants [chat-id identities]
  (r/write
    (fn []
      (let [query (include-query :identity identities)
            chat  (r/single (r/get-by-field :chats :chat-id chat-id))]
        (-> (aget chat "contacts")
            (r/filtered query)
            (.forEach (fn [object index collection]
                        (aset object "is-in-chat" false))))))))

(defn active-group-chats []
  (let [results (r/filtered (r/get-all :chats)
                            "group-chat = true && is-active = true")]
    (js->clj (.map results (fn [object index collection]
                             (aget object "chat-id"))))))


(defn set-chat-active [chat-id active?]
  (r/write (fn []
             (-> (r/get-by-field :chats :chat-id chat-id)
                 (r/single)
                 (aset "is-active" active?)))))
