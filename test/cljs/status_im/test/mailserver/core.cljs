(ns status-im.test.mailserver.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.utils :as utils]
            [status-im.mailserver.core :as mailserver]))

(def enode "enode://08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b@163.172.177.138:40404")
(def enode2 "enode://12d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b@163.172.177.138:40404")

(deftest test-extract-enode-id
  (testing "Get enode id from enode uri"
    (is (= "08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b"
           (utils/extract-enode-id enode))))
  (testing "Get enode id from mailformed enode uri"
    (is (= ""
           (utils/extract-enode-id "08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b@163.172.177.138:40404"))))
  (testing "Test empty string"
    (is (= ""
           (utils/extract-enode-id ""))))
  (testing "Test nil"
    (is (= ""
           (utils/extract-enode-id nil)))))

(def peers
  [{:id "08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b"
    :name "StatusIM/v0.9.9-unstable/linux-amd64/go1.9.2"}
   {:id "0f7c65277f916ff4379fe520b875082a56e587eb3ce1c1567d9ff94206bdb05ba167c52272f20f634cd1ebdec5d9dfeb393018bfde1595d8e64a717c8b46692f"
    :name "Geth/v1.7.2-stable/linux-amd64/go1.9.1"}])

(deftest test-registered-peer?
  (testing "Peer is registered"
    (is (mailserver/registered-peer? peers enode)))
  (testing "Peer is not peers list"
    (is (not (mailserver/registered-peer? peers enode2))))
  (testing "Empty peers"
    (is (not (mailserver/registered-peer? [] enode))))
  (testing "Empty peer"
    (is (not (mailserver/registered-peer? peers ""))))
  (testing "Nil peer"
    (is (not (mailserver/registered-peer? peers nil)))))

(def enode-id "1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40")
(def password "password")
(def host "167.99.209.61:30504")

(def valid-enode-address (str "enode://" enode-id "@" host))
(def valid-enode-url (str "enode://" enode-id ":" password "@" host))

(deftest valid-enode-address-test
  (testing "url without password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40@167.99.209.61:30504"]
      (is (mailserver/valid-enode-address? address))))
  (testing "url with password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40:somepasswordwith@and:@@167.99.209.61:30504"]
      (is (not (mailserver/valid-enode-address? address)))))
  (testing "invalid url"
    (is (not (mailserver/valid-enode-address? "something not valid")))))

(deftest valid-enode-url-test
  (testing "url without password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40@167.99.209.61:30504"]
      (is (not (mailserver/valid-enode-url? address)))))
  (testing "url with password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40:somepasswordwith@and:@@167.99.209.61:30504"]
      (is (mailserver/valid-enode-url? address))))
  (testing "invalid url"
    (is (not (mailserver/valid-enode-url? "something not valid")))))

(deftest address->mailserver
  (testing "with password"
    (let [address "enode://some-id:the-password@206.189.56.154:30504"]
      (is (= {:address "enode://some-id@206.189.56.154:30504"
              :password "the-password"
              :user-defined true}
             (mailserver/address->mailserver address)))))
  (testing "without password"
    (let [address "enode://some-id@206.189.56.154:30504"]
      (is (= {:address "enode://some-id@206.189.56.154:30504"
              :user-defined true}
             (mailserver/address->mailserver address))))))

(deftest set-input
  (testing "it validates names"
    (testing "correct name"
      (is (= {:db {:mailserver.edit/mailserver {:name {:value "value"
                                                       :error false}}}}
             (mailserver/set-input {:db {}} :name "value"))))
    (testing "blank name"
      (is (= {:db {:mailserver.edit/mailserver {:name {:value ""
                                                       :error true}}}}
             (mailserver/set-input {:db {}} :name "")))))
  (testing "it validates enodes url"
    (testing "correct url"
      (is (= {:db {:mailserver.edit/mailserver {:url {:value valid-enode-url
                                                      :error false}}}}
             (mailserver/set-input {:db {}} :url valid-enode-url))))
    (testing "broken url"
      (is (= {:db {:mailserver.edit/mailserver {:url {:value "broken"
                                                      :error true}}}}
             (mailserver/set-input {:db {}} :url "broken"))))))

(deftest edit-mailserver
  (let [db {:mailserver/mailservers
            {:eth.beta {"a" {:id      "a"
                             :address valid-enode-address
                             :password password
                             :name    "name"}}}}
        cofx {:db db}]
    (testing "when no id is given"
      (let [actual (mailserver/edit cofx nil)]
        (testing "it resets :mailserver/manage"
          (is (= {:id   {:value nil
                         :error false}
                  :url  {:value ""
                         :error true}
                  :name {:value ""
                         :error true}}
                 (-> actual :db :mailserver.edit/mailserver))))
        (testing "it navigates to edit-mailserver view"
          (is (= :edit-mailserver
                 (:status-im.ui.screens.navigation/navigate-to actual))))))
    (testing "when an id is given"
      (testing "when the mailserver is in the list"
        (let [actual (mailserver/edit cofx "a")]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value "a"
                           :error false}
                    :url  {:value valid-enode-url
                           :error false}
                    :name {:value "name"
                           :error false}}
                   (-> actual :db :mailserver.edit/mailserver))))
          (testing "it navigates to edit-mailserver view"
            (is (= :edit-mailserver
                   (:status-im.ui.screens.navigation/navigate-to actual))))))
      (testing "when the mailserver is not in the list"
        (let [actual (mailserver/edit cofx "not-existing")]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value nil
                           :error false}
                    :url  {:value ""
                           :error true}
                    :name {:value ""
                           :error true}}
                   (-> actual :db :mailserver.edit/mailserver))))
          (testing "it navigates to edit-mailserver view"
            (is (= :edit-mailserver
                   (:status-im.ui.screens.navigation/navigate-to actual)))))))))

(deftest connected-mailserver
  (testing "it returns true when set in mailserver/current-id"
    (let [cofx {:db {:mailserver/current-id "a"}}]
      (is (mailserver/connected? cofx "a"))))
  (testing "it returns false otherwise"
    (is (not (mailserver/connected? {:db {}} "a")))))

(deftest fetch-mailserver
  (testing "it fetches the mailserver from the db"
    (let [cofx {:db {:mailserver/mailservers {:eth.beta {"a" {:id      "a"
                                                              :name    "old-name"
                                                              :address "enode://old-id:old-password@url:port"}}}}}]
      (is (mailserver/fetch cofx "a")))))

(deftest fetch-current-mailserver
  (testing "it fetches the mailserver from the db with corresponding id"
    (let [cofx {:db {:mailserver/current-id "a"
                     :mailserver/mailservers {:eth.beta {"a" {:id      "a"
                                                              :name    "old-name"
                                                              :address "enode://old-id:old-password@url:port"}}}}}]
      (is (mailserver/fetch-current cofx)))))

(deftest set-current-mailserver
  (with-redefs [rand-nth (comp last sort)]
    (let [cofx {:db {:mailserver/mailservers {:eth.beta {"a" {}
                                                         "b" {}
                                                         "c" {}
                                                         "d" {}}}}}]
      (testing "the user has already a preference"
        (let [cofx (assoc-in cofx
                             [:db :account/account :settings]
                             {:mailserver {:eth.beta "a"}})]
          (testing "the mailserver exists"
            (testing "it sets the preferred mailserver"
              (is (= "a" (-> (mailserver/set-current-mailserver cofx)
                             :db
                             :mailserver/current-id)))))
          (testing "the mailserver does not exists"
            (let [cofx (update-in cofx [:db :mailserver/mailservers :eth.beta] dissoc "a")]
              (testing "sets a random mailserver"
                (is (= "d" (-> (mailserver/set-current-mailserver cofx)
                               :db
                               :mailserver/current-id))))))))
      (testing "the user has not set an explicit preference"
        (testing "current-id is not set"
          (testing "it sets a random mailserver"
            (is (= "d" (-> (mailserver/set-current-mailserver cofx)
                           :db
                           :mailserver/current-id)))))
        (testing "current-id is set"
          (testing "it sets the next mailserver"
            (is (= "c" (-> (mailserver/set-current-mailserver (assoc-in
                                                               cofx
                                                               [:db :mailserver/current-id]
                                                               "b"))
                           :db
                           :mailserver/current-id)))
            (is (= "a" (-> (mailserver/set-current-mailserver (assoc-in
                                                               cofx
                                                               [:db :mailserver/current-id]
                                                               "d"))
                           :db
                           :mailserver/current-id)))
            (is (= "a" (-> (mailserver/set-current-mailserver (assoc-in
                                                               cofx
                                                               [:db :mailserver/current-id]
                                                               "non-existing"))
                           :db
                           :mailserver/current-id)))))))))

(deftest delete-mailserver
  (testing "the user is not connected to the mailserver"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver/mailservers {:eth.beta {"a" {:id           "a"
                                                              :name         "old-name"
                                                              :user-defined true
                                                              :address      "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/delete cofx "a")]
      (testing "it removes the mailserver from the list"
        (is (not (mailserver/fetch actual "a"))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))))
  (testing "the mailserver is not user-defined"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver/mailservers {:eth.beta {"a" {:id      "a"
                                                              :name    "old-name"
                                                              :address "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/delete cofx "a")]
      (testing "it does not delete the mailserver"
        (is (= {:dispatch [:navigate-back]} actual)))))
  (testing "the user is connected to the mailserver"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver/mailservers {:eth.beta {"a" {:id      "a"
                                                              :name    "old-name"
                                                              :address "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/delete cofx "a")]
      (testing "it does not remove the mailserver from the list"
        (is (= {:dispatch [:navigate-back]} actual))))))

(deftest upsert-mailserver
  (testing "new mailserver"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver.edit/mailserver {:name {:value "test-name"}
                                                  :url  {:value "enode://test-id:test-password@url:port"}}

                     :mailserver/mailservers {}}}
          actual (mailserver/upsert cofx)]

      (testing "it adds the enode to mailserver/mailservers"
        (is (= {:eth.beta {:randomid {:password "test-password"
                                      :address "enode://test-id@url:port"
                                      :name "test-name"
                                      :id :randomid
                                      :user-defined true}}}
               (get-in actual [:db :mailserver/mailservers]))))
      (testing "it navigates back"
        (is (= [:navigate-back]
               (:dispatch actual))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))))
  (testing "existing mailserver"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver.edit/mailserver {:id   {:value  :a}
                                                  :name {:value "new-name"}
                                                  :url  {:value "enode://new-id:new-password@url:port"}}

                     :mailserver/mailservers {:eth.beta {:a {:id      :a
                                                             :name    "old-name"
                                                             :address "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/upsert cofx)]
      (testing "it navigates back"
        (is (= [:navigate-back]
               (:dispatch actual))))
      (testing "it updates the enode to mailserver/mailservers"
        (is (= {:eth.beta {:a {:password "new-password"
                               :address "enode://new-id@url:port"
                               :name "new-name"
                               :id :a
                               :user-defined true}}}
               (get-in actual [:db :mailserver/mailservers]))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))
      (testing "it logs the user out if connected to the current mailserver"
        (let [actual (mailserver/upsert (assoc-in cofx
                                                  [:db :mailserver/current-id] :a))]
          (is (= [:accounts.logout.ui/logout-confirmed]
                 (-> actual :data-store/tx first :success-event))))))))

(defn cofx-fixtures [sym-key registered-peer?]
  {:db {:mailserver/state :connected
        :peers-summary (if registered-peer?
                         [{:id "mailserver-id" :enode "enode://mailserver-id@ip"}]
                         [])
        :account/account {:settings {:fleet :eth.beta}}
        :mailserver/current-id "mailserver-a"
        :mailserver/mailservers {:eth.beta {"mailserver-a" {:sym-key-id sym-key
                                                            :address "enode://mailserver-id@ip"}}}}})

(defn peers-summary-change-result [sym-key registered-peer? registered-peer-before?]
  (mailserver/peers-summary-change (cofx-fixtures sym-key
                                                  registered-peer?)
                                   (if registered-peer-before?
                                     [{:id "mailserver-id" :enode "enode://mailserver-id@ip"}]
                                     [])))

(deftest peers-summary-change
  (testing "Mailserver added, sym-key doesn't exist"
    (let [result (peers-summary-change-result false true false)]
      (is (= (into #{} (keys result))
             #{:mailserver/mark-trusted-peer :shh/generate-sym-key-from-password :db}))))
  (testing "Mailserver disconnected, sym-key exists"
    (let [result (peers-summary-change-result true false true)]
      (is (= (into #{} (keys result))
             #{:db :mailserver/add-peer :utils/dispatch-later}))
      (is (= (get-in result [:db :mailserver/state])
             :connecting))))
  (testing "Mailserver disconnected, sym-key doesn't exists (unlikely situation in practice)"
    (let [result (peers-summary-change-result false false true)]
      (is (= (into #{} (keys result))
             #{:db :mailserver/add-peer :utils/dispatch-later  :shh/generate-sym-key-from-password}))
      (is (= (get-in result [:db :mailserver/state])
             :connecting))))
  (testing "Mailserver isn't concerned by peer summary changes"
    (is (= (into #{} (keys (peers-summary-change-result true true true)))
           #{}))
    (is (= (into #{} (keys (peers-summary-change-result true false false)))
           #{}))))

(deftest connect-to-mailserver
  (let [db {:mailserver/current-id "mailserverid"
            :mailserver/mailservers
            {:eth.beta {"mailserverid" {:address  "mailserver-address"
                                        :password "mailserver-password"}}}
            :account/account
            {:settings {:fleet :eth.beta
                        :mailserver {:eth.beta "mailserverid"}}}}]
    (testing "it adds the peer"
      (is (= "mailserver-address"
             (:mailserver/add-peer (mailserver/connect-to-mailserver {:db db})))))
    (testing "it generates a sym key if hasn't been generated before"
      (is (= "mailserver-password"
             (-> (mailserver/connect-to-mailserver {:db db})
                 :shh/generate-sym-key-from-password
                 first
                 :password))))
    (let [mailserver-with-sym-key-db (assoc-in db
                                               [:mailserver/mailservers :eth.beta "mailserverid" :sym-key-id]
                                               "somesymkeyid")]
      (testing "it does not generate a sym key if already present"
        (is (not (-> (mailserver/connect-to-mailserver {:db mailserver-with-sym-key-db})
                     :shh/generate-sym-key-from-password
                     first)))))))
