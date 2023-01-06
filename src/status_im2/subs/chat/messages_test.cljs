(ns status-im2.subs.chat.messages-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im2.subs.chat.messages :as messages]
            [status-im2.common.constants :as constants]))

(def messages-state
  [{:message-id "0x111" :album-id "abc" :albumize? true}
   {:message-id "0x222" :album-id "abc" :albumize? true}
   {:message-id "0x333" :album-id "abc" :albumize? true}
   {:message-id "0x444" :album-id "abc" :albumize? true}
   {:message-id "0x555" :album-id "efg" :albumize? true}])

(def messages-albumized-state
  [{:album        [{:message-id "0x444" :album-id "abc" :albumize? true}
                   {:message-id "0x333" :album-id "abc" :albumize? true}
                   {:message-id "0x222" :album-id "abc" :albumize? true}
                   {:message-id "0x111" :album-id "abc" :albumize? true}]
    :album-id     "abc"
    :message-id   "abc"
    :content-type constants/content-type-album}
   {:message-id "0x555" :album-id "efg" :albumize? true}])

(deftest albumize-messages
  (testing "Finding albums in the messages list"
    (is (= (messages/albumize-messages messages-state) messages-albumized-state))))
