(ns utils.security.security-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [native-module.core :as native-module]
    [utils.security.core :as security]))

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
  (testing "an url-encoded javascript link"
    (is (not (security/safe-link? "javascript:/%2F%250dalert(document.domain)"))))
  (testing "rtlo links"
    (is (not (security/safe-link? rtlo-link)))))

(deftest safe-link-text-test-exceptions
  (testing "rtlo links"
    (is (not (security/safe-link-text? rtlo-link-text)))))

(deftest mask-data-test
  (testing "returns an instance of MaskedData"
    (is (instance? security/MaskedData (security/mask-data "test"))))
  (testing "hides the original value"
    (is (= "******" (str (security/mask-data "test")))))
  (testing "succeeds the equality check between same MaskedData instances"
    (is (= (security/mask-data "value") (security/mask-data "value"))))
  (testing "fails the equality check between different MaskedData instances"
    (is (not (= (security/mask-data "value-A") (security/mask-data "value-B")))))
  (testing "fails the equality check with non-MaskedData instances"
    (is (not (= (security/mask-data "value") "value"))))
  (testing "counts the masked data correctly"
    (is (= (count "test") (count (security/mask-data "test")))))
  (testing "unmasks the data correctly"
    (is (= "test" (-> "test" security/mask-data security/safe-unmask-data)))))

(deftest hash-masked-password-test
  (testing "returns an instance of MaskedData with the hashed content"
    (is (= (-> "test" native-module/sha3 security/mask-data)
           (-> "test" security/mask-data security/hash-masked-password))))
  (testing "returns the hashed content if the argument is not a MaskedData instance"
    (is (= (native-module/sha3 "test")
           (-> "test" security/hash-masked-password security/safe-unmask-data)))))
