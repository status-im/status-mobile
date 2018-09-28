(ns status-im.group-chats.core
  (:require
   [status-im.utils.config :as config]
   [status-im.transport.utils :as transport.utils]
   [status-im.transport.db :as transport.db]
   [status-im.transport.utils :as transport.utils]
   [status-im.transport.message.core :as protocol.message]
   [status-im.transport.message.v1.core :as transport]
   [status-im.transport.message.v1.protocol :as transport.protocol]
   [status-im.utils.fx :as fx]
   [status-im.chat.models :as models.chat]))

(defn wrap-group-message [cofx chat-id message]
  (when-let [chat (get-in cofx [:db :chats chat-id])]
    (transport/GroupMembershipUpdate.
     chat-id
     (:name chat)
     (:group-admin chat)
     (:contacts chat)
     nil
     nil
     message)))

(defn update-membership [cofx previous-chat {:keys [chat-id chat-name participants leaves signature version]}]
  (when (< (:membership-version previous-chat)
           version)
    (models.chat/upsert-chat cofx
                             {:chat-id chat-id
                              :membership-version version})))

(defn send-membership-update [cofx payload chat-id]
  (let [{:keys [participants]} payload
        {:keys [current-public-key web3]} (:db cofx)]
    (fx/merge
     cofx
     {:shh/send-group-message {:web3 web3
                               :src     current-public-key

                               :dsts    (disj participants current-public-key)
                               :success-event [:transport/set-message-envelope-hash
                                               chat-id
                                               (transport.utils/message-id (:message payload))
                                               :group-user-message]
                               :payload payload}})))

(defn handle-group-leave [payload chat-id cofx]
  (transport.protocol/send cofx
                           {:chat-id       chat-id
                            :payload       payload
                            :success-event [:group/unsubscribe-from-chat chat-id]}))

(fx/defn handle-membership-update [cofx {:keys [chat-id chat-name participants leaves message signature version] :as membership-update} sender-signature]
  (when config/group-chats-enabled?
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
                  #(protocol.message/receive message chat-id sender-signature nil %))
        chat-fx))))
