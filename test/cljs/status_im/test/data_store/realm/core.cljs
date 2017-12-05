(ns status-im.test.data-store.realm.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.data-store.realm.core :as core]))

(deftest serialization
  (is (nil? (core/deserialize "")))
  (is (nil? (core/deserialize "giberrish")))
  (is (nil? (core/deserialize nil)))
  (is (nil? (core/deserialize (core/serialize nil)))))

(deftest transit-preparation
  (testing "Check if leading Transit special characters are properly escaped with tildes"
    (let [data          {:to-be-escaped1 "~bad string"
                         :to-be-escaped2 "^another bad string"
                         :to-be-escaped3 "`and another bad string"
                         :no-escaping    "no escaping"
                         :vector-content ["a" "b" "c"]}
          prepared-data (core/prepare-for-transit data)]
      (is (= "~~bad string" (:to-be-escaped1 prepared-data)))
      (is (= "~^another bad string" (:to-be-escaped2 prepared-data)))
      (is (= "~`and another bad string" (:to-be-escaped3 prepared-data)))
      (is (= "no escaping" (:no-escaping prepared-data)))
      (is (= data (-> prepared-data
                      clj->js
                      core/internal-convert))))))
