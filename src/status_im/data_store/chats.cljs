(ns status-im.data-store.chats
  (:require [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.data-store.messages :as messages]
            [status-im.utils.fx :as fx]
            [status-im.data-store.realm.core :as core]
            [status-im.ethereum.json-rpc :as json-rpc]
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

(def one-to-one-chat-type 1)
(def public-chat-type 2)
(def private-group-chat-type 3)

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
            (map #(-> %
                      (assoc
                       :clockValue (:clock-value %)
                       :id (event-id %)
                       :signature signature
                       :from from)
                      (dissoc :clock-value)) events)) updates))

(defn unmarshal-membership-updates [chat-id updates]
  (->> updates
       (group-by :signature)
       (map (fn [[signature events]]
              {:events
               (into []
                     (sort-by :clock-value (map #(-> %
                                                     (assoc :clock-value (:clockValue %))
                                                     (update :members (fn [members] (into #{} members)))
                                                     (dissoc :signature :from :id :clockValue)
                                                     remove-empty-vals) events)))
               :from  (-> events first :from)
               :signature signature
               :chat-id chat-id}))
       (into #{})))

(defn type->rpc [{:keys [public? group-chat] :as chat}]
  (assoc chat :chatType (cond
                          public? public-chat-type
                          group-chat private-group-chat-type
                          :else one-to-one-chat-type)))

(defn rpc->type [{:keys [chatType] :as chat}]
  (cond
    (= public-chat-type chatType) (assoc chat :public? true :group-chat true)
    (= private-group-chat-type chatType) (assoc chat :public? false :group-chat true)
    :else (assoc chat :public? false :group-chat false)))

(defn- marshal-members [{:keys [admins contacts members-joined chatType] :as chat}]
  (cond-> chat
    (= chatType private-group-chat-type)
    (assoc :members (map #(hash-map :id %
                                    :admin (boolean (admins %))
                                    :joined (boolean (members-joined %))) contacts))
    :always
    (dissoc :admins :contacts :members-joined)))

(defn- unmarshal-members [{:keys [members chatType] :as chat}]
  (cond
    (= public-chat-type chatType) (assoc chat
                                         :contacts #{}
                                         :admins #{}
                                         :members-joined #{})
    (= private-group-chat-type chatType) (merge chat
                                                (reduce (fn [acc member]
                                                          (cond-> acc
                                                            (:admin member)
                                                            (update :admins conj (:id member))
                                                            (:joined member)
                                                            (update :members-joined conj (:id member))
                                                            :always
                                                            (update :contacts conj (:id member))))
                                                        {:admins #{}
                                                         :members-joined #{}
                                                         :contacts #{}}
                                                        members))
    :else
    (assoc chat
           :contacts #{(:id chat)}
           :admins #{}
           :members-joined #{})))

(defn- ->rpc [chat]
  (-> chat
      type->rpc
      marshal-members
      (update :membership-updates marshal-membership-updates)
      (utils/update-if-present :last-message-content messages/prepare-content)
      (clojure.set/rename-keys {:chat-id :id
                                :membership-updates :membershipUpdates
                                :unviewed-messages-count :unviewedMessagesCount
                                :last-message-content :lastMessageContent
                                :last-message-content-type :lastMessageContentType
                                :deleted-at-clock-value :deletedAtClockValue
                                :is-active :active
                                :last-clock-value :lastClockValue})
      (dissoc :referenced-messages :message-groups :gaps-loaded? :pagination-info
              :public? :group-chat :messages
              :might-have-join-time-messages?
              :group-chat-local-version :loaded-unviewed-messages-ids
              :messages-initialized? :contacts :admins :members-joined)))

(defn <-rpc [chat]
  (-> chat
      rpc->type
      unmarshal-members
      (clojure.set/rename-keys {:id :chat-id
                                :membershipUpdates :membership-updates
                                :unviewedMessagesCount :unviewed-messages-count
                                :lastMessageContent :last-message-content
                                :lastMessageContentType :last-message-content-type
                                :deletedAtClockValue :deleted-at-clock-value
                                :active :is-active
                                :lastClockValue :last-clock-value})
      (update :membership-updates (partial unmarshal-membership-updates (:id chat)))
      (update :last-message-content utils/safe-read-message-content)
      (update :last-clock-value utils.clocks/safe-timestamp)
      (assoc :group-chat-local-version 1) ;; TODO(cammellos): this can be removed
      (dissoc :chatType :members)))

(fx/defn save-chat [cofx {:keys [chat-id] :as chat}]
  {::json-rpc/call [{:method "shhext_saveChat"
                     :params [(->rpc chat)]
                     :on-success #(log/debug "saved chat" chat-id "successfuly")
                     :on-failure #(log/error "failed to save chat" chat-id %)}]})

(fx/defn fetch-chats-rpc [cofx {:keys [on-success]}]
  {::json-rpc/call [{:method "shhext_chats"
                     :params [0 -1]
                     :on-success #(on-success (map <-rpc %))
                     :on-failure #(log/error "failed to fetch chats" 0 -1 %)}]})

(defn delete-chat-rpc [chat-id chat-type]
  (json-rpc/call {:method "shhext_deleteChat"
                  :params [chat-id chat-type]
                  :on-success #(log/debug "deleteed chat" chat-id chat-type)
                  :on-failure #(log/error "failed to delete chat" chat-id chat-type %)}))

(re-frame/reg-fx
 ::delete-chat
 (fn [[chat-id chat-type]]
   (delete-chat-rpc chat-id chat-type)))

(fx/defn delete-chat [cofx chat-id chat-type]
  {::delete-chat [chat-id chat-type]})
