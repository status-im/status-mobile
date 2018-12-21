(ns status-im.ui.screens.add-new.many-public-chats.events
  (:require [re-frame.core :as re-frame]
            [status-im.chat.models :as models]
            [status-im.transport.utils :as transport.utils]
            [status-im.utils.clocks :as clock]
            [status-im.utils.datetime :as utils.datetime]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.user-statuses :as statuses]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.accounts.db :as accounts.db]
            [status-im.transport.message.public-chat :as public-chat]))

(def another-user
  "0xf6eb6203c4b601ce393147fcb4d97c1d8113ad80f5b8b425759f97ee690b6232")

(defn generate-message
  [chat-id
   text
   from
   public-key
   clock-value
   timestamp]
  (let [from             (if (zero? (rand-int 2))
                           from
                           public-key)
        raw-payload-hash (transport.utils/sha3
                          (str clock-value text chat-id from))
        message-id       (transport.utils/sha3 raw-payload-hash)]
    {:message
     {:message-status   nil
      :old-message-id   message-id
      :message-id       message-id
      :content          {:chat-id chat-id :text text}
      :username         nil
      :show?            true
      :message-type     :public-group-user-message
      :clock-value      clock-value
      :from             from
      :chat-id          chat-id
      :content-type     "text/plain"
      :timestamp        timestamp
      :retry-count      0
      :outgoing         (= from public-key)
      :to               nil
      :raw-payload-hash raw-payload-hash}
     :status
     {:message-id message-id
      :chat-id    chat-id
      :public-key public-key
      :status     :seen}}))

(defn- get-add-chats-fx-fns
  [{:keys [base-topic timestamp message-num total-num base-message]}]
  (mapcat
   (fn [i]
     (let [topic       (str base-topic i)
           clock-value (inc (* (+ timestamp i (* 2 message-num)) 100))]
       [(models/upsert-chat
         {:chat-id                      topic
          :is-active                    true
          :name                         topic
          :group-chat                   true
          :contacts                     #{}
          :public?                      true
          :unviewed-messages-count      0
          :loaded-unviewed-messages-ids #{}
          :last-clock-value             clock-value
          :last-message-content         {:chat-id topic
                                         :text    (str base-message
                                                       (dec message-num))}
          :last-message-content-type    :public-group-user-message})
        (public-chat/join-public-chat topic)]))
   (range total-num)))

(defn- db-transactions
  [{:keys [base-topic timestamp public-key message-num total-num
           base-message]}]
  (mapcat
   (fn [i]
     (let [timestamp (+ timestamp i)
           topic     (str base-topic i)]
       (mapcat (fn [j]
                 (let [timestamp   (+ timestamp j)
                       clock-value (* timestamp 100)
                       {:keys [message status]}
                       (generate-message
                        topic
                        (str base-message j)
                        another-user
                        public-key
                        timestamp
                        clock-value)]
                   [(messages/save-message-tx message)
                    (statuses/save-status-tx status)]))
               (range message-num))))
   (range total-num)))

(handlers/register-handler-fx
 :add-many-pub-chats
 (fn [{:keys [db] :as cofx}]
   (let [{:public-chats/keys
          [base-message base-topic total-num messages-num]}
         db

         public-key       (accounts.db/current-public-key cofx)

         total-num-int    (js/parseInt total-num)
         message-num-int  (js/parseInt messages-num)

         timestamp        (utils.datetime/timestamp)
         add-chats-fx-fns (get-add-chats-fx-fns
                           {:base-topic   base-topic
                            :base-message base-message
                            :timestamp    timestamp
                            :message-num  message-num-int
                            :total-num    total-num-int})
         transactions     (db-transactions
                           {:base-topic   base-topic
                            :base-message base-message
                            :timestamp    timestamp
                            :message-num  message-num-int
                            :total-num    total-num-int
                            :public-key   public-key})]

     (apply
      fx/merge
      cofx
      {:data-store/tx transactions}
      add-chats-fx-fns))))
