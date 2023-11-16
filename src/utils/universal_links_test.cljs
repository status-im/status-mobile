(ns utils.universal-links-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [utils.universal-links :as links]))

(deftest universal-link-test
  (testing "status-app://blah"
    (testing "it returns true"
      (is (links/universal-link? "status-app://blah"))))
  (testing "status-app://blah"
    (testing "it returns true"
      (is (links/deep-link? "status-app://blah"))))
  (testing "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
    (testing "it returns true"
      (is (links/deep-link? "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"))))
  (testing "http://status.app/blah"
    (testing "it returns true"
      (is (links/universal-link? "http://status.app/blah"))))
  (testing "https://status.app/blah"
    (testing "it returns true"
      (is (links/universal-link? "https://status.app/blah"))))
  (testing "unicode characters"
    (testing "it returns false"
      (is (not (links/universal-link? "https://status.app/browse/www.аррӏе.com")))))
  (testing "not-status-app://blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "http://not.status.im/blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "https://not.status.im/blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "http://status.app/blah"
    (testing "it returns false"
      (is (not (links/deep-link? "http://status.app/blah"))))))
