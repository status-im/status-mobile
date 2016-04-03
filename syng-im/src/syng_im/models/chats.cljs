(ns syng-im.models.chats
  (:require [syng-im.persistence.realm :as r]
            [syng-im.utils.random :refer [timestamp]]
            [clojure.string :refer [join blank?]]
            [syng-im.db :as db]
            [syng-im.utils.logging :as log]))

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

(defn create-chat
  ([db chat-id identities]
   (create-chat db chat-id identities nil))
  ([db chat-id identities chat-name]
   (if (r/exists? :chats :chat-id chat-id)
     db
     (let [chat-name (or chat-name
                         (get-chat-name chat-id identities))
           _         (log/debug "creating chat" chat-name)]
       (r/write
         (fn []
           (let [group-chat? (> (count identities) 1)
                 contacts    (mapv (fn [ident]
                                     {:identity ident}) identities)]
             (r/create :chats {:chat-id    chat-id
                               :name       chat-name
                               :group-chat group-chat?
                               :timestamp  (timestamp)
                               :contacts   contacts}))))
       (signal-chats-updated db)))))

(defn chats-list []
  (-> (r/get-all :chats)
      (r/sorted :timestamp :desc)))

(defn chat-by-id [chat-id]
  (-> (r/get-by-field :chats :chat-id chat-id)
      (r/single-cljs)))

(comment
  (chat-by-id "1459693168208-31d4942e-ca3b-5c03-a397-cd7a29f777d4")

  (chats-list)
  (r/delete (chats-list))

  (swap! re-frame.db/app-db signal-chats-updated)

  (create-chat "0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"
               ["0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"])

  (+ 1 1)



  (swap! re-frame.db/app-db (fn [db]
                              (create-chat db "A group chat")))

  )