(ns syng-im.models.chats
  (:require [clojure.set :refer [difference]]
            [syng-im.persistence.realm :as r]
            [syng-im.utils.random :refer [timestamp]]
            [clojure.string :refer [join blank?]]
            [syng-im.db :as db]
            [syng-im.utils.logging :as log]
            [syng-im.persistence.realm-queries :refer [include-query]]
            [syng-im.models.chat :refer [signal-chat-updated]]))

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

(defn create-chat
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
             (r/create :chats {:chat-id    chat-id
                               :is-active  true
                               :name       chat-name
                               :group-chat group-chat?
                               :timestamp  (timestamp)
                               :contacts   contacts}))))
       (signal-chats-updated db)))))

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

(defn chats-list []
  (r/sorted (r/get-all :chats) :timestamp :desc))

(defn chat-by-id [chat-id]
  (-> (r/get-by-field :chats :chat-id chat-id)
      (r/single-cljs)
      (r/list-to-array :contacts)))

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

(comment
  (active-group-chats)


  (-> (r/get-by-field :chats :chat-id "0x04ed4c3797026cddeb7d64a54ca58142e57ea03cda21072358d67455b506db90c56d95033e3d221992f70d01922c3d90bf0697c49e4be118443d03ae4a1cd3c15c")
      (r/single)
      (aget "contacts")
      (.map (fn [object index collection]
              object)))

  (-> (chat-by-id "0x04ed4c3797026cddeb7d64a54ca58142e57ea03cda21072358d67455b506db90c56d95033e3d221992f70d01922c3d90bf0697c49e4be118443d03ae4a1cd3c15c")
      :contacts
      vals
      vec)


  (-> (aget (aget (chats-list) 0) "contacts")
      (r/cljs-list))

  (r/delete (chats-list))

  (swap! re-frame.db/app-db signal-chats-updated)

  (create-chat "0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"
               ["0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"])

  (+ 1 1)



  (swap! re-frame.db/app-db (fn [db]
                              (create-chat db "A group chat")))


  (-> (chats-list)
      (.find (fn [object index collection]
               (= "console1" (aget object "chat-id")))))
  )
