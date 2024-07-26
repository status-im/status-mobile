(ns utils.address-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [utils.address]))

(deftest get-shortened-compressed-key-test
  (testing "Ensure the function correctly abbreviates a valid public key"
    (is (= "zQ3...sgt5N"
           (utils.address/get-shortened-compressed-key
            "zQ3ssgRy5TtB47MMiMKMKaGyaawkCgMqqbrnAUYrZJ1sgt5N"))))

  (testing "Ensure the function returns nil when given an empty string"
    (is (nil? (utils.address/get-shortened-compressed-key ""))))

  (testing "Ensure the function returns nil when given a nil input"
    (is (nil? (utils.address/get-shortened-compressed-key nil))))

  (testing "Ensure the function returns nil when given a public key shorter than 9 characters"
    (is (nil? (utils.address/get-shortened-compressed-key "abc")))
    (is (nil? (utils.address/get-shortened-compressed-key "1234")))))


(deftest get-abbreviated-profile-url-test
  (testing "Ensure the function correctly generates an abbreviated profile URL for a valid public key"
    (is (= "status.app/u/abcde...mrdYpzeFUa"
           (utils.address/get-abbreviated-profile-url
            "https://status.app/u/abcdefg#zQ3shPrnUhhR42JJn3QdhodGest8w8MjiH8hPaimrdYpzeFUa"))))

  (testing "Ensure the function returns nil when given an empty public key"
    (is (nil? (utils.address/get-abbreviated-profile-url "status.app/u/abcdefg")))
    (is (nil? (utils.address/get-abbreviated-profile-url "status.app/u/abcdefg#"))))

  (testing "Ensure the function returns nil when given an incorrect base URL"
    (is (nil? (utils.address/get-abbreviated-profile-url
               "https://status.app/uwu/abcdefg#zQ3shPrnUhhR42JJn3QdhodGest8w8MjiH8hPaimrdYpzeFUa"))))

  (testing "Ensure the function returns nil when given a public key shorter than 10 characters"
    (is (nil? (utils.address/get-abbreviated-profile-url "https://status.app/u/abcdefg#012345678"))))

  (testing "Ensure the function returns nil when given nil"
    (is (nil? (utils.address/get-abbreviated-profile-url nil)))))
