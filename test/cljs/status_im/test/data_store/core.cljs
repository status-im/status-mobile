(ns status-im.test.data-store.core
  (:require [cljs.test :refer-macros [deftest is testing use-fixtures]]
            [status-im.utils.utils :as utils]
            [clojure.string :as string]
            [status-im.data-store.core :as core]))

(deftest merge-events-of-type
  (def events-test [[:event1 [:data1]] [:event1 [:data2]]
                    [:event2 [:data3]] [:event2 [:data4]]
                    [:event3 [:data5]]
                    [:event4 :data6]
                    [:event5]])
  (testing "merging-events-with-data"
    (is (= (core/merge-events-of-type events-test :event1)
           [[:event1 [:data1 :data2]]
            [:event2 [:data3]] [:event2 [:data4]]
            [:event3 [:data5]]
            [:event4 :data6]
            [:event5]]))

    (is (= (core/merge-events-of-type events-test :event2)
           [[:event2 [:data3 :data4]]
            [:event1 [:data1]] [:event1 [:data2]]
            [:event3 [:data5]]
            [:event4 :data6]
            [:event5]]))

    (is (= (core/merge-events-of-type events-test :event3)
           [[:event3 [:data5]]
            [:event1 [:data1]] [:event1 [:data2]]
            [:event2 [:data3]] [:event2 [:data4]]
            [:event4 :data6]
            [:event5]]))

    ;; we can't group non-vector event data
    (is (= (core/merge-events-of-type events-test :event4)
           [[:event1 [:data1]] [:event1 [:data2]]
            [:event2 [:data3]] [:event2 [:data4]]
            [:event3 [:data5]]
            [:event4 :data6]
            [:event5]]))

    ;; we can't group non-vector event data
    (is (= (core/merge-events-of-type events-test :event5)
           [[:event1 [:data1]] [:event1 [:data2]]
            [:event2 [:data3]] [:event2 [:data4]]
            [:event3 [:data5]]
            [:event4 :data6]
            [:event5]]))))
