(ns status-im.test.data-store.chats
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.random :as utils.random]
            [status-im.data-store.chats :as chats]))

(deftest ->to-rpc
  (let [chat {:public? false
              :group-chat true
              :message-list []
              :color "color"
              :contacts #{"a" "b" "c" "d"}
              :last-clock-value 10
              :admins #{"a" "b"}
              :members-joined #{"a" "c"}
              :name "name"
              :membership-update-events :events
              :gaps-loaded? true
              :unviewed-messages-count 2
              :is-active true
              :messages {}
              :pagination-info {}
              :chat-id "chat-id"
              :loaded-unviewed-messages-ids []
              :timestamp 2
              :messages-initialized? true}
        expected-chat {:id "chat-id"
                       :color "color"
                       :name "name"
                       :chatType 3
                       :lastMessage nil
                       :members #{{:id "a"
                                   :admin true
                                   :joined true}
                                  {:id "b"
                                   :admin true
                                   :joined false}
                                  {:id "c"
                                   :admin false
                                   :joined true}
                                  {:id "d"
                                   :admin false
                                   :joined false}}
                       :lastClockValue 10
                       :membershipUpdateEvents :events
                       :unviewedMessagesCount 2
                       :active true
                       :timestamp 2}]
    (testing "marshaling chat"
      (is (= expected-chat (-> (chats/->rpc chat)
                               (update :members #(into #{} %))))))))

(deftest normalize-chat-test
  (let [chat {:id "chat-id"
              :color "color"
              :name "name"
              :chatType 3
              :members [{:id "a"
                         :admin true
                         :joined true}
                        {:id "b"
                         :admin true
                         :joined false}
                        {:id "c"
                         :admin false
                         :joined true}
                        {:id "d"
                         :admin false
                         :joined false}]
              :lastClockValue 10
              :membershipUpdateEvents :events
              :unviewedMessagesCount 2
              :active true
              :timestamp 2}
        expected-chat {:public? false
                       :group-chat true
                       :color "color"
                       :contacts #{"a" "b" "c" "d"}
                       :last-clock-value 10
                       :last-message nil
                       :admins #{"a" "b"}
                       :members-joined #{"a" "c"}
                       :name "name"
                       :membership-update-events :events
                       :unviewed-messages-count 2
                       :is-active true
                       :chat-id "chat-id"
                       :timestamp 2}]
    (testing "from-rpc"
      (is (= expected-chat (chats/<-rpc chat))))))
