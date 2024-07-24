(ns status-im.contexts.centralized-metrics.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.centralized-metrics.events :as events]
    [status-im.contexts.centralized-metrics.tracking :as tracking]
    [test-helpers.unit :as h]))

(deftest show-confirmation-modal-test
  (testing "returns true if the user confirmed"
    (is (false? (events/show-confirmation-modal? {events/user-confirmed-key true})))
    (is (true? (events/show-confirmation-modal? {})))))

(deftest push-event-test
  (testing "returns correct boolean value"
    (is (true? (events/push-event? {:centralized-metrics/user-confirmed? false})))
    (is (true? (events/push-event? {:centralized-metrics/enabled? true})))
    (is (false? (events/push-event? {:centralized-metrics/user-confirmed? true
                                     :centralized-metrics/enabled?        false})))))

(deftest centralized-metrics-interceptor-test
  (testing "processes context correctly"
    (with-redefs [tracking/tracked-event (fn [_] {:metric "mocked-event"})
                  events/push-event?     (fn [_] true)]
      (let [context {:coeffects {:event [:some-event]
                                 :db    {:centralized-metrics/enabled? true}}}]
        (is (= context (events/centralized-metrics-interceptor context)))))))

(h/deftest-event :centralized-metrics/toggle-centralized-metrics
  [event-id dispatch]
  (testing "toggling value to true"
    (let [enabled?     true
          expected-fxs {:db {:centralized-metrics/user-confirmed? true
                             :centralized-metrics/enabled?        enabled?}
                        :fx [[:effects.centralized-metrics/toggle-metrics enabled?]]}]
      (is (match? expected-fxs (dispatch [event-id enabled?])))))
  (testing "toggling value to false"
    (let [enabled?     false
          expected-fxs {:db {:centralized-metrics/user-confirmed? true
                             :centralized-metrics/enabled?        enabled?}
                        :fx [[:effects.centralized-metrics/toggle-metrics enabled?]]}]
      (is (match? expected-fxs (dispatch [event-id enabled?]))))))
