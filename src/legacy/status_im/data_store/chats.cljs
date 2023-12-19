(ns legacy.status-im.data-store.chats
  (:require
    [clojure.set :as set]
    [legacy.status-im.data-store.messages :as messages]
    [legacy.status-im.utils.deprecated-types :as types]
    [status-im2.constants :as constants]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn rpc->type
  [{:keys [chat-type name] :as chat}]
  (cond
    (or (= constants/profile-chat-type chat-type)
        (= constants/timeline-chat-type chat-type))
    (assoc chat
           :chat-name  (str "#" name)
           :public?    true
           :group-chat true
           :timeline?  (= constants/timeline-chat-type chat-type))
    (= constants/community-chat-type chat-type) (assoc chat
                                                       :chat-name  name
                                                       :group-chat true)
    (= constants/private-group-chat-type chat-type) (assoc chat
                                                           :chat-name  name
                                                           :public?    false
                                                           :group-chat true)
    :else (assoc chat :public? false :group-chat false)))

(defn members-reducer
  [acc member]
  (cond-> acc
    (:admin member)
    (update :admins conj (:id member))
    (:joined member)
    (update :members-joined conj (:id member))
    :always
    (update :contacts conj (:id member))))

(defn- unmarshal-members
  [{:keys [members chat-type] :as chat}]
  (cond
    (= constants/private-group-chat-type chat-type) (merge chat
                                                           (reduce members-reducer
                                                                   {:admins         #{}
                                                                    :members-joined #{}
                                                                    :contacts       #{}}
                                                                   members))
    :else
    (assoc chat
           :contacts       #{(:id chat)}
           :admins         #{}
           :members-joined #{})))

(defn <-rpc
  [chat]
  (-> chat
      (set/rename-keys {:id                     :chat-id
                        :communityId            :community-id
                        :syncedFrom             :synced-from
                        :syncedTo               :synced-to
                        :membershipUpdateEvents :membership-update-events
                        :deletedAtClockValue    :deleted-at-clock-value
                        :chatType               :chat-type
                        :unviewedMessagesCount  :unviewed-messages-count
                        :unviewedMentionsCount  :unviewed-mentions-count
                        :lastMessage            :last-message
                        :lastClockValue         :last-clock-value
                        :invitationAdmin        :invitation-admin
                        :profile                :profile-public-key
                        :muteTill               :muted-till})
      rpc->type
      unmarshal-members
      (update :last-message #(when % (messages/<-rpc %)))
      (dissoc :members)))

(defn <-rpc-js
  [^js chat]
  (-> {:name                    (.-name chat)
       :description             (.-description chat)
       :color                   (.-color chat)
       :emoji                   (.-emoji chat)
       :timestamp               (.-timestamp chat)
       :alias                   (.-alias chat)
       :muted                   (.-muted chat)
       :joined                  (.-joined chat)
       :muted-till              (.-muteTill chat)
       :chat-id                 (.-id chat)
       :community-id            (.-communityId chat)
       :synced-from             (.-syncedFrom chat)
       :synced-to               (.-syncedTo chat)
       :deleted-at-clock-value  (.-deletedAtClockValue chat)
       :chat-type               (.-chatType chat)
       :unviewed-messages-count (.-unviewedMessagesCount chat)
       :unviewed-mentions-count (.-unviewedMentionsCount chat)
       :last-message            {:content            {:text        (.-text chat)
                                                      :parsed-text (types/js->clj (.-parsedText chat))
                                                      :response-to (.-responseTo chat)}
                                 :content-type       (.-contentType chat)
                                 :community-id       (.-contentCommunityId chat)
                                 :outgoing           (boolean (.-outgoingStatus chat))
                                 :album-images-count (.-albumImagesCount chat)
                                 :from               (.-from chat)
                                 :deleted?           (.-deleted chat)
                                 :deleted-for-me?    (.-deletedForMe chat)}
       :last-clock-value        (.-lastClockValue chat)
       :profile-public-key      (.-profile chat)
       :highlight               (.-highlight chat)
       :active                  (.-active chat)
       :members                 (types/js->clj (.-members chat))}
      rpc->type
      unmarshal-members))

(rf/defn fetch-chats-preview
  [_ {:keys [on-success]}]
  {:json-rpc/call [{:method      "wakuext_chatsPreview"
                    :params      []
                    :js-response true
                    :on-success  #(on-success ^js %)
                    :on-error    #(log/error "failed to fetch chats" 0 -1 %)}]})
