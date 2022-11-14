(ns status-im2.subs.communities-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im2.subs.communities :as subs]))

(deftest community->home-item-test
  (testing "has unread messages"
    (is (= {:name                  "name-1"
            :muted?                true
            :unread-messages?      true
            :unread-mentions-count 5
            :community-icon        "icon-1"}
           (subs/community->home-item
            {:name "name-1"
             :muted true
             :images {:thumbnail {:uri "icon-1"}}}
            {:unviewed-messages-count 1
             :unviewed-mentions-count 5}))))
  (testing "no unread messages"
    (is (= {:name                  "name-2"
            :muted?                false
            :unread-messages?      false
            :unread-mentions-count 5
            :community-icon        "icon-2"}
           (subs/community->home-item
            {:name "name-2"
             :muted false
             :images {:thumbnail {:uri "icon-2"}}}
            {:unviewed-messages-count 0
             :unviewed-mentions-count 5})))))

(deftest calculate-unviewed-counts-test
  (let [chats [{:unviewed-messages-count 1
                :unviewed-mentions-count 2}
               {:unviewed-messages-count 3
                :unviewed-mentions-count 0}
               {:unviewed-messages-count 2
                :unviewed-mentions-count 1}]]
    (is (= {:unviewed-messages-count 6
            :unviewed-mentions-count 3}
           (subs/calculate-unviewed-counts chats)))))
