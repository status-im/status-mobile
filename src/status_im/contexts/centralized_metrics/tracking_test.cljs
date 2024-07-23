(ns status-im.contexts.centralized-metrics.tracking-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.utils.build :as build]
    [react-native.platform :as platform]
    [status-im.contexts.centralized-metrics.tracking :as tracking]))

(deftest user-journey-event-test
  (testing "creates correct metric event"
    (let [action   "some-action"
          expected {:metric {:eventName  "user-journey"
                             :platform   platform/os
                             :appVersion build/app-short-version
                             :eventValue {:action action}}}]
      (is (= expected (tracking/user-journey-event action))))))

(deftest track-view-id-event-test
  (testing "returns correct event for view-id"
    (is (= (tracking/user-journey-event tracking/communities-tab-clicked)
           (tracking/track-view-id-event :communities-stack)))
    (is (= (tracking/user-journey-event tracking/chats-tab-clicked)
           (tracking/track-view-id-event :chats-stack)))
    (is (= (tracking/user-journey-event tracking/wallet-tab-clicked)
           (tracking/track-view-id-event :wallet-stack)))
    (is (nil? (tracking/track-view-id-event :unknown-stack)))))

(deftest tracked-event-test
  (testing "returns correct event for given inputs"
    (is (= (tracking/user-journey-event tracking/navigate-to-create-profile-event)
           (tracking/tracked-event [:onboarding/navigate-to-create-profile])))
    (is (= (tracking/user-journey-event tracking/app-started-event)
           (tracking/tracked-event [:profile/get-profiles-overview-success])))
    (is (= (tracking/track-view-id-event :wallet-stack)
           (tracking/tracked-event [:set-view-id :wallet-stack])))
    (is (nil? (tracking/tracked-event [:unknown-event])))))
