(ns status-im.test.data-store.messages
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.data-store.messages :as m]))

(def message-id "0xfe96d03da2159e632a6653d04028b0de8b55f78f03521b26ce10dc5f48a16aee")
(def chat-id "chat-id")
(def from "0x0424a68f89ba5fcd5e0640c1e1f591d561fa4125ca4e2a43592bc4123eca10ce064e522c254bb83079ba404327f6eafc01ec90a1444331fe769d3f3a7f90b0dde1")

(deftest message->rpc
  (testing "message to rpc"
    (let [message  {:message-id message-id
                    :content {:chat-id chat-id
                              :response-to-v2 "id-2"
                              :text "hta"}
                    :whisper-timestamp 1
                    :js-obj {}
                    :dedup-id "ATIwMTkwODE0YTdkNWZhZGY1N2E0ZDU3MzUxZmJkNDZkZGM1ZTU4ZjRlYzUyYWYyMDA5NTc2NWYyYmIxOTQ2OTM3NGUwNjdiMvEpTIGEjHOTAyqsrN39wST4npnSAv1AR8jJWeubanjkoGIyJooD5RVRnx6ZMt+/JzBOD2hoZzlHQWA0bC6XbdU="
                    :outgoing-status :sending
                    :message-type :public-group-user-message
                    :clock-value 2
                    :from from
                    :chat-id chat-id
                    :content-type "text/plain"
                    :timestamp 3}
          expected {:id message-id
                    :whisperTimestamp 1
                    :from from
                    :chatId chat-id
                    :replyTo "id-2"
                    :content "{\"chat-id\":\"chat-id\",\"response-to-v2\":\"id-2\",\"text\":\"hta\"}"
                    :contentType "text/plain"
                    :messageType "public-group-user-message"
                    :clockValue 2
                    :timestamp 3
                    :outgoingStatus "sending"}]
      (is (= expected (m/->rpc message))))))

(deftest message<-rpc
  (testing "message to rpc"
    (let [expected  {:message-id message-id
                     :content {:chat-id chat-id
                               :text "hta"}
                     :whisper-timestamp 1
                     :outgoing-status :sending
                     :outgoing :sending
                     :message-type :public-group-user-message
                     :clock-value 2
                     :from from
                     :chat-id chat-id
                     :quoted-message {:from "from"
                                      :text "reply"}
                     :content-type "text/plain"
                     :timestamp 3}
          message {:id message-id
                   :whisperTimestamp 1
                   :from from
                   :chatId chat-id
                   :content "{\"chat-id\":\"chat-id\",\"text\":\"hta\"}"
                   :contentType "text/plain"
                   :messageType "public-group-user-message"
                   :clockValue 2
                   :quotedMessage {:from "from"
                                   :content "{\"chat-id\":\"chat-id\",\"text\":\"reply\"}"}
                   :timestamp 3
                   :outgoingStatus "sending"}]
      (is (= expected (m/<-rpc message))))))
