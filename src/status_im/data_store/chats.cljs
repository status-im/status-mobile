(ns status-im.data-store.chats
  (:require [goog.object :as object]
            [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.utils.ethereum.core :as utils.ethereum]
            [status-im.data-store.realm.core :as core]))

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
(def event-id (comp utils.ethereum/sha3 event->string))

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

(defn- get-last-clock-value [chat-id]
  (-> (core/get-by-field @core/account-realm
                         :message :chat-id chat-id)
      (core/sorted :clock-value :desc)
      (core/single-clj :message)
      :clock-value))

(defn- normalize-chat [{:keys [chat-id] :as chat}]
  (let [last-clock-value (get-last-clock-value chat-id)]
    (-> chat
        (update :admins   #(into #{} %))
        (update :contacts #(into #{} %))
        (update :membership-updates  (partial unmarshal-membership-updates chat-id))
        (assoc :last-clock-value (or last-clock-value 0)))))

(re-frame/reg-cofx
 :data-store/all-chats
 (fn [cofx _]
   (assoc cofx :all-stored-chats (map normalize-chat
                                      (-> @core/account-realm
                                          (core/get-all :chat)
                                          (core/sorted :timestamp :desc)
                                          (core/all-clj :chat))))))

(defn save-chat-tx
  "Returns tx function for saving chat"
  [{:keys [chat-id] :as chat}]
  (fn [realm]
    (core/create
     realm
     :chat
     (update chat :membership-updates marshal-membership-updates)
     true)))

;; Only used in debug mode
(defn delete-chat-tx
  "Returns tx function for hard deleting the chat"
  [chat-id]
  (fn [realm]
    (core/delete realm (core/get-by-field realm :chat :chat chat-id))))

(defn- get-chat-by-id [chat-id realm]
  (core/single (core/get-by-field realm :chat :chat-id chat-id)))

(defn clear-history-tx
  "Returns tx function for clearing the history of chat"
  [chat-id deleted-at-clock-value]
  (fn [realm]
    (let [chat (get-chat-by-id chat-id realm)]
      (doto chat
        (aset "deleted-at-clock-value" deleted-at-clock-value)))))

(defn deactivate-chat-tx
  "Returns tx function for deactivating chat"
  [chat-id now]
  (fn [realm]
    (let [chat (get-chat-by-id chat-id realm)]
      (doto chat
        (aset "is-active" false)
        (aset "removed-at" now)))))

(defn add-chat-contacts-tx
  "Returns tx function for adding chat contacts"
  [chat-id contacts]
  (fn [realm]
    (let [chat              (get-chat-by-id chat-id realm)
          existing-contacts (object/get chat "contacts")]
      (aset chat "contacts"
            (clj->js (into #{} (concat contacts
                                       (core/list->clj existing-contacts))))))))

(defn remove-chat-contacts-tx
  "Returns tx function for removing chat contacts"
  [chat-id contacts]
  (fn [realm]
    (let [chat              (get-chat-by-id chat-id realm)
          existing-contacts (object/get chat "contacts")]
      (aset chat "contacts"
            (clj->js (remove (into #{} contacts)
                             (core/list->clj existing-contacts)))))))
