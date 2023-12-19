(ns legacy.status-im.keycard.common-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.keycard.common :as common]))

(deftest test-show-connection-sheet
  (testing "the card is not connected yet"
    (let [db  {:keycard {:card-connected? false}}
          res (common/show-connection-sheet-component
               {:db db}
               {:on-card-connected :do-something
                :handler           (fn [{:keys [db]}]
                                     {:db (assoc db :some-key :some-value)})})]
      (is (= :do-something
             (get-in res [:db :keycard :on-card-connected])))
      (is (nil? (get-in res [:db :some-key])))
      (is (true? (get-in res [:db :bottom-sheet/show?])))))
  (testing "the card is connected before the interaction"
    (let [db  {:keycard {:card-connected? true}}
          res (common/show-connection-sheet-component
               {:db db}
               {:on-card-connected :do-something
                :handler           (fn [{:keys [db]}]
                                     {:db (assoc db :some-key :some-value)})})]
      (is (nil? (get-in res [:db :keycard :on-card-connected])))
      (is (= :do-something
             (get-in res [:db :keycard :last-on-card-connected])))
      (is (= :some-value (get-in res [:db :some-key])))
      (is (true? (get-in res [:db :bottom-sheet/show?])))))
  (testing "on-card-connected is not specified"
    (is
     (thrown?
      js/Error
      (common/show-connection-sheet-component
       {:db {}}
       {:handler (fn [_])}))))
  (testing "handler is not specified"
    (is
     (thrown?
      js/Error
      (common/show-connection-sheet-component
       {:db {}}
       {:on-card-connected :do-something})))))
