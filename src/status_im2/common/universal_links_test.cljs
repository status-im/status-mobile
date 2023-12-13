(ns status-im2.common.universal-links-test
  (:require
    [cljs.test :refer-macros [deftest is are testing]]
    [re-frame.core :as re-frame]
    [status-im2.common.universal-links :as links]))

(deftest handle-url-test
  (testing "the user is not logged in"
    (testing "it stores the url for later processing"
      (is (= {:db {:universal-links/url "some-url"}}
             (links/handle-url {:db {}} "some-url")))))
  (testing "the user is logged in"
    (let [db {:profile/profile     {:public-key "pk"}
              :app-state           "active"
              :universal-links/url "some-url"}]
      (testing "it clears the url"
        (is (nil? (get-in (links/handle-url {:db db} "some-url")
                          [:db :universal-links/url]))))
      (testing "Handle a custom string"
        (is (= (get-in (links/handle-url {:db db} "https://status.app/u#statuse2e")
                       [:router/handle-uri :uri])
               "https://status.app/u#statuse2e"))))))

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

(deftest generate-profile-url
  (testing "user has ens name"
    (testing "it calls the ens rpc method with ens name as param"
      (let [pubkey "pubkey"
            db     {:profile/profile {:ens-name? true :public-key pubkey}}
            rst    (links/generate-profile-url {:db db})]
        (are [result expected] (= result expected)
         "wakuext_shareUserURLWithENS" (-> rst :json-rpc/call first :method)
         pubkey                        (-> rst :json-rpc/call first :params first)))))
  (testing "user has no ens name"
    (testing "it calls the ens rpc method with public keyas param"
      (let [pubkey "pubkey"
            db     {:profile/profile {:public-key pubkey}}
            rst    (links/generate-profile-url {:db db})]
        (are [result expected] (= result expected)
         "wakuext_shareUserURLWithData" (-> rst :json-rpc/call first :method)
         pubkey                         (-> rst :json-rpc/call first :params first)))))
  (testing "contact has ens name"
    (testing "it calls the ens rpc method with ens name as param"
      (let [pubkey "pubkey"
            ens    "ensname.eth"
            db     {:contacts/contacts {pubkey {:ens-name ens}}}
            rst    (links/generate-profile-url {:db db} [{:public-key pubkey}])]
        (are [result expected] (= result expected)
         "wakuext_shareUserURLWithENS" (-> rst :json-rpc/call first :method)
         pubkey                        (-> rst :json-rpc/call first :params first)))))
  (testing "contact has no ens name"
    (testing "it calls the ens rpc method with public keyas param"
      (let [pubkey "pubkey"
            db     {:contacts/contacts {pubkey {:public-key pubkey}}}
            rst    (links/generate-profile-url {:db db} [{:public-key pubkey}])]
        (are [result expected] (= result expected)
         "wakuext_shareUserURLWithData" (-> rst :json-rpc/call first :method)
         pubkey                         (-> rst :json-rpc/call first :params first))))))

(deftest save-profile-url
  (testing "given a contact public key and profile url"
    (testing "it updates the contact in db"
      (let [pubkey "pubkey"
            url    "url"
            db     {:contacts/contacts {pubkey {:public-key pubkey}}}
            rst    (links/save-profile-url {:db db} [pubkey url])]
        (is (= (get-in rst [:db :contacts/contacts pubkey :universal-profile-url]) url)))))
  (testing "given a user public key and profile url"
    (testing "it updates the user profile in db"
      (let [pubkey "pubkey"
            url    "url"
            db     {:profile/profile {:public-key pubkey}}
            rst    (links/save-profile-url {:db db} [pubkey url])]
        (is (= (get-in rst [:db :profile/profile :universal-profile-url]) url)))))
  (testing "given a invalid url"
    (testing "it returns the db untouched"
      (let [pubkey "pubkey"
            url    "url"
            db     {:profile/profile {:public-key pubkey}}
            rst    (links/save-profile-url {:db db} ["invalid pubkey" url])]
        (is (= (:db rst) db)))))
  (testing "given a nil as url"
    (testing "it returns nil"
      (let [pubkey "pubkey"
            db     {:profile/profile {:public-key pubkey}}
            rst    (links/save-profile-url {:db db} ["invalid pubkey"])]
        (is (nil? rst))))))
