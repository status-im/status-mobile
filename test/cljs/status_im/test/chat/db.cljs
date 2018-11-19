(ns status-im.test.chat.db
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.db :as chat.db]))

(deftest chat-name
  (testing "it prepends # if it's a public chat"
    (is (= "#withhash" (chat.db/chat-name {:group-chat true
                                           :chat-id "1"
                                           :public? true
                                           :name "withhash"} nil))))
  (testing "it leaves the name unchanged if it's a group chat"
    (is (= "unchanged" (chat.db/chat-name {:group-chat true
                                           :chat-id "1"
                                           :name "unchanged"} nil))))
  (testing "it pulls the name from contact if it's a one-to-one"
    (is (= "this-one" (chat.db/chat-name {:chat-id "1"
                                          :name "not-this-one"} {:name "this-one"}))))
  (testing "it generates the name from chat id if no contact"
    (is (= "Blond Cooperative Coelacanth" (chat.db/chat-name {:chat-id "1"
                                                              :name "not-this-one"} nil)))))
;;TODO write tests for message ordering

(deftest active-chats-test
  (let [active-chat-1 {:is-active true :chat-id "1"}
        active-chat-2 {:is-active true :chat-id "2"}
        chats         {"1" active-chat-1
                       "2" active-chat-2
                       "3" {:is-active false :chat-id "3"}}]
    (testing "it returns only chats with is-active"
      (is (= ["1" "2"]
             (keys (chat.db/active-chats chats {} {} "mainnet")))))))
