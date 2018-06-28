(ns status-im.test.utils.universal-links.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [re-frame.core :as re-frame]
            [status-im.utils.universal-links.core :as links]))

(deftest handle-url-test
  (testing "the user is not logged in"
    (testing "it stores the url for later processing"
      (is (= {:db {:universal-links/url "some-url"}}
             (links/handle-url "some-url" {:db {}})))))
  (testing "the user is logged in"
    (let [db {:account/account      {:public-key "pk"}
              :universal-links/url "some-url"}]
      (testing "it clears the url"
        (is (nil? (get-in (links/handle-url "some-url"
                                            {:db db})
                          [:db :universal-links/url]))))
      (testing "a public chat link"
        (testing "it joins the chat"
          (is (get-in (links/handle-url "app://get.status.im/chat/public/status"
                                        {:db db})
                      [:db :chats "status"]))))

      (testing "a browse dapp link"
        (testing "it open the dapps"
          (is
           (= "app://get.status.im/browse/www.cryptokitties.co"
              (:browse (links/handle-url "app://get.status.im/browse/www.cryptokitties.co"
                                         {:db db}))))))
      (testing "a user profile link"
        (testing "it loads the profile"
          (let [actual (links/handle-url "app://get.status.im/user/profile-id"
                                         {:db db})]
            (is (= "profile-id" (get-in actual [:db :contacts/identity])))
            (is (= :profile (get-in actual [:db :view-id]))))))
      (testing "a not found url"
        (testing "it does nothing"
          (is (nil? (links/handle-url "app://get.status.im/not-existing"
                                      {:db db}))))))))

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
  (testing "app://get.status.im/blah"
    (testing "it returns true"
      (is (links/universal-link? "app://get.status.im/blah"))))
  (testing "http://get.status.im/blah"
    (testing "it returns true"
      (is (links/universal-link? "http://get.status.im/blah"))))
  (testing "https://get.status.im/blah"
    (testing "it returns true"
      (is (links/universal-link? "https://get.status.im/blah"))))
  (testing "app://not.status.im/blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "http://not.status.im/blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "https://not.status.im/blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah"))))))

(deftest stored-url-event
  (testing "the url is in the database"
    (testing "it returns the event"
      (= [:handle-universal-link "some-url"]
         (links/stored-url-event {:db {:universal-links/url "some-url"}}))))
  (testing "the url is not in the database"
    (testing "it returns nil"
      (= nil
         (links/stored-url-event {:db {}})))))
