(ns status-im.test.utils.security
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.security :as security]))

(def rtlo-link "‮http://google.com")
(def rtlo-link-text "blah blah ‮  some other blah blah http://google.com blah bash")

(deftest safe-link-test-happy-path
  (testing "an http link"
    (is (security/safe-link? "http://test.com")))
  (testing "an https link"
    (is (security/safe-link? "https://test.com")))
  (testing "a link without a a protocol"
    (is (security/safe-link? "test.com"))))

(deftest safe-link-test-exceptions
  (testing "a javascript link"
    (is (not (security/safe-link? "javascript://anything"))))
  (testing "a javascript link mixed cases"
    (is (not (security/safe-link? "JaVasCrIpt://anything"))))
  (testing "a javascript link upper cases"
    (is (not (security/safe-link? "JAVASCRIPT://anything"))))
  (testing "rtlo links"
    (is (not (security/safe-link? rtlo-link)))))

(deftest safe-link-text-test-exceptions
  (testing "rtlo links"
    (is (not (security/safe-link-text? rtlo-link-text)))))
