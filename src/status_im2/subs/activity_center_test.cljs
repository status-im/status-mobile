(ns status-im2.subs.activity-center-test
  (:require [cljs.test :refer [is testing]]
            [re-frame.db :as rf-db]
            [status-im2.contexts.activity-center.notification-types :as types]
            status-im2.subs.activity-center
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

(h/deftest-sub :activity-center/filter-status-unread-enabled?
  [sub-name]
  (testing "returns true when filter status is unread"
    (swap! rf-db/app-db assoc-in [:activity-center :filter :status] :unread)
    (is (true? (rf/sub [sub-name]))))

  (testing "returns false when filter status is not unread"
    (swap! rf-db/app-db assoc-in [:activity-center :filter :status] :all)
    (is (false? (rf/sub [sub-name])))))

(h/deftest-sub :activity-center/notification-types-with-unread
  [sub-name]
  (testing "returns an empty set when no types have unread notifications"
    (swap! rf-db/app-db assoc-in
      [:activity-center :notifications]
      {types/system  {:all {:data [{:id "0x1" :read true}]}}
       types/mention {:all {:data [{:id "0x2" :read true}]}}})

    (is (= #{}
           (rf/sub [sub-name]))))

  (testing "ignores the 'no-type'"
    (swap! rf-db/app-db assoc-in
      [:activity-center :notifications]
      {types/no-type {:all {:data [{:id "0x1" :read false}
                                   {:id "0x2" :read true}]}}})

    (is (= #{}
           (rf/sub [sub-name]))))

  (testing "returns a set with all types containing unread notifications"
    (swap! rf-db/app-db assoc-in
      [:activity-center :notifications]
      {types/reply   {:all    {:data []}
                      :unread {:data []}}
       types/system  {:all    {:data [{:id "0x1" :read true}
                                      {:id "0x2" :read true}
                                      {:id "0x3" :read false}]}
                      :unread {:data [{:id "0x3" :read false}]}}
       types/mention {:all    {:data [{:id "0x4" :read false}]}
                      :unread {:data [{:id "0x5" :read false}]}}})

    (let [actual (rf/sub [sub-name])]
      (is (= #{types/system types/mention}
             actual))
      (is (set? actual)))))
