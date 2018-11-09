(ns status-im.test.search.core
  (:require [cljs.test :refer-macros [deftest testing is]]
            [status-im.search.subs :as search.subs]))

(deftest filter-chats
  (let [chats [{:name "name1"
                :random-name "random-name1"
                :tags #{"tag1"}}
               {:name "name2"
                :random-name "random-name2"
                :tags #{"tag2" "tag3"}}
               {:name "name3"
                :random-name "random-name3"
                :tags #{}}
               {:name "name4"
                :random-name "random-name4"
                :tags #{"tag4"}}]]
    (testing "no search filter"
      (is (= 4 (count (search.subs/filter-chats [chats ""])))))
    (testing "searching for a specific tag"
      (is (= 1 (count (search.subs/filter-chats [chats "tag2"])))))
    (testing "searching for a partial tag"
      (is (= 3 (count (search.subs/filter-chats [chats "tag"])))))
    (testing "searching for a specific random-name"
      (is (= 1 (count (search.subs/filter-chats [chats "random-name1"])))))
    (testing "searching for a partial random-name"
      (is (= 4 (count (search.subs/filter-chats [chats "random-name"])))))
    (testing "searching for a specific chat name"
      (is (= 1 (count (search.subs/filter-chats [chats "name4"])))))))
