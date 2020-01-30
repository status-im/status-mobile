(ns status-im.data-store.chats
  (:require [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.data-store.messages :as messages]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
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
      (update :last-message messages/->rpc)
      (clojure.set/rename-keys {:chat-id :id
                                :membership-update-events :membershipUpdateEvents
                                :unviewed-messages-count :unviewedMessagesCount
                                :last-message :lastMessage
                                :deleted-at-clock-value :deletedAtClockValue
                                :is-active :active
                                :last-clock-value :lastClockValue})
      (dissoc :message-list :gaps-loaded? :pagination-info
              :public? :group-chat :messages
              :might-have-join-time-messages?
              :loaded-unviewed-messages-ids
              :messages-initialized? :contacts :admins :members-joined)))

(defn <-rpc [chat]
  (-> chat
      rpc->type
      unmarshal-members
      (clojure.set/rename-keys {:id :chat-id
                                :membershipUpdateEvents :membership-update-events
                                :deletedAtClockValue :deleted-at-clock-value
                                :unviewedMessagesCount :unviewed-messages-count
                                :lastMessage :last-message
                                :active :is-active
                                :lastClockValue :last-clock-value})
      (update :last-message #(when % (messages/<-rpc %)))
      (dissoc :chatType :members)))

(fx/defn save-chat [cofx {:keys [chat-id] :as chat}]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_saveChat"
                               "shhext_saveChat")
                     :params [(->rpc chat)]
                     :on-success #(log/debug "saved chat" chat-id "successfuly")
                     :on-failure #(log/error "failed to save chat" chat-id %)}]})

(fx/defn fetch-chats-rpc [cofx {:keys [on-success]}]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_chats"
                               "shhext_chats")
                     :params []
                     :on-success #(on-success (map <-rpc %))
                     :on-failure #(log/error "failed to fetch chats" 0 -1 %)}]})
