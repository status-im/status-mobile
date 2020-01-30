(ns status-im.group-chats.core
  (:refer-clojure :exclude [remove])
  (:require [clojure.set :as clojure.set]
            [clojure.spec.alpha :as spec]
            [clojure.string :as string]
            [status-im.ethereum.json-rpc :as json-rpc]
            [re-frame.core :as re-frame]
            [status-im.utils.config :as config]
            [status-im.constants :as constants]
            [status-im.chat.models.message-content :as message-content]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.data-store.chats :as data-store.chats]
            [status-im.data-store.messages :as data-store.messages]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.utils.pairing :as pairing.utils]
            [status-im.chat.models :as models.chat]
            [status-im.chat.models.message :as models.message]
            [status-im.contact.core :as models.contact]
            [status-im.transport.filters.core :as transport.filters]
            [status-im.group-chats.db :as group-chats.db]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as native-module]
            [status-im.transport.message.protocol :as protocol]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.fx :as fx]
            [status-im.mailserver.core :as mailserver]
            [status-im.mailserver.topics :as mailserver.topics]
            [taoensso.timbre :as log]))

(fx/defn remove-member
  "Format group update message and sign membership"
  [{:keys [db] :as cofx} chat-id member]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_removeMemberFromGroupChat"
                               "shhext_removeMemberFromGroupChat")
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
                       (models.chat/navigate-to-chat % chat-id {:navigation-reset? true})
                       (navigation/navigate-to-cofx % :home {}))]

    (apply fx/merge cofx (concat [chat-fx]
                                 messages-fx
                                 [navigate-fx]))))

(fx/defn join-chat
  [cofx chat-id]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_confirmJoiningGroup"
                               "shhext_confirmJoiningGroup")
                     :params [chat-id]
                     :on-success #(re-frame/dispatch [::chat-updated %])}]})

(fx/defn create
  [{:keys [db] :as cofx} group-name]
  (let [selected-contacts (:group/selected-contacts db)]
    {::json-rpc/call [{:method (if config/waku-enabled?
                                 "wakuext_createGroupChatWithMembers"
                                 "shhext_createGroupChatWithMembers")
                       :params [nil group-name (into [] selected-contacts)]
                       :on-success #(re-frame/dispatch [::chat-updated %])}]}))

(fx/defn make-admin
  [{:keys [db] :as cofx} chat-id member]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_addAdminsToGroupChat"
                               "shhext_addAdminsToGroupChat")
                     :params [nil chat-id [member]]
                     :on-success #(re-frame/dispatch [::chat-updated %])}]})

(fx/defn add-members
  "Add members to a group chat"
  [{{:keys [current-chat-id selected-participants]} :db :as cofx}]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_addMembersToGroupChat"
                               "shhext_addMembersToGroupChat")
                     :params [nil current-chat-id selected-participants]
                     :on-success #(re-frame/dispatch [::chat-updated %])}]})
(fx/defn remove
  "Remove & leave chat"
  [{:keys [db] :as cofx} chat-id]
  {::json-rpc/call [{:method (if config/waku-enabled?
                               "wakuext_leaveGroupChat"
                               "shhext_leaveGroupChat")
                     :params [nil chat-id]
                     :on-success #(re-frame/dispatch [::chat-updated %])}]})

(defn- valid-name? [name]
  (spec/valid? :profile/name name))

(fx/defn update-name [{:keys [db]} name]
  {:db (-> db
           (assoc-in [:group-chat-profile/profile :valid-name?] (valid-name? name))
           (assoc-in [:group-chat-profile/profile :name] name))})

(fx/defn handle-name-changed
  "Store name in profile scratchpad"
  [cofx new-chat-name]
  (update-name cofx new-chat-name))

(fx/defn save
  "Save chat from edited profile"
  [{:keys [db] :as cofx}]
  (let [new-name (get-in cofx [:db :group-chat-profile/profile :name])
        current-chat-id (:current-chat-id db)]
    (when (valid-name? new-name)
      {::json-rpc/call [{:method (if config/waku-enabled?
                                   "wakuext_changeGroupChatName"
                                   "shhext_changeGroupChatName")
                         :params [nil current-chat-id new-name]
                         :on-success #(re-frame/dispatch [::chat-updated %])}]})))

