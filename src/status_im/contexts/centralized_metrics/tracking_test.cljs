(ns status-im.contexts.centralized-metrics.tracking-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.utils.build :as build]
    [react-native.platform :as platform]
    [status-im.contexts.centralized-metrics.tracking :as tracking]))

(def platform-os platform/os)
(def app-version build/app-short-version)

(deftest key-value-event-test
  (testing "creates correct key-value event"
    (let [event-name "test-event"
          val-key    :test-key
          value      "test-value"
          expected   {:metric
                      {:eventName  event-name
                       :platform   platform-os
                       :appVersion app-version
                       :eventValue {val-key value}}}]
      (is (= expected (tracking/key-value-event event-name {val-key value}))))))

(deftest user-journey-event-test
  (testing "creates correct user journey event"
    (let [action   "test-action"
          expected {:metric
                    {:eventName  "user-journey"
                     :platform   platform-os
                     :appVersion app-version
                     :eventValue {:action action}}}]
      (is (= expected (tracking/user-journey-event action))))))

(deftest navigation-event-test
  (testing "creates correct navigation event"
    (let [view-id  :test-view-id
          expected {:metric
                    {:eventName  "navigation"
                     :platform   platform-os
                     :appVersion app-version
                     :eventValue {:viewId view-id}}}]
      (is (= expected (tracking/navigation-event view-id))))))

(deftest track-view-id-event-test
  (testing "returns correct navigation event for view-id"
    (is (= [{:metric
             {:eventName  "navigation"
              :platform   platform-os
              :appVersion app-version
              :eventValue {:viewId "communities-stack"}}}]
           (tracking/track-view-id-event :communities-stack)))
    (is (= [{:metric
             {:eventName  "navigation"
              :platform   platform-os
              :appVersion app-version
              :eventValue {:viewId "onboarding.create-profile-password"}}}]
           (tracking/track-view-id-event :screen/onboarding.create-profile-password)))
    (is (= [] (tracking/track-view-id-event :unknown-stack)))))

(deftest tracked-event-test
  (testing "returns correct event for given inputs"
    (is (= {:metric
            {:eventName  "user-journey"
             :platform   platform-os
             :appVersion app-version
             :eventValue {:action tracking/app-started-event}}}
           (tracking/metrics-event [:profile/get-profiles-overview-success])))
    (is (= {:metric
            {:eventName  "events.metrics-enabled"
             :platform   platform-os
             :appVersion app-version
             :eventValue {:enabled true}}}
           (tracking/metrics-event [:centralized-metrics/toggle-centralized-metrics true])))
    (is (= [{:metric
             {:eventName  "navigation"
              :platform   platform-os
              :appVersion app-version
              :eventValue {:viewId "wallet-stack"}}}]
           (tracking/metrics-event [:set-view-id :wallet-stack])))
    (is (nil? (tracking/metrics-event [:unknown-event])))
    (is (= [{:metric
             {:eventName  "navigation"
              :platform   platform-os
              :appVersion app-version
              :eventValue {:viewId "onboarding.intro"}}}]
           (tracking/metrics-event [:set-view-id :screen/onboarding.intro])))))
