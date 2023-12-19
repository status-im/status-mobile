(ns legacy.status-im.fleet.core-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.fleet.core :as fleet]
    [status-im2.constants :as constants]))

#_(deftest fleets-test
    (testing "not passing any extra fleet"
      (testing "it returns the default fleets"
        (is (=
             #{:eth.prod
               :eth.staging
               :eth.test}
             (into #{}
                   (keys (node/fleets {})))))))
    (testing "passing a custom fleet"
      (testing "it sets the custom fleet"
        (is (= {:mail    {"a" "a"}
                :whisper {"w" "w"}
                :boot    {"b" "b"}}
               (:custom-fleet
                (node/fleets {:custom-fleets {:custom-fleet
                                              {:mail    {"a" "a"}
                                               :whisper {"w" "w"}
                                               :boot    {"b" "b"}}}})))))))

(deftest set-nodes-test
  (testing "set-nodes"
    (let [actual              (fleet/set-nodes {:db {}} :test-fleet ["a" "b" "c"])
          actual-custom-fleet (get-in actual [:db :custom-fleets])
          actual-mailservers  (get-in actual [:db :mailserver/mailservers :test-fleet])]
      (testing "it sets the custom fleet in the db"
        (is actual-custom-fleet))
      (testing "it sets the custom mailservers in the db"
        (is actual-mailservers))
      (testing "it correctly formats mailservers"
        (is (= {:a {:id       :a
                    :name     "a"
                    :password constants/mailserver-password
                    :address  "a"}
                :b {:id       :b
                    :name     "b"
                    :password constants/mailserver-password
                    :address  "b"}
                :c {:id       :c
                    :name     "c"
                    :password constants/mailserver-password
                    :address  "c"}}
               actual-mailservers))))))
