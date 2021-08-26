(ns status-im.data-store.chats
  (:require [clojure.set :as clojure.set]
            [status-im.data-store.messages :as messages]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.constants :as constants]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn rpc->type [{:keys [chatType name] :as chat}]
  (cond
    (or (= constants/public-chat-type chatType)
        (= constants/profile-chat-type chatType)
        (= constants/timeline-chat-type chatType)) (assoc chat
                                                          :chat-name (str "#" name)
                                                          :public? true
                                                          :group-chat true
                                                          :timeline? (= constants/timeline-chat-type chatType))
    (= constants/community-chat-type chatType) (assoc chat
                                                      :chat-name name
                                                      :group-chat true)
    (= constants/private-group-chat-type chatType) (assoc chat
                                                          :chat-name name
                                                          :public? false
                                                          :group-chat true)
    :else (assoc chat :public? false :group-chat false)))

(defn- unmarshal-members [{:keys [members chatType] :as chat}]
  (cond
    (= constants/public-chat-type chatType) (assoc chat
                                                   :contacts #{}
                                                   :admins #{}
                                                   :members-joined #{})
    (= constants/private-group-chat-type chatType) (merge chat
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

(defn <-rpc [chat]
  (-> chat
      rpc->type
      unmarshal-members
      (clojure.set/rename-keys {:id :chat-id
                                :communityId :community-id
                                :syncedFrom :synced-from
                                :syncedTo :synced-to
                                :membershipUpdateEvents :membership-update-events
                                :deletedAtClockValue :deleted-at-clock-value
                                :chatType :chat-type
                                :unviewedMessagesCount :unviewed-messages-count
                                :unviewedMentionsCount :unviewed-mentions-count
                                :lastMessage :last-message
                                :active :is-active
                                :lastClockValue :last-clock-value
                                :invitationAdmin :invitation-admin
                                :profile :profile-public-key})
      (update :last-message #(when % (messages/<-rpc %)))
      (dissoc :members)))

(fx/defn fetch-chats-rpc [cofx {:keys [on-success]}]
  {::json-rpc/call [{:method (json-rpc/call-ext-method "chats")
                     :params []
                     :on-success #(on-success (map <-rpc %))
                     :on-failure #(log/error "failed to fetch chats" 0 -1 %)}]})
