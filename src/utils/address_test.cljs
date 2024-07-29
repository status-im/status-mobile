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

(def valid-metamask-addresses
  ["ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa4b1"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"])

(def invalid-metamask-addresses
  ["ethe:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1"
   ":0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa4b1"
   "0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1d"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd20xa4b1"
   "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2:0xa"])

(def metamask-to-status
  [{:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1"
    :status   "eth:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa4b1"
    :status   "arb1:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa"
    :status   "oeth:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"
    :status   "0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2"}
   {:metamask "ethe:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1" :status nil}
   {:metamask ":0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa4b1" :status nil}
   {:metamask "0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0xa" :status nil}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2@0x1d" :status nil}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd20xa4b1" :status nil}
   {:metamask "ethereum:0x38cf6E0Ba4C4530735616e1Ee7ff5FbCB726fBd2:0xa" :status nil}])

(deftest metamask-address?-test
  (testing "Check valid metamask addresses"
    (dorun
     (for [address valid-metamask-addresses]
       (is (utils.address/metamask-address? address)))))
  (testing "Check invalid metamask addresses"
    (dorun
     (for [address invalid-metamask-addresses]
       (is (not (utils.address/metamask-address? address)))))))

(deftest metamask-address->status-address-test
  (testing "Check metamask to status address conversion is valid"
    (dorun
     (for [{metamask-address :metamask
            status-address   :status} metamask-to-status]
       (is (= status-address (utils.address/metamask-address->status-address metamask-address)))))))
