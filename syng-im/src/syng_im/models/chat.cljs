(ns syng-im.models.chat
  (:require [syng-im.db :as db]))

(defn set-current-chat-id [db chat-id]
  (assoc-in db db/current-chat-id-path chat-id))

(defn current-chat-id [db]
  (get-in db db/current-chat-id-path))

(defn signal-chat-updated [db chat-id]
  (update-in db (db/latest-msg-id-path chat-id) (fn [current]
                                                  (if current
                                                    (inc current)
                                                    0))))

(defn latest-msg-id [db chat-id]
  (->> (db/latest-msg-id-path chat-id)
       (get-in db)))

(comment

  (swap! re-frame.db/app-db (fn [db]
                              (signal-chat-updated db "0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd")))
  )