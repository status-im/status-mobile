(ns status-im.search.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [status-im.subs :as search.subs]))

(deftest filter-chats
  (let [chats {:chat-1 {:name "name1"
                        :alias "alias1"
                        :tags #{"tag1"}}
               :chat-2 {:name "name2"
                        :alias "alias2"
                        :tags #{"tag2" "tag3"}}
               :chat-3 {:name "name3"
                        :alias "alias3"
                        :tags #{}}
               :chat-4 {:name "name4"
                        :alias "alias4"
                        :tags #{"tag4"}}}]
    (testing "no search filter"
      (is (= (count chats)
             (count (search.subs/apply-filter ""
                                              chats
                                              search.subs/extract-chat-attributes
                                              false)))))
    (testing "searching for a specific tag"
      (is (= 1
             (count (search.subs/apply-filter "tag2"
                                              chats
                                              search.subs/extract-chat-attributes
                                              false)))))
    (testing "searching for a partial tag"
      (is (= 3
             (count (search.subs/apply-filter "tag"
                                              chats
                                              search.subs/extract-chat-attributes
                                              false)))))
    (testing "searching for a specific alias"
      (is (= 1
             (count (search.subs/apply-filter "alias4"
                                              chats
                                              search.subs/extract-chat-attributes
                                              false)))))
    (testing "searching for a partial alias"
      (is (= 4
             (count (search.subs/apply-filter "alias"
                                              chats
                                              search.subs/extract-chat-attributes
                                              false)))))
    (testing "searching for a specific chat name"
      (is (= 1
             (count (search.subs/apply-filter "name4"
                                              chats
                                              search.subs/extract-chat-attributes
                                              false)))))))
