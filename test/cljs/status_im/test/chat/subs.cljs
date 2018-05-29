(ns status-im.test.chat.subs
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.constants :as const]
            [status-im.chat.subs :as s]))

(deftest message-stream-tests
  (testing "messages with no interspersed datemarks"
    (let [m1 {:from "1"
              :datemark "a"
              :outgoing false}
          m2   {:from "2"
                :datemark "a"
                :outgoing true}
          m3    {:from "2"
                 :datemark "a"
                 :outgoing true}
          dm1  {:type :datemark
                :value "a"}
          messages [m1 m2 m3 dm1]
          [actual-m1
           actual-m2
           actual-m3] (s/messages-stream messages)]
      (testing "it marks only the first message as :last?"
        (is (:last? actual-m1))
        (is (not (:last? actual-m2)))
        (is (not (:last? actual-m3))))
      (testing "it marks the first outgoing message as :last-outgoing?"
        (is (not (:last-outgoing? actual-m1)))
        (is (:last-outgoing? actual-m2))
        (is (not (:last-outgoing? actual-m3))))
      (testing "it marks the message with :same-direction? when from the same author"
        (is (not (:same-direction? actual-m1)))
        (is (not (:same-direction? actual-m2))
            (is (:same-direction? actual-m3))))
      (testing "it marks messages from the same author next to another with :first-in-group?"
        (is (:first-in-group? actual-m1))
        (is (not (:first-in-group? actual-m2)))
        (is (:first-in-group? actual-m3)))
      (testing "it marks messages with display-photo? when they are not outgoing and we are in a group chat"
        (is (:display-photo? actual-m1))
        (is (not (:display-photo? actual-m2)))
        (is (not (:display-photo? actual-m3))))
      (testing "it marks messages with display-username? when we display the photo and are the first in a group"
        (is (:display-username? actual-m1))
        (is (not (:display-username? actual-m2)))
        (is (not (:display-username? actual-m3))))
      (testing "it marks the last message from the same author with :last-in-group?"
        (is (:last-in-group? actual-m1))
        (is (:last-in-group? actual-m2))
        (is (not (:last-in-group? actual-m3))))))
  (testing "messages with interspersed datemarks"
    (let [m1 {:from "2"          ; first & last in group
              :timestamp 63000
              :outgoing true}
          dm1 {:type :datemark
               :value "a"}
          m2  {:from "2"         ; first & last in group as more than 1 minute after previous message
               :timestamp 62000
               :outgoing false}
          m3  {:from "2"         ; last in group
               :timestamp 1
               :outgoing false}
          m4  {:from "2"         ; first in group
               :timestamp 0
               :outgoing false}
          dm2 {:type :datemark
               :value "b"}
          messages [m1 dm1 m2 m3 m4 dm2]
          [actual-m1
           _
           actual-m2
           actual-m3
           actual-m4
           _] (s/messages-stream messages)]
      (testing "it marks the first outgoing message as :last-outgoing?"
        (is (:last-outgoing? actual-m1))
        (is (not (:last-outgoing? actual-m2)))
        (is (not (:last-outgoing? actual-m3)))
        (is (not (:last-outgoing? actual-m4))))
      (testing "it resets :same-direction? after a datemark"
        (is (not (:same-direction? actual-m1)))
        (is (not (:same-direction? actual-m2))
            (is (:same-direction? actual-m3)))
        (is (:same-direction? actual-m4)))
      (testing "it sets :first-in-group? after a datemark"
        (is (:first-in-group? actual-m1))
        (is (:first-in-group? actual-m4)))
      (testing "it sets :first-in-group? if more than 60s have passed since last message"
        (is (:first-in-group? actual-m2)))
      (testing "it sets :last-in-group? after a datemark"
        (is (:last-in-group? actual-m1))
        (is (:last-in-group? actual-m2))
        (is (:last-in-group? actual-m3))
        (is (not (:last-in-group? actual-m4)))))))

(deftest active-chats-test
  (let [active-chat-1 {:is-active true :chat-id 1}
        active-chat-2 {:is-active true :chat-id 2}
        console       {:is-active true :chat-id const/console-chat-id}
        chats         {1 active-chat-1
                       2 active-chat-2
                       3 {:is-active false :chat-id 3}
                       const/console-chat-id console}]
    (testing "in normal it returns only chats with is-active, without console"
      (is (= {1 active-chat-1
              2 active-chat-2}
             (s/active-chats [chats {:dev-mode? false}]))))
    (testing "in dev-mode it returns only chats with is-active, keeping console"
      (is (= {1 active-chat-1
              2 active-chat-2
              const/console-chat-id console}
             (s/active-chats [chats {:dev-mode? true}]))))))
