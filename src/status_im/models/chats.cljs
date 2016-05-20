(ns status-im.models.chats
  (:require [clojure.set :refer [difference]]
            [re-frame.core :refer [dispatch]]
            [status-im.persistence.realm :as r]
            [status-im.utils.random :as random :refer [timestamp]]
            [clojure.string :refer [join blank?]]
            [status-im.utils.logging :as log]
            [status-im.constants :refer [content-type-status]]
            [status-im.models.messages :refer [save-message]]
            [status-im.persistence.realm-queries :refer [include-query]]))

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
  ([db chat-id identities group-chat? chat-name]
   (when-not (chat-exists? chat-id)
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
       (add-status-message chat-id)))))

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
  db)

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
            (.push contacts (clj->js {:identity contact-identity})))))))
  ;; TODO temp. Update chat in db atom
  (dispatch [:initialize-chats]))

;; TODO deprecated? (is there need to remove multiple member at once?)
(defn chat-remove-participants [chat-id identities]
  (r/write
    (fn []
      (let [query (include-query :identity identities)
            chat  (r/single (r/get-by-field :chats :chat-id chat-id))]
        (-> (aget chat "contacts")
            (r/filtered query)
            (.forEach (fn [object _ _]
                        (aset object "is-in-chat" false))))))))

(defn active-group-chats []
  (let [results (r/filtered (r/get-all :chats)
                            "group-chat = true && is-active = true")]
    (js->clj (.map results (fn [object _ _]
                             (aget object "chat-id"))))))

(defn set-chat-active [chat-id active?]
  (r/write (fn []
             (-> (r/get-by-field :chats :chat-id chat-id)
                 (r/single)
                 (aset "is-active" active?)))))
