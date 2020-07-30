(ns status-im.utils.universal-links.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [re-frame.core :as re-frame]
            [status-im.router.core :as router]
            [status-im.utils.universal-links.core :as links]))

(deftest handle-url-test
  (with-redefs [gfycat/generate-gfy (constantly "generated")
                identicon/identicon (constantly "generated")]
    (testing "the user is not logged in"
      (testing "it stores the url for later processing"
        (is (= {:db {:universal-links/url "some-url"}}
               (links/handle-url {:db {}} "some-url")))))
    (testing "the user is logged in"
      (let [db {:multiaccount      {:public-key "pk"}
                :universal-links/url "some-url"}]
        (testing "it clears the url"
          (is (nil? (get-in (links/handle-url {:db db} "some-url")
                            [:db :universal-links/url]))))
        (testing "Handle a custom string"
          (is (= (get-in (links/handle-url {:db db} "https://join.status.im/u/statuse2e")
                         [::router/handle-uri :uri])
                 "https://join.status.im/u/statuse2e")))))))

(deftest url-event-listener
  (testing "the url is not nil"
    (testing "it dispatches the url"
      (let [actual (atom nil)]
        (with-redefs [re-frame/dispatch #(reset! actual %)]
          (links/url-event-listener #js {:url "some-url"})
          (is (= [:handle-universal-link "some-url"] @actual))))))
  (testing "the url is nil"
    (testing "it does not dispatches the url"
      (let [actual (atom nil)]
        (with-redefs [re-frame/dispatch #(reset! actual %)]
          (links/url-event-listener #js {})
          (is (= nil @actual)))))))

(deftest universal-link-test
  (testing "status-im://blah"
    (testing "it returns true"
      (is (links/universal-link? "status-im://blah"))))
  (testing "status-im://blah"
    (testing "it returns true"
      (is (links/deep-link? "status-im://blah"))))
  (testing "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"
    (testing "it returns true"
      (is (links/deep-link? "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7"))))
  (testing "http://join.status.im/blah"
    (testing "it returns true"
      (is (links/universal-link? "http://join.status.im/blah"))))
  (testing "https://join.status.im/blah"
    (testing "it returns true"
      (is (links/universal-link? "https://join.status.im/blah"))))
  (testing "unicode characters"
    (testing "it returns false"
      (is (not (links/universal-link? "https://join.status.im/browse/www.аррӏе.com")))))
  (testing "not-status-im://blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "http://not.status.im/blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "https://not.status.im/blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "http://join.status.im/blah"
    (testing "it returns false"
      (is (not (links/deep-link? "http://join.status.im/blah"))))))
