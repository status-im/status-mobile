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
              :membership-updates [{:chat-id "chat-id"
                                    :from "a"
                                    :signature "b"
                                    :events [{:type "chat-created"
                                              :name "test"
                                              :clock-value 1}
                                             {:type "members-added"
                                              :clock-value 2
                                              :members ["a" "b"]}]}]
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
                       :membershipUpdates #{{:type "chat-created"
                                             :name "test"
                                             :clockValue 1
                                             :id "0xcdf4a63e0c98d0018cf532b3a48350bb80e292cc46249e3f876aaa65eb97a231"
                                             :signature "b"
                                             :from "a"}
                                            {:type "members-added"
                                             :clockValue 2
                                             :members ["a" "b"]
                                             :id "0x1c34d6b4d022c432b7eb6b645e095791af0c6bdb626db7d705e6db0d7cd74b56"
                                             :signature "b"
                                             :from "a"}}
                       :unviewedMessagesCount 2
                       :active true
                       :timestamp 2}]
    (testing "marshaling chat"
      (is (= expected-chat (-> (chats/->rpc chat)
                               (update :members #(into #{} %))
                               (update :membershipUpdates #(into #{} %))))))))

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
              :membershipUpdates [{:type "chat-created"
                                   :name "test"
                                   :clockValue 1
                                   :id "0xcdf4a63e0c98d0018cf532b3a48350bb80e292cc46249e3f876aaa65eb97a231"
                                   :signature "b"
                                   :from "a"}
                                  {:type "members-added"
                                   :clockValue 2
                                   :members ["a" "b"]
                                   :id "0x1c34d6b4d022c432b7eb6b645e095791af0c6bdb626db7d705e6db0d7cd74b56"
                                   :signature "b"
                                   :from "a"}]
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
                       :membership-updates #{{:chat-id "chat-id"
                                              :from "a"
                                              :signature "b"
                                              :events [{:type "chat-created"
                                                        :name "test"
                                                        :clock-value 1}
                                                       {:type "members-added"
                                                        :clock-value 2
                                                        :members #{"a" "b"}}]}}
                       :unviewed-messages-count 2
                       :is-active true
                       :chat-id "chat-id"
                       :timestamp 2}]
    (testing "from-rpc"
      (is (= expected-chat (chats/<-rpc chat))))))

(deftest marshal-membership-updates-test
  (let [raw-updates [{:chat-id "chat-id"
                      :signature "b"
                      :from   "id-1"
                      :events [{:type "chat-created" :clock-value 0 :name "blah"}]}
                     {:chat-id "chat-id"
                      :signature "a"
                      :from   "id-2"
                      :events [{:type "members-added" :clock-value 10 :members [1 2]}
                               {:type "member-removed" :clock-value 11 :member 1}]}]
        expected    #{{:type "members-added" :clockValue 10 :from "id-2" :members [1 2] :signature "a" :id "0xb7690375de21da4890d2d5acca8b56e327d9eb75fd3b4bcceca4bf1679c2f830"}
                      {:type "member-removed" :clockValue 11 :from "id-2" :member 1 :signature "a" :id "0x2a66f195abf6e6903c4245e372e1e2e6aea2b2c0a74ad03080a313e94197a64f"}
                      {:type "chat-created" :clockValue 0 :from "id-1" :name "blah" :signature "b" :id "0x7fad22accf1dec64daedf83e7af19b0dcde8c5facfb479874a48da5fb6967e07"}}
        actual      (into #{} (chats/marshal-membership-updates raw-updates))]
    (is (= expected actual))))
