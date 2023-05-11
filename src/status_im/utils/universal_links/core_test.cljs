(ns status-im.utils.universal-links.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [re-frame.core :as re-frame]
            [status-im.router.core :as router]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.universal-links.core :as links]))

(deftest handle-url-test
  (with-redefs [gfycat/generate-gfy (constantly "generated")]
    (testing "the user is not logged in"
      (testing "it stores the url for later processing"
        (is (= {:db {:universal-links/url "some-url"}}
               (links/handle-url {:db {}} "some-url")))))
    (testing "the user is logged in"
      (let [db {:multiaccount        {:public-key "pk"}
                :app-state           "active"
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
          (is (= [:universal-links/handle-url "some-url"] @actual))))))
  (testing "the url is nil"
    (testing "it does not dispatches the url"
      (let [actual (atom nil)]
        (with-redefs [re-frame/dispatch #(reset! actual %)]
          (links/url-event-listener #js {})
          (is (= nil @actual)))))))
