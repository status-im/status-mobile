(ns status-im.test.data-store.chats
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.random :as utils.random]
            [status-im.data-store.chats :as chats]))

(deftest normalize-chat-test
  (testing "admins & contacts"
    (with-redefs [chats/get-last-clock-value (constantly 42)]
      (is (= {:last-clock-value 42
              :admins #{4}
              :contacts #{2}
              :tags #{}
              :membership-updates []
              :last-message-type :message-type
              :last-message-content {:foo "bar"}}
             (chats/normalize-chat
              {:admins            [4]
               :contacts          [2]
               :last-message-type "message-type"
               :last-message-content "{:foo \"bar\"}"})))))
  (testing "membership-updates"
    (with-redefs [chats/get-last-clock-value (constantly 42)]
      (let [raw-events {"1" {:id "1" :type "members-added" :clock-value 10 :members [1 2] :signature "a" :from "id-1"}
                        "2" {:id "2" :type "member-removed" :clock-value 11 :member 1 :signature "a" :from "id-1"}
                        "3" {:id "3" :type "chat-created" :clock-value 0 :name "blah" :signature "b" :from "id-2"}}
            expected    #{{:chat-id "chat-id"
                           :from "id-2"
                           :signature "b"
                           :events [{:type "chat-created" :clock-value 0 :name "blah"}]}
                          {:chat-id "chat-id"
                           :signature "a"
                           :from "id-1"
                           :events [{:type "members-added" :clock-value 10 :members [1 2]}
                                    {:type "member-removed" :clock-value 11 :member 1}]}}
            actual      (->> (chats/normalize-chat {:chat-id "chat-id"
                                                    :membership-updates raw-events})
                             :membership-updates
                             (into #{}))]
        (is (= expected
               actual))))))

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
        expected    #{{:type "members-added" :clock-value 10 :from "id-2" :members [1 2] :signature "a" :id "0xb7690375de21da4890d2d5acca8b56e327d9eb75fd3b4bcceca4bf1679c2f830"}
                      {:type "member-removed" :clock-value 11 :from "id-2" :member 1 :signature "a" :id "0x2a66f195abf6e6903c4245e372e1e2e6aea2b2c0a74ad03080a313e94197a64f"}
                      {:type "chat-created" :clock-value 0 :from "id-1" :name "blah" :signature "b" :id "0x7fad22accf1dec64daedf83e7af19b0dcde8c5facfb479874a48da5fb6967e07"}}
        actual      (into #{} (chats/marshal-membership-updates raw-updates))]
    (is (= expected actual))))
