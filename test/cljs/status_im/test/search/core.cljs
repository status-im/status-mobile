(ns status-im.test.search.core
  (:require [cljs.test :refer-macros [deftest testing is]]
            [status-im.search.subs :as search.subs]))

(deftest filter-chats
  (let [chats {:chat-1 {:name "name1"
                        :random-name "random-name1"
                        :tags #{"tag1"}}
               :chat-2 {:name "name2"
                        :random-name "random-name2"
                        :tags #{"tag2" "tag3"}}
               :chat-3 {:name "name3"
                        :random-name "random-name3"
                        :tags #{}}
               :chat-4 {:name "name4"
                        :random-name "random-name4"
                        :tags #{"tag4"}}}]
    (testing "no search filter"
      (is (= 0
             (count (search.subs/apply-filter ""
                                              chats
                                              search.subs/extract-chat-attributes)))))
    (testing "searching for a specific tag"
      (is (= 1
             (count (search.subs/apply-filter "tag2"
                                              chats
                                              search.subs/extract-chat-attributes)))))
    (testing "searching for a partial tag"
      (is (= 3
             (count (search.subs/apply-filter "tag"
                                              chats
                                              search.subs/extract-chat-attributes)))))
    (testing "searching for a specific random-name"
      (is (= 1
             (count (search.subs/apply-filter "random-name1"
                                              chats
                                              search.subs/extract-chat-attributes)))))
    (testing "searching for a partial random-name"
      (is (= 4
             (count (search.subs/apply-filter "random-name"
                                              chats
                                              search.subs/extract-chat-attributes)))))
    (testing "searching for a specific chat name"
      (is (= 1
             (count (search.subs/apply-filter "name4"
                                              chats
                                              search.subs/extract-chat-attributes)))))))
