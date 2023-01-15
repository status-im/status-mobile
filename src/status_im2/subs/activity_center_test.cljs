(ns status-im2.subs.activity-center-test
  (:require [cljs.test :refer [is testing]]
            [re-frame.db :as rf-db]
            [test-helpers.unit :as h]
            status-im2.subs.activity-center
            [utils.re-frame :as rf]))

(h/deftest-sub :activity-center/filter-status-unread-enabled?
  [sub-name]
  (testing "returns true when filter status is unread"
    (swap! rf-db/app-db assoc-in [:activity-center :filter :status] :unread)
    (is (true? (rf/sub [sub-name]))))

  (testing "returns false when filter status is not unread"
    (swap! rf-db/app-db assoc-in [:activity-center :filter :status] :all)
    (is (false? (rf/sub [sub-name])))))
