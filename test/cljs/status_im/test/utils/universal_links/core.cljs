(ns status-im.test.utils.universal-links.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [re-frame.core :as re-frame]
            [status-im.utils.universal-links.core :as links]))

(deftest handle-url-test
  (testing "the user is not logged in"
    (testing "it stores the url for later processing"
      (is (= {:db {:universal-links/url "some-url"}}
             (links/handle-url {:db {}} "some-url")))))
  (testing "the user is logged in"
    (let [db {:account/account      {:public-key "pk"}
              :universal-links/url "some-url"}]
      (testing "it clears the url"
        (is (nil? (get-in (links/handle-url {:db db} "some-url")
                          [:db :universal-links/url]))))
      (testing "a public chat link"
        (testing "it joins the chat"
          (is (get-in (links/handle-url {:db db} "status-im://chat/public/status")
                      [:db :chats "status"]))))

      (testing "a browse dapp link"
        (testing "it open the dapps"
          (is
           (= "www.cryptokitties.co"
              (:browser/show-browser-selection (links/handle-url {:db db} "status-im://browse/www.cryptokitties.co"))))))
      (testing "a user profile link"
        (testing "it loads the profile"
          (let [actual (links/handle-url {:db db} "status-im://user/0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073")]
            (is (= "0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073" (get-in actual [:db :contacts/identity]))))))
      (testing "if does nothing because the link is invalid"
        (is (= (links/handle-url {:db db} "status-im://user/CONTACTCODE")
               nil)))
      (testing "a not found url"
        (testing "it does nothing"
          (is (nil? (links/handle-url {:db db} "status-im://not-existing"))))))))

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
  (testing "http://get.status.im/blah"
    (testing "it returns true"
      (is (links/universal-link? "http://get.status.im/blah"))))
  (testing "https://get.status.im/blah"
    (testing "it returns true"
      (is (links/universal-link? "https://get.status.im/blah"))))
  (testing "unicode characters"
    (testing "it returns false"
      (is (not (links/universal-link? "https://get.status.im/browse/www.аррӏе.com")))))
  (testing "not-status-im://blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "http://not.status.im/blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "https://not.status.im/blah"
    (testing "it returns false"
      (is (not (links/universal-link? "https://not.status.im/blah")))))
  (testing "http://get.status.im/blah"
    (testing "it returns false"
      (is (not (links/deep-link? "http://get.status.im/blah"))))))

(deftest process-stored-event
  (testing "the url is in the database"
    (testing "it returns the event"
      (= "some-url"
         (links/process-stored-event {:db {:universal-links/url "some-url"}}))))
  (testing "the url is not in the database"
    (testing "it returns nil"
      (= nil
         (links/process-stored-event {:db {}})))))
