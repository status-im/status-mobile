(ns status-im.data-store.chats
  (:require [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.data-store.messages :as messages]
            [status-im.ethereum.core :as ethereum]
            [taoensso.timbre :as log]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.core :as utils]))

(defn remove-empty-vals
  "Remove key/value when empty seq or nil"
  [e]
  (into {} (remove (fn [[_ v]]
                     (or (nil? v)
                         (and (coll? v)
                              (empty? v)))) e)))

(defn- event->string
  "Transform an event in an a vector with keys in alphabetical order, to compute
  a predictable id"
  [event]
  (js/JSON.stringify
   (clj->js
    (mapv
     #(vector % (get event %))
     (sort (keys event))))))

; Build an event id from a message
(def event-id (comp ethereum/sha3 event->string))

(defn marshal-membership-updates [updates]
  (mapcat (fn [{:keys [signature events from]}]
            (map #(assoc %
                         :id (event-id %)
                         :signature signature
                         :from from) events)) updates))

(defn unmarshal-membership-updates [chat-id updates]
  (->> updates
       vals
       (group-by :signature)
       (map (fn [[signature events]]
              {:events (map #(-> (dissoc % :signature :from :id)
                                 remove-empty-vals) events)
               :from  (-> events first :from)
               :signature signature
               :chat-id chat-id}))))

(defn- normalize-chat [{:keys [chat-id] :as chat}]
  (-> chat
      (update :admins   #(into #{} %))
      (update :contacts #(into #{} %))
      (update :members-joined #(into #{} %))
      (update :tags #(into #{} %))
      (update :membership-updates  (partial unmarshal-membership-updates chat-id))
      (update :last-clock-value utils.clocks/safe-timestamp)
      (update :last-message-content utils/safe-read-message-content)))

(defn save-chat-tx
  "Returns tx function for saving chat"
  [chat]
  (fn [realm]))

;; Only used in debug mode
(defn delete-chat-tx
  "Returns tx function for hard deleting the chat"
  [chat-id]
  (fn [realm]))

(defn- get-chat-by-id [chat-id realm])

(defn clear-history-tx
  "Returns tx function for clearing the history of chat"
  [chat-id deleted-at-clock-value]
  (fn [realm]
    (let [chat (get-chat-by-id chat-id realm)]
      (doto chat
        (aset "last-message-content" nil)
        (aset "last-message-content-type" nil)
        (aset "deleted-at-clock-value" deleted-at-clock-value)))))

(defn deactivate-chat-tx
  "Returns tx function for deactivating chat"
  [chat-id now]
  (fn [realm]
    (let [chat (get-chat-by-id chat-id realm)]
      (doto chat
        (aset "is-active" false)))))

(defn add-chat-contacts-tx
  "Returns tx function for adding chat contacts"
  [chat-id contacts]
  (fn [realm]))

(defn remove-chat-contacts-tx
  "Returns tx function for removing chat contacts"
  [chat-id contacts]
  (fn [realm]))

(defn add-chat-tag-tx
  "Returns tx function for adding chat contacts"
  [chat-id tag]
  (fn [realm]))

(defn remove-chat-tag-tx
  "Returns tx function for removing chat contacts"
  [chat-id tag]
  (fn [realm]))

(re-frame/reg-cofx
 :data-store/all-chats
 (fn [cofx _]
   (assoc cofx :get-all-stored-chats (fn [] {}))))
