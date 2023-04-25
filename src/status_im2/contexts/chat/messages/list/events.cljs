(ns status-im2.contexts.chat.messages.list.events
  (:require [status-im2.constants :as constants]
            [utils.datetime :as datetime]
            [utils.re-frame :as rf]
            [utils.red-black-tree :as red-black-tree]))

(defn- add-datemark
  [{:keys [whisper-timestamp] :as msg}]
  ;;NOTE(performance) this is slow
  (assoc msg :datemark (datetime/day-relative whisper-timestamp)))

(defn- add-timestamp
  [{:keys [whisper-timestamp] :as msg}]
  (assoc msg :timestamp-str (datetime/timestamp->time whisper-timestamp)))

(defn prepare-message
  [{:keys [message-id
           clock-value
           message-type
           album-id
           from
           outgoing
           whisper-timestamp
           deleted?
           deleted-for-me?
           albumize?]}]
  (-> {:whisper-timestamp whisper-timestamp
       :from              from
       :one-to-one?       (= constants/message-type-one-to-one message-type)
       :system-message?   (boolean
                           (or
                            (= constants/message-type-private-group-system-message
                               message-type)
                            deleted?
                            deleted-for-me?))
       :clock-value       clock-value
       :type              :message
       :message-id        message-id
       :outgoing          (boolean outgoing)
       :albumize?         (if (and album-id outgoing) true albumize?)}
      add-datemark
      add-timestamp))

(defn same-group?
  "Whether a message is in the same group as the one after it.
  We check the time, and the author"
  [a b]
  (and
   (not (:system-message? a))
   (not (:system-message? b))
   (= (:from a) (:from b))
   (<= (js/Math.abs (- (:whisper-timestamp a) (:whisper-timestamp b))) constants/group-ms)))

(defn display-photo?
  "We display photos for other users, and not in 1-to-1 chats"
  [{:keys [system-message? one-to-one? outgoing]}]
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
  [{:keys [system-message?
           one-to-one? outgoing]
    :as   current-message}
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
           :last-in-group?    last-in-group?
           :display-username? (and last-in-group?
                                   (not system-message?)
                                   (not outgoing)
                                   (not one-to-one?))
           :display-photo?    (display-photo? current-message))))

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
  [current-message
   {:keys [one-to-one?
           system-message?
           outgoing]
    :as   previous-message}]
  (let [last-in-group? (not (same-group? current-message previous-message))]
    (assoc previous-message
           :display-username? (and last-in-group?
                                   (not system-message?)
                                   (not outgoing)
                                   (not one-to-one?))
           :last-in-group?    last-in-group?)))

(defn update-message
  "Update the message and siblings with positional info"
  [tree message]
  (let [iter                  (red-black-tree/find tree message)
        previous-message      (red-black-tree/get-prev iter)
        next-message          (red-black-tree/get-next iter)
        message-with-pos-data (add-group-info message previous-message next-message)]
    (cond-> (red-black-tree/update iter message-with-pos-data)
      next-message
      (-> (red-black-tree/find next-message)
          (red-black-tree/update (update-next-message message-with-pos-data next-message)))

      (and previous-message
           (not= :datemark (:type previous-message)))
      (-> (red-black-tree/find previous-message)
          (red-black-tree/update (update-previous-message message-with-pos-data previous-message))))))

(defn remove-message
  "Remove a message in the list"
  [tree prepared-message]
  (let [iter (red-black-tree/find tree prepared-message)]
    (if (not iter)
      tree
      (let [new-tree     (red-black-tree/remove iter)
            next-message (red-black-tree/get-next iter)]
        (if (not next-message)
          new-tree
          (update-message new-tree next-message))))))

(defn insert-message
  "Insert a message in the list, pull it's left and right messages, calculate
  its positional metadata, and update the left & right messages if necessary,
  this operation is O(logN) for insertion, and O(logN) for the updates, as
  we need to re-find (there's probably a better way)"
  [old-message-list prepared-message]
  (let [tree (red-black-tree/insert old-message-list prepared-message)]
    (update-message tree prepared-message)))

(defn add
  [message-list message]
  (insert-message (or message-list (red-black-tree/tree compare-fn)) (prepare-message message)))

(defn add-many
  [message-list messages]
  (reduce add
          message-list
          messages))

(defn ->seq
  [message-list]
  (if message-list
    (array-seq (red-black-tree/get-values message-list))
    []))

;; NOTE(performance): this is too expensive, probably we could mark message somehow and just hide it in
;; the UI
(rf/defn rebuild-message-list
  [{:keys [db]} chat-id]
  {:db (assoc-in db
        [:message-lists chat-id]
        (add-many nil (vals (get-in db [:messages chat-id]))))})
