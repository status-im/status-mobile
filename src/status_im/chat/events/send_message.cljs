(ns status-im.chat.events.send-message
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.constants :as constants]
            [status-im.utils.clocks :as clocks]
            [status-im.utils.config :as config]
            [status-im.chat.models :as chat.models]
            [status-im.chat.utils :as chat.utils]
            [status-im.protocol.core :as protocol]
            [status-im.native-module.core :as status]
            [taoensso.timbre :as log]))

(re-frame/reg-fx
  ::send-notification
  (fn [fcm-token]
    (log/debug "send-notification fcm-token: " fcm-token) 
    (status/notify fcm-token #(log/debug "send-notification cb result: " %))))

(re-frame/reg-fx
  ::send-message
  (fn [message]
    (protocol/send-message! message)))

(re-frame/reg-fx
  ::send-group-message
  (fn [message]
    (protocol/send-group-message! message)))

(re-frame/reg-fx
  ::send-public-group-message
  (fn [message]
    (protocol/send-public-group-message! message)))

(defn- send-message
  [{:keys          [web3 network-status local-storage chats]
    :contacts/keys [contacts]
    :as db}
   {:keys [message-type content from chat-id to] :as message}]
  (let [{:keys [dapp? fcm-token]}        (get contacts chat-id)
        {:keys [public-key private-key]} (get chats chat-id)
        sender-name                      (get-in contacts [from :name])]
    ;; whenever we are sending message to DApp, we are assuming it's a status bot,
    ;; so we are just calling jail `on-message-send` function
    (when message
      (if dapp?
        {:call-jail-function {:chat-id    chat-id
                              :function   :on-message-send
                              :parameters {:message content}
                              :content    {:data (get local-storage chat-id)
                                           :from from}}}
        (let [payload         (select-keys message [:timestamp :content :content-type
                                                    :clock-value :show?])
              message-to-send {:web3    web3
                               :message (-> (select-keys message [:message-id :from])
                                            (assoc :payload (if (= :offline network-status) 
                                                              (assoc payload :show? false)
                                                              payload)))}]
          (case message-type
            :group-user-message        {::send-group-message
                                        (assoc message-to-send
                                               :group-id chat-id
                                               :keypair  {:public  public-key
                                                          :private private-key})}
            :public-group-user-message {::send-public-group-message
                                        (assoc message-to-send
                                               :group-id chat-id
                                               :username sender-name)}
            :user-message              (cond-> {::send-message
                                                (assoc-in message-to-send
                                                          [:message :to] to)} 
                                         (and fcm-token config/notifications-wip-enabled?)
                                         (assoc ::send-notification fcm-token))))))))

(defn prepare-message
  [{:keys [db now random-id get-last-clock-value] :as cofx}
   {:keys [chat-id identity message-text] :as params}]
  (let [{:keys [group-chat public?]}   (get-in db [:chats chat-id])
        message (cond-> {:message-id   random-id
                         :chat-id      chat-id
                         :content      message-text
                         :from         identity
                         :content-type constants/text-content-type
                         :outgoing     true
                         :timestamp    now
                         :clock-value  (clocks/send (get-last-clock-value chat-id))
                         :show?        true}
                  (not group-chat)
                  (assoc :message-type :user-message
                         :to           chat-id)
                  group-chat
                  (assoc :group-id chat-id)
                  (and group-chat public?)
                  (assoc :message-type :public-group-user-message)
                  (and group-chat (not public?))
                  (assoc :message-type :group-user-message))]
    (as-> (chat.models/upsert-chat cofx {:chat-id chat-id})
        fx (merge fx
                  {:db           (chat.utils/add-message-to-db (:db fx) chat-id chat-id message)
                   :save-message message
                   ;; TODO janherich - get rid of this, there is absolutely no reason why it can't be just
                   ;; plain app-db inc + chat update
                   :dispatch     [:update-message-overhead! chat-id (:network-status db)]}
                  (send-message (:db fx) message)))))
