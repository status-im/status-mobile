(ns status-im2.contexts.chat.messages.pin.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im2.contexts.chat.messages.pin.events :as events]))

(deftest receive-signal-test
  (let [chat-id      "chat-id"
        message-id-1 "0x1"
        message-id-2 "0x2"
        message-id-3 "0x3"
        message-1    {:id message-id-1 :whisperTimestamp 1 :timestamp 1 :clock 2}
        message-3    {:id message-id-3 :whisperTimestamp 1 :timestamp 1 :clock 2}
        db           {:current-chat-id chat-id
                      :pin-messages    {chat-id {message-id-1 {}
                                                 message-id-2 {}}}}]
    (testing "receiving a pinned messages update"
      (let [pinned-messages-signal (clj->js [{:pinned        true
                                              :localChatId   chat-id
                                              :message_id    message-id-1
                                              :pinnedMessage {:pinnedAt 1
                                                              :pinnedBy "0x1"
                                                              :message  message-1}}
                                             {:pinned      false
                                              :localChatId chat-id
                                              :message_id  message-id-2}
                                             {:pinned        true
                                              :localChatId   chat-id
                                              :message_id    message-id-3
                                              :pinnedMessage {:pinnedAt 1
                                                              :pinnedBy "0x1"
                                                              :message  message-3}}])
            actual                 (events/receive-signal {:db db}
                                                          pinned-messages-signal)
           ]
        (is (not (get-in actual [:db :pin-messages chat-id message-id-2])))
        (is (get-in actual [:db :pin-messages chat-id message-id-1 :message-id]))
        (is (get-in actual [:db :pin-messages chat-id message-id-3 :message-id]))))))
