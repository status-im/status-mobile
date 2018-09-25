(ns status-im.group-chats.core
  (:require
   [status-im.transport.utils :as transport.utils]
   [status-im.transport.db :as transport.db]
   [status-im.transport.message.core :as protocol.message]
   [status-im.utils.fx :as fx]
   [status-im.chat.models :as models.chat]))

(fx/defn wrap-group-message [cofx chat-id message]
  (when-let [chat (get-in cofx [:db :chats chat-id])]
    (GroupMembershipUpdate.
     chat-id
     (:name chat)
     (:group-admin chat)
     (:contacts chat)
     nil
     nil
     message)))

(fx/defn update-membership [cofx previous-chat {:keys [chat-id chat-name participants leaves signature version]}]
  (when (< (:membership-version previous-chat)
           version)
    (models.chat/upsert-chat cofx
                             {:chat-id chat-id
                              :membership-version version})))

(fx/defn send-membership-update [cofx payload chat-id]
  (let [{:keys [participants]} payload
        {:keys [current-public-key web3]} (:db cofx)]
    (fx/merge
     cofx
     {:shh/send-group-message {:web3 web3
                               :src     current-public-key
                               :dsts    (disj participants current-public-key)
                               :payload payload}})))

(fx/defn handle-membership-update [cofx {:keys [chat-id chat-name participants leaves message signature version] :as membership-update} sender-signature]
  (let [chat-fx (if-let [previous-chat (get-in cofx [:db :chats chat-id])]
                  (update-membership cofx previous-chat membership-update)
                  (models.chat/upsert-chat
                   cofx
                   {:chat-id chat-id
                    :name chat-name
                    :is-active true
                    :group-chat true
                    :group-admin signature
                    :contacts participants
                    :membership-version version}))]
    (if message
      (fx/merge cofx
                chat-fx
                (protocol.message/receive message chat-id sender-signature nil))
      chat-fx)))
