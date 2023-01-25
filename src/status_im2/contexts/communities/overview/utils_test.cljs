(ns status-im2.contexts.communities.overview.utils-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im2.contexts.communities.overview.utils :as u]))

(deftest open-activity-center-test
  (testing "One user"
    (let [users [{:full-name "Alicia K"}]]

      (is (= "Join Alicia"
             (u/join-existing-users-string users)))))
  (testing "Two users"
    (let [users [{:full-name "Alicia K"}
                 {:full-name "Marcus C"}]]
      (is (= "Join Alicia and Marcus"
             (u/join-existing-users-string users)))))
  (testing "Three users"
    (let [users [{:full-name "Alicia K"}
                 {:full-name "Marcus C"}
                 {:full-name "MNO PQR"}]]
      (is (= "Join Alicia, Marcus and 1 more"
             (u/join-existing-users-string users)))))
  (testing "Four users"
    (let [users [{:full-name "Alicia K"}
                 {:full-name "Marcus C"}
                 {:full-name "MNO PQR"}
                 {:full-name "STU VWX"}]]

      (is (= "Join Alicia, Marcus and 2 more"
             (u/join-existing-users-string users))))))
