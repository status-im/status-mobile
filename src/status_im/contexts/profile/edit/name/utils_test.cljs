(ns status-im.contexts.profile.edit.name.utils-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.profile.edit.name.utils :as utils]
    [utils.i18n :as i18n]))

(deftest has-emojis-test
  (testing "it returns true when string has emojis"
    (is (utils/has-emojis "Hello 😊")))
  (testing "it returns false when string has no emojis"
    (is (not (utils/has-emojis "Hello")))))

(deftest has-common-names-test
  (testing "it returns true when string contains common names"
    (is (utils/has-common-names "Ethereum")))
  (testing "it returns false when string does not contain common names"
    (is (not (utils/has-common-names "Hello")))))

(deftest has-special-characters-test
  (testing "it returns true when string has special characters"
    (is (utils/has-special-characters "@name")))
  (testing "it returns false when string has no special characters"
    (is (not (utils/has-special-characters "name")))))

(deftest name-too-short-test
  (testing "it returns true when name is too short"
    (is (utils/name-too-short "abc")))
  (testing "it returns false when name is not too short"
    (is (not (utils/name-too-short "abcdef")))))

(deftest name-too-long-test
  (testing "it returns true when name is too long"
    (is (utils/name-too-long (apply str (repeat 25 "a")))))
  (testing "it returns false when name is not too long"
    (is (not (utils/name-too-long "abcdef")))))

(deftest validation-name-test
  (testing "it returns nil when name is nil or empty"
    (is (nil? (utils/validation-name nil)))
    (is (nil? (utils/validation-name ""))))
  (testing "it returns error when name has special characters"
    (is (= (i18n/label :t/are-not-allowed {:check (i18n/label :t/special-characters)})
           (utils/validation-name "@name"))))
  (testing "it returns error when name ends with -eth"
    (is (= (i18n/label :t/ending-not-allowed {:ending "-eth"}) (utils/validation-name "name-eth"))))
  (testing "it returns error when name ends with _eth"
    (is (= (i18n/label :t/ending-not-allowed {:ending "_eth"}) (utils/validation-name "name_eth"))))
  (testing "it returns error when name ends with .eth"
    (is (= (i18n/label :t/ending-not-allowed {:ending ".eth"}) (utils/validation-name "name.eth"))))
  (testing "it returns error when name starts with space"
    (is (= (i18n/label :t/start-with-space) (utils/validation-name " name"))))
  (testing "it returns error when name ends with space"
    (is (= (i18n/label :t/ends-with-space) (utils/validation-name "name "))))
  (testing "it returns error when name contains common names"
    (is (= (i18n/label :t/are-not-allowed {:check (i18n/label :t/common-names)})
           (utils/validation-name "Ethereum"))))
  (testing "it returns error when name contains emojis"
    (is (= (i18n/label :t/are-not-allowed {:check (i18n/label :t/emojis)})
           (utils/validation-name "Hello 😊"))))
  (testing "it returns error when name is too short"
    (is (= (i18n/label :t/minimum-characters {:min-chars 5}) (utils/validation-name "name"))))
  (testing "it returns error when name is too long"
    (is (= (i18n/label :t/profile-name-is-too-long)
           (utils/validation-name (apply str (repeat 25 "a")))))))
