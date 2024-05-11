(ns legacy.status-im.data-store.chats-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.data-store.chats :as chats]))

(deftest normalize-chat-test
  (let [chat          {:id                     "chat-id"
                       :color                  "yellow"
                       :name                   "name"
                       :chatType               3
                       :members                [{:id     "a"
                                                 :admin  true
                                                 :joined true}
                                                {:id     "b"
                                                 :admin  true
                                                 :joined false}
                                                {:id     "c"
                                                 :admin  false
                                                 :joined true}
                                                {:id     "d"
                                                 :admin  false
                                                 :joined false}]
                       :lastClockValue         10
                       :membershipUpdateEvents :events
                       :unviewedMessagesCount  2
                       :timestamp              2}
        expected-chat {:public?                  false
                       :group-chat               true
                       :color                    :yellow
                       :chat-name                "name"
                       :contacts                 #{"a" "b" "c" "d"}
                       :chat-type                3
                       :last-clock-value         10
                       :last-message             nil
                       :admins                   #{"a" "b"}
                       :members-joined           #{"a" "c"}
                       :name                     "name"
                       :membership-update-events :events
                       :unviewed-messages-count  2
                       :chat-id                  "chat-id"
                       :timestamp                2}]
    (testing "from-rpc"
      (is (= expected-chat (chats/<-rpc chat))))))

(deftest decode-chat-id-test
  (let [community-id "0x0322b9b84acb1631d6a31d41d9cf8e1938d352a624bc130de92869e025c7ca79c4"
        channel-id   "02081bc7-20e4-4362-99e2-37669c28cc08"
        chat-id      (str community-id channel-id)]
    (is (= {:community-id community-id
            :channel-id   channel-id}
           (chats/decode-chat-id chat-id)))))
