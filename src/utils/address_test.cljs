(ns utils.address-test
  (:require [cljs.test :refer [deftest is testing]]
            [utils.address]))

(deftest get-shortened-compressed-key
  (testing "Ensure the function correctly abbreviates a valid public key"
    (is (= "zQ3...1sgt5N"
           (utils.address/get-shortened-compressed-key
            "zQ3ssgRy5TtB47MMiMKMKaGyaawkCgMqqbrnAUYrZJ1sgt5N"))))

  (testing "Ensure the function returns nil when given an empty string"
    (is (nil? (utils.address/get-shortened-compressed-key ""))))

  (testing "Ensure the function returns nil when given a nil input"
    (is (nil? (utils.address/get-shortened-compressed-key nil))))

  (testing "Ensure the function returns nil when given a public key shorter than 9 characters"
    (is (nil? (utils.address/get-shortened-compressed-key "abc")))
    (is (nil? (utils.address/get-shortened-compressed-key "1234")))))


(deftest test-get-abbreviated-profile-url
  (testing "Ensure the function correctly generates an abbreviated profile URL for a valid public key"
    (is (= "join.status.im/u/zQ3sh...mrdYpzeFUa"
           (utils.address/get-abbreviated-profile-url
            "join.status.im/u/"
            "zQ3shPrnUhhR42JJn3QdhodGest8w8MjiH8hPaimrdYpzeFUa"))))

  (testing "Ensure the function returns nil when given an empty public key"
    (is (nil? (utils.address/get-abbreviated-profile-url "join.status.im/u/" ""))))

  (testing "Ensure the function returns nil when given a nil public key"
    (is (nil? (utils.address/get-abbreviated-profile-url "join.status.im/u/" nil))))

  (testing "Ensure the function returns nil when given an incorrect base URL"
    (is (nil? (utils.address/get-abbreviated-profile-url
               "join.status.im/uwu/"
               "zQ3shPrnUhhR42JJn3QdhodGest8w8MjiH8hPaimrdYpzeFUa"))))

  (testing "Ensure the function returns nil when given a public key shorter than 17 characters"
    (is (nil? (utils.address/get-abbreviated-profile-url "join.status.im/u/" "abc")))
    (is (nil? (utils.address/get-abbreviated-profile-url "join.status.im/u/" "1234")))))
