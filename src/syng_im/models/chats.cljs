(ns syng-im.models.chats
  (:require [syng-im.persistence.realm :as r]
            [syng-im.utils.random :refer [timestamp]]
            [clojure.string :refer [join blank?]]
            [syng-im.db :as db]
            [syng-im.utils.logging :as log]
            [syng-im.constants :refer [group-chat-colors]]))

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
  ([db chat-id identities group-chat?]
   (create-chat db chat-id identities group-chat? nil))
  ([db chat-id identities group-chat? chat-name]
   (if (r/exists? :chats :chat-id chat-id)
     db
     (let [chat-name (or chat-name
                         (get-chat-name chat-id identities))
           _         (log/debug "creating chat" chat-name)]
       (r/write
         (fn []
           (let [contacts (mapv (fn [ident {:keys [background text]}]
                                  {:identity         ident
                                   :background-color background
                                   :text-color       text}) identities group-chat-colors)]
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
      (r/single-cljs)
      (r/list-to-array :contacts)))

(defn chat-add-participants [chat-id identities]
  (r/write
    (fn []
      (let [chat           (-> (r/get-by-field :chats :chat-id chat-id)
                               (r/single))
            contacts       (aget chat "contacts")
            contacts-count (aget contacts "length")
            colors         (drop contacts-count group-chat-colors)
            new-contacts   (mapv (fn [ident {:keys [background text]}]
                                   {:identity         ident
                                    :background-color background
                                    :text-color       text}) identities colors)]
        (doseq [contact new-contacts]
          (.push contacts (clj->js contact)))))))

(defn active-group-chats []
  (let [results (-> (r/get-all :chats)
                    (r/filtered "group-chat = true"))]
    (->> (.map results (fn [object index collection]
                         (aget object "chat-id")))
         (js->clj))))

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
      (js->clj :keywordize-keys true)
      )

  (r/delete (chats-list))

  (swap! re-frame.db/app-db signal-chats-updated)

  (create-chat "0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"
               ["0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd"])

  (+ 1 1)



  (swap! re-frame.db/app-db (fn [db]
                              (create-chat db "A group chat")))

  )