(ns status-im2.subs.chat.messages-test
  (:require [cljs.test :refer [deftest is testing]]
            [status-im2.subs.chat.messages :as messages]
            [status-im2.constants :as constants]))

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

(deftest intersperse-datemarks
  (testing "it mantains the order even when timestamps are across days"
    (let [message-1           {:datemark          "Dec 31, 1999"
                               :whisper-timestamp 946641600000} ; 1999}
          message-2           {:datemark          "Jan 1, 2000"
                               :whisper-timestamp 946728000000} ; 2000 this will displayed in 1999
          message-3           {:datemark          "Dec 31, 1999"
                               :whisper-timestamp 946641600000} ; 1999
          message-4           {:datemark          "Jan 1, 2000"
                               :whisper-timestamp 946728000000} ; 2000
          ordered-messages    [message-4
                               message-3
                               message-2
                               message-1]
          [m1 d1 m2 m3 m4 d2] (messages/add-datemarks ordered-messages)]
      (is (= "Jan 1, 2000"
             (:datemark m1)))
      (is (= {:type  :datemark
              :value "Jan 1, 2000"}
             d1))
      (is (= "Dec 31, 1999"
             (:datemark m2)
             (:datemark m3)
             (:datemark m4)))
      (is (= {:type  :datemark
              :value "Dec 31, 1999"}
             d2)))))
