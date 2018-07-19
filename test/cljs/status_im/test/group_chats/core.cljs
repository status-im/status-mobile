(ns status-im.test.group-chats.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.config :as config]
            [status-im.group-chats.core :as group-chats]))

(def chat-id "0xba")
(def chat-name "chat-name")

(def member-1 "member-1")
(def member-2 "member-2")

(def admin member-1)

(def invitation-m1 {:id "m-1"
                    :user member-1})
(def invitation-m2 {:id "m-2"
                    :user member-2})

(def initial-message {:chat-id chat-id
                      :chat-name chat-name
                      :admin admin
                      :participants [invitation-m1
                                     invitation-m2]
                      :leaves []
                      :signature "some"
                      :version 1})

(deftest handle-group-membership-update
  (with-redefs [config/group-chats-enabled? true]
    (testing "a brand new chat"
      (let [actual   (->
                      (group-chats/handle-membership-update {:db {}} initial-message admin)
                      :db
                      :chats
                      (get chat-id))]
        (testing "it creates a new chat"
          (is actual))
        (testing "it sets the right participants"
          (is (= [invitation-m1
                  invitation-m2]
                 (:contacts actual))))
        (testing "it sets the right version"
          (is (= 1
                 (:membership-version actual))))))
    (testing "an already existing chat"
      (let [cofx {:db {:chats {chat-id {:contacts [invitation-m1
                                                   invitation-m2]
                                        :group-admin admin
                                        :membership-version 2}}}}]
        (testing "an update from the admin is received"
          (testing "the message is an older version"
            (let [actual (group-chats/handle-membership-update cofx initial-message admin)]
              (testing "it noops"
                (is (not actual)))))
          (testing "the message is a more recent version"
            (testing "it sets the right participants")))
        (testing "a leave from a member is received"
          (testing "the user is removed"))))))
