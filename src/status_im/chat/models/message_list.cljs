(ns status-im.chat.models.message-list
  (:require
   [status-im.js-dependencies :as dependencies]
   [taoensso.timbre :as log]
   [status-im.utils.fx :as fx]
   [status-im.chat.db :as chat.db]
   [status-im.utils.datetime :as time]))

(defn- add-datemark [{:keys [whisper-timestamp] :as msg}]
  (assoc msg :datemark (time/day-relative whisper-timestamp)))

(defn- add-timestamp [{:keys [whisper-timestamp] :as msg}]
  (assoc msg :timestamp-str (time/timestamp->time whisper-timestamp)))

(defn prepare-message [{:keys [message-id
                               clock-value
                               message-type
                               outgoing
                               whisper-timestamp]}]
  (-> {:whisper-timestamp whisper-timestamp
       :one-to-one? (= :user-message message-type)
       :system-message? (= :system-message message-type)
       :clock-value clock-value
       :type :message
       :message-id message-id
       :outgoing outgoing}
      add-datemark
      add-timestamp))

;; any message that comes after this amount of ms will be grouped separately
(def ^:private group-ms 60000)

(defn same-group?
  "Whether a message is in the same group as the one after it.
  We check the time, and the author"
  [a b]
  (and
   (= (:from a) (:from b))
   (<= (js/Math.abs (- (:whisper-timestamp a) (:whisper-timestamp b))) group-ms)))

(defn display-photo?
  "We display photos for other users, and not in 1-to-1 chats"
  [{:keys [system-message? one-to-one?
           outgoing message-type]}]
  (or system-message?
      (and
       (not outgoing)
       (not one-to-one?))))

(defn compare-fn
  "Compare two messages, first compare by clock-value, and break ties by message-id,
  which gives us total ordering across all clients"
  [a b]
  (let [initial-comparison (compare (:clock-value b) (:clock-value a))]
    (if (= initial-comparison 0)
      (compare (:message-id a) (:message-id b))
      initial-comparison)))

(defn add-group-info
  "Add positional data to a message, based on the next and previous message.
  We divide messages in groups. Messages are sorted descending so :first? is
  the most recent message, similarly :first-in-group? is the most recent message
  in a group."
  [{:keys [one-to-one? outgoing] :as current-message}
   {:keys [outgoing-seen?] :as previous-message}
   next-message]
  (let [last-in-group? (or (nil? next-message)
                           (not (same-group? current-message next-message)))]
    (assoc current-message
           :first?            (nil? previous-message)
           :first-outgoing?   (and outgoing
                                   (not outgoing-seen?))
           :outgoing-seen?    (or outgoing-seen?
                                  outgoing)
           :first-in-group?   (or (nil? previous-message)
                                  (not (same-group? current-message previous-message)))
           :last-in-group?    (or (nil? next-message)
                                  (not (same-group? current-message next-message)))
           :display-username? (and last-in-group?
                                   (not outgoing)
                                   (not one-to-one?))
           :display-photo?   (display-photo? current-message))))

(defn update-next-message
  "Update next message in the list, we set :first? to false, and check if it
  :first-outgoing? state has changed because of the insertion"
  [current-message next-message]
  (assoc
   next-message
   :first? false
   :first-outgoing? (and
                     (not (:first-outgoing? current-message))
                     (:first-outgoing? next-message))
   :outgoing-seen? (:outgoing-seen? current-message)
   :first-in-group?
   (not (same-group? current-message next-message))))

(defn update-previous-message
  "If this is a new group, we mark the previous as the last one in the group"
  [current-message {:keys [one-to-one? outgoing] :as previous-message}]
  (let [last-in-group? (not (same-group? current-message previous-message))]
    (assoc previous-message
           :display-username? (and last-in-group?
                                   (not outgoing)
                                   (not one-to-one?))
           :last-in-group?  last-in-group?)))

(defn get-prev-element
  "Get previous item in the iterator, and wind it back to the initial state"
  [iter]
  (.prev iter)
  (let [e (.-value iter)]
    (.next iter)
    e))

(defn get-next-element
  "Get next item in the iterator, and wind it back to the initial state"
  [iter]
  (.next iter)
  (let [e (.-value iter)]
    (.prev iter)
    e))

(defn insert-message
  "Insert a message in the list, pull it's left and right messages, calculate
  its positional metadata, and update the left & right messages if necessary,
  this operation is O(logN) for insertion, and O(logN) for the updates, as
  we need to re-find (there's probably a better way)"
  [old-message-list {:keys [key] :as prepared-message}]
  (let [tree (.insert old-message-list prepared-message prepared-message)
        iter (.find tree prepared-message)
        previous-message (when (.-hasPrev iter)
                           (get-prev-element iter))
        next-message     (when (.-hasNext iter)
                           (get-next-element iter))
        message-with-pos-data (add-group-info prepared-message previous-message next-message)]
    (cond->
     (.update iter message-with-pos-data)

      next-message
      (-> (.find next-message)
          (.update (update-next-message message-with-pos-data next-message)))

      (and previous-message
           (not= :datemark (:type previous-message)))
      (-> (.find previous-message)
          (.update (update-previous-message message-with-pos-data previous-message))))))

(defn add [message-list message]
  (insert-message (or message-list (dependencies/rb-tree compare-fn))
                  (prepare-message message)))

(defn add-many [message-list messages]
  (reduce add
          message-list
          messages))
