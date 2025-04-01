(ns status-im.subs.shell-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.constants :as constants]
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

;; Note - for 1-1 chats, all unread messages are counted as mentions and shown with counter
(def one-to-one-group-community-chats1
  {"0xone-to-one-chat1" {:chat-type               constants/one-to-one-chat-type
                         :unviewed-messages-count 0
                         :unviewed-mentions-count 0}
   "0xgroup-chat1"      {:chat-type               constants/private-group-chat-type
                         :unviewed-messages-count 2
                         :unviewed-mentions-count 0}
   "0xcommunity-chat1"  {:chat-type               constants/community-chat-type
                         :unviewed-messages-count 3
                         :unviewed-mentions-count 0}})

(def expected-notification-data-for-one-to-one-group-community-chats1
  {:screen/communities-stack {:new-notifications?     true
                              :notification-indicator :unread-dot
                              :counter-label          0}
   :screen/chats-stack       {:new-notifications?     true
                              :notification-indicator :unread-dot
                              :counter-label          0}})

(def one-to-one-group-community-chats2
  (merge
   one-to-one-group-community-chats1
   {"0xone-to-one-chat2" {:chat-type               constants/one-to-one-chat-type
                          :unviewed-messages-count 8
                          :unviewed-mentions-count 6}
    "0xgroup-chat2"      {:chat-type               constants/private-group-chat-type
                          :unviewed-messages-count 4
                          :unviewed-mentions-count 3}
    "0xcommunity-chat2"  {:chat-type               constants/community-chat-type
                          :unviewed-messages-count 9
                          :unviewed-mentions-count 7}}))

(def expected-notification-data-for-one-to-one-group-community-chats2
  {:screen/communities-stack {:new-notifications?     true
                              :notification-indicator :counter
                              :counter-label          7}
   :screen/chats-stack       {:new-notifications?     true
                              :notification-indicator :counter
                              :counter-label          11}})

(h/deftest-sub :shell/bottom-tabs-notifications-data
  [sub-name]
  (testing "chats with only unviewed-messages, without unviewed-mentions count should use unread-dot"
    (swap! rf-db/app-db assoc :chats one-to-one-group-community-chats1)
    (is (= (rf/sub [sub-name]) expected-notification-data-for-one-to-one-group-community-chats1)))

  (testing
    "chats with both unviewed-messages and unviewed-mentions count should use counter with mentions count"
    (swap! rf-db/app-db assoc :chats one-to-one-group-community-chats2)
    (is (= (rf/sub [sub-name]) expected-notification-data-for-one-to-one-group-community-chats2))))
