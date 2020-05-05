(ns status-im.group-chats.core
  (:refer-clojure :exclude [remove])
  (:require [clojure.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.chat.models :as models.chat]
            [status-im.chat.models.message :as models.message]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.group-chats.db :as group-chats.db]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.transport.filters.core :as transport.filters]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.waku.core :as waku]))

(fx/defn remove-member
  "Format group update message and sign membership"
  [{:keys [db] :as cofx} chat-id member]
  {::json-rpc/call [{:method (json-rpc/call-ext-method (waku/enabled? cofx) "removeMemberFromGroupChat")
                     :params [nil chat-id member]
                     :on-success #(re-frame/dispatch [::chat-updated %])}]})

(fx/defn set-up-filter
  "Listen/Tear down the shared topic/contact-codes. Stop listening for members who
  have left the chat"
  [cofx chat-id previous-chat]
  (let [my-public-key (multiaccounts.model/current-public-key cofx)
        new-chat (get-in cofx [:db :chats chat-id])
        members (:members-joined new-chat)]
    ;; If we left the chat do nothing
    (when-not (and (group-chats.db/joined? my-public-key previous-chat)
                   (not (group-chats.db/joined? my-public-key new-chat)))
      (fx/merge
       cofx
       (transport.filters/upsert-group-chat-topics)
       (transport.filters/load-members members)))))

(fx/defn handle-chat-update
  {:events [::chat-updated]}
  [cofx {:keys [chats messages]}]
  (let [{:keys [chat-id] :as chat} (-> chats
                                       first
                                       (data-store.chats/<-rpc))

        previous-chat (get-in cofx [:chats chat-id])
        set-up-filter-fx (set-up-filter chat-id previous-chat)
        chat-fx (models.chat/ensure-chat
                 (dissoc chat :unviewed-messages-count))
        messages-fx (map #(models.message/receive-one
                           (data-store.messages/<-rpc %))
                         messages)
        navigate-fx #(if (get-in % [:db :chats chat-id :is-active])
                       (models.chat/navigate-to-chat % chat-id)
                       (navigation/navigate-to-cofx % :home {}))]

    (apply fx/merge cofx (concat [chat-fx]
                                 messages-fx
                                 [navigate-fx]))))

(fx/defn join-chat
  [cofx chat-id]
  {::json-rpc/call [{:method (json-rpc/call-ext-method (waku/enabled? cofx) "confirmJoiningGroup")
                     :params [chat-id]
                     :on-success #(re-frame/dispatch [::chat-updated %])}]})

(fx/defn create
  [{:keys [db] :as cofx} group-name]
  (let [selected-contacts (:group/selected-contacts db)]
    {::json-rpc/call [{:method (json-rpc/call-ext-method (waku/enabled? cofx) "createGroupChatWithMembers")
                       :params [nil group-name (into [] selected-contacts)]
                       :on-success #(re-frame/dispatch [::chat-updated %])}]}))

(fx/defn make-admin
  [{:keys [db] :as cofx} chat-id member]
  {::json-rpc/call [{:method (json-rpc/call-ext-method (waku/enabled? cofx) "addAdminsToGroupChat")
                     :params [nil chat-id [member]]
                     :on-success #(re-frame/dispatch [::chat-updated %])}]})

(fx/defn add-members
  "Add members to a group chat"
  [{{:keys [current-chat-id selected-participants]} :db :as cofx}]
  {::json-rpc/call [{:method (json-rpc/call-ext-method (waku/enabled? cofx) "addMembersToGroupChat")
                     :params [nil current-chat-id selected-participants]
                     :on-success #(re-frame/dispatch [::chat-updated %])}]})

(fx/defn leave
  "Leave chat"
  {:events [:group-chats.ui/leave-chat-confirmed]}
  [{:keys [db] :as cofx} chat-id]
  {::json-rpc/call [{:method (json-rpc/call-ext-method (waku/enabled? cofx) "leaveGroupChat")
                     :params [nil chat-id true]
                     :on-success #(re-frame/dispatch [::chat-updated %])}]})

(defn- valid-name? [name]
  (spec/valid? :profile/name name))

(fx/defn name-changed
  "Save chat from edited profile"
  {:events [:group-chats.ui/name-changed]}
  [{:keys [db] :as cofx} chat-id new-name]
  (when (valid-name? new-name)
    {:db (assoc-in db [:chats chat-id :name] new-name)
     ::json-rpc/call [{:method (json-rpc/call-ext-method (waku/enabled? cofx) "changeGroupChatName")
                       :params [nil chat-id new-name]
                       :on-success #(re-frame/dispatch [::chat-updated %])}]}))
