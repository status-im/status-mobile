(ns status-im.utils.universal-links.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [re-frame.core :as re-frame]
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
        (testing "a public chat link"
          (testing "it joins the chat, short version"
            (is (get-in (links/handle-url {:db db} "status-im://status")
                        [:db :chats "status"])))
          (testing "it joins the chat, short version, https"
            (is (get-in (links/handle-url {:db db} "https://join.status.im/status")
                        [:db :chats "status"])))
          (testing "it joins the chat"
            (is (get-in (links/handle-url {:db db} "status-im://chat/public/status")
                        [:db :chats "status"]))))
        (testing "a browse dapp link"
          (testing "it open the dapps short version"
            (is
             (= "www.cryptokitties.co"
                (:browser/show-browser-selection (links/handle-url {:db db} "status-im://b/www.cryptokitties.co")))))
          (testing "it open the dapps short version, https"
            (is
             (= "www.cryptokitties.co"
                (:browser/show-browser-selection (links/handle-url {:db db} "https://join.status.im/b/www.cryptokitties.co")))))
          (testing "it open the dapps"
            (is
             (= "www.cryptokitties.co"
                (:browser/show-browser-selection (links/handle-url {:db db} "status-im://browse/www.cryptokitties.co"))))))
        (testing "a user profile link"
          (testing "it loads the profile, short version"
            (let [actual (links/handle-url {:db db} "status-im://u/0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073")]
              (is (= "0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073" (get-in actual [:db :contacts/identity])))))
          (testing "it loads the profile, short version https"
            (let [actual (links/handle-url {:db db} "https://join.status.im/u/0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073")]
              (is (= "0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073" (get-in actual [:db :contacts/identity])))))
          (testing "it loads the profile"
            (let [actual (links/handle-url {:db db} "status-im://user/0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073")]
              (is (= "0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073" (get-in actual [:db :contacts/identity]))))))
        (testing "Handle a custom string as a an profile link with ens-name"
          (is (= (get-in (links/handle-url {:db db} "status-im://u/CONTACTCODE")
                         [:resolve-public-key :contact-identity])
                 "CONTACTCODE")))
        (testing "Handle a custom string as a an profile link with ens-name, http"
          (is (= (get-in (links/handle-url {:db db} "https://join.status.im/u/statuse2e")
                         [:resolve-public-key :contact-identity])
                 "statuse2e")))
        (testing "Handle a custom string as a an profile link with ens-name"
          (is (= (get-in (links/handle-url {:db db} "status-im://user/CONTACTCODE")
                         [:resolve-public-key :contact-identity])
                 "CONTACTCODE")))
        (testing "a not found url"
          (testing "it does nothing"
            (is (nil? (links/handle-url {:db db} "status-im://blah/not-existing")))))))))

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
