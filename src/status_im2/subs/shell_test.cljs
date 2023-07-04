(ns status-im2.subs.shell-test
  (:require [utils.re-frame :as rf]
            [re-frame.db :as rf-db]
            [test-helpers.unit :as h]
            [cljs.test :refer [is testing]]
            [status-im2.constants :as constants]))

(def expected-notification-data-for-public-profile-timeline-chats
  {:communities-stack {:new-notifications?     false
                       :notification-indicator :unread-dot
                       :counter-label          0}
   :chats-stack       {:new-notifications?     false
                       :notification-indicator :unread-dot
                       :counter-label          0}})

(def one-to-one-group-community-chats1
  {"0xone-to-one-chat1" {:chat-type               constants/one-to-one-chat-type
                         :unviewed-messages-count 5
                         :unviewed-mentions-count 0}
   "0xgroup-chat1"      {:chat-type               constants/private-group-chat-type
                         :unviewed-messages-count 2
                         :unviewed-mentions-count 0}
   "0xcommunity-chat1"  {:chat-type               constants/community-chat-type
                         :unviewed-messages-count 3
                         :unviewed-mentions-count 0}})

(def expected-notification-data-for-one-to-one-group-community-chats1
  {:communities-stack {:new-notifications?     true
                       :notification-indicator :unread-dot
                       :counter-label          0}
   :chats-stack       {:new-notifications?     true
                       :notification-indicator :counter
                       :counter-label          7}})

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
  {:communities-stack {:new-notifications?     true
                       :notification-indicator :counter
                       :counter-label          7}
   :chats-stack       {:new-notifications?     true
                       :notification-indicator :counter
                       :counter-label          19}})

(h/deftest-sub :shell/bottom-tabs-notifications-data
  [sub-name]
  (testing "chats with only unviewed-messages, without unviewed-mentions count should use unread-dot"
    (swap! rf-db/app-db assoc :chats one-to-one-group-community-chats1)
    (is (= (rf/sub [sub-name]) expected-notification-data-for-one-to-one-group-community-chats1)))

  (testing
    "chats with both unviewed-messages and unviewed-mentions count should use counter with mentions count"
    (swap! rf-db/app-db assoc :chats one-to-one-group-community-chats2)
    (is (= (rf/sub [sub-name]) expected-notification-data-for-one-to-one-group-community-chats2))))
