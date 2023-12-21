(ns status-im.common.universal-links-test
  (:require
    [cljs.test :refer-macros [deftest is are testing]]
    matcher-combinators.test
    [re-frame.core :as re-frame]
    [status-im.common.universal-links :as links]))

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

(deftest universal-link-test
  (testing "universal-link?"
    (are [l rst] (match? (links/universal-link? l) rst)
     "status-app://blah"
      false
     "http://status.app/blah"
      false
     "http://status.app/c#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgB"
      true
     "http://status.app/u#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgB"
      true
     "https://status.app/u/Ow==#zQ3shsKnV5HJMWJR61c6dssWzHszdbLfBoMF1gcLtSQAYdw2d"
      true
     "https://status.app/c/Ow==#zQ3shYSHp7GoiXaauJMnDcjwU2yNjdzpXLosAWapPS4CFxc11"
      true
     "https://status.app/c/Ow==#zQ3shYSHp7GoiXaauJMnDcjwU2yNjdzpXLosAWapPS4CFxc111"
      false
     "https://status.app/u/G10A4B0JdgwyRww90WXtnP1oNH1ZLQNM0yX0Ja9YyAMjrqSZIYINOHCbFhrnKRAcPGStPxCMJDSZlGCKzmZrJcimHY8BbcXlORrElv_BbQEegnMDPx1g9C5VVNl0fE4y#zQ3shwQPhRuDJSjVGVBnTjCdgXy5i9WQaeVPdGJD6yTarJQSj"
      true
     "https://status.app/c/G00AAGS9TbI9mSR-ZNmFrhRjNuEeXAAbcAIUaLLJyjMOG3ACJQ12oIHD78QhzO9s_T5bUeU7rnATWJg3mGgTUemrAg==#zQ3shYf5SquxkiY3FmCW6Nz2wuFWFcM6JEdUD62ApjAvE5YPv"
      true
     "http://status.app/c#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgBhttp://status.app/c#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgB"
      false
     "http://status.app/u#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgBhttp://status.app/u#zQ3shPyZJnxZK4Bwyx9QsaksNKDYTPmpwPvGSjMYVHoXHeEgB"
      false
     "https://status.app/u/Ow==#zQ3shsKnV5HJMWJR61c6dssWzHszdbLfBoMF1gcLtSQAYdw2dhttps://status.app/u/Ow==#zQ3shsKnV5HJMWJR61c6dssWzHszdbLfBoMF1gcLtSQAYdw2d"
      false
     "https://status.app/c/Ow==#zQ3shYSHp7GoiXaauJMnDcjwU2yNjdzpXLosAWapPS4CFxc11https://status.app/c/Ow==#zQ3shYSHp7GoiXaauJMnDcjwU2yNjdzpXLosAWapPS4CFxc11"
      false
     "https://status.app/u/G10A4B0JdgwyRww90WXtnP1oNH1ZLQNM0yX0Ja9YyAMjrqSZIYINOHCbFhrnKRAcPGStPxCMJDSZlGCKzmZrJcimHY8BbcXlORrElv_BbQEegnMDPx1g9C5VVNl0fE4y#zQ3shwQPhRuDJSjVGVBnTjCdgXy5i9WQaeVPdGJD6yTarJQSjhttps://status.app/u/G10A4B0JdgwyRww90WXtnP1oNH1ZLQNM0yX0Ja9YyAMjrqSZIYINOHCbFhrnKRAcPGStPxCMJDSZlGCKzmZrJcimHY8BbcXlORrElv_BbQEegnMDPx1g9C5VVNl0fE4y#zQ3shwQPhRuDJSjVGVBnTjCdgXy5i9WQaeVPdGJD6yTarJQSj"
      false
     "https://status.app/c/Ow==#zQ3shbmfT3hvh4mKa1v6uAjjyztQEroh8Mfn6Ckegjd7LT3XKhttps://status.app/c#zQ3shbmfT3hvh4mKa1v6uAjjyztQEroh8Mfn6Ckegjd7LT3XK"
      false
     "https://status.app/blah"
      false
     "https://status.app/browse/www.аррӏе.com"
      false
     "https://not.status.im/blah"
      false
     "http://not.status.im/blah"
      false))

  (testing "deep-link?"
    (are [l rst] (match? (links/deep-link? l) rst)
     "status-app://blah"                                   true
     "http://status.app/blah"                              false
     "ethereum:0x89205a3a3b2a69de6dbf7f01ed13b2108b2c43e7" true)))
