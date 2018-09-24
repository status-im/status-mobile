(ns status-im.test.mailserver.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.mailserver.core :as mailserver]))

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
      (is (= {:db {:mailservers/manage {:name {:value "value"
                                               :error false}}}}
             (mailserver/set-input {:db {}} :name "value"))))
    (testing "blank name"
      (is (= {:db {:mailservers/manage {:name {:value ""
                                               :error true}}}}
             (mailserver/set-input {:db {}} :name "")))))
  (testing "it validates enodes url"
    (testing "correct url"
      (is (= {:db {:mailservers/manage {:url {:value valid-enode-url
                                              :error false}}}}
             (mailserver/set-input {:db {}} :url valid-enode-url))))
    (testing "broken url"
      (is (= {:db {:mailservers/manage {:url {:value "broken"
                                              :error true}}}}
             (mailserver/set-input {:db {}} :url "broken"))))))

(deftest edit-mailserver
  (let [db {:inbox/wnodes
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
                 (-> actual :db :mailservers/manage))))
        (testing "it navigates to edit-mailserver view"
          (is (= :edit-mailserver
                 (:status-im.ui.screens.navigation/navigate-to actual))))))
    (testing "when an id is given"
      (testing "when the wnode is in the list"
        (let [actual (mailserver/edit cofx "a")]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value "a"
                           :error false}
                    :url  {:value valid-enode-url
                           :error false}
                    :name {:value "name"
                           :error false}}
                   (-> actual :db :mailservers/manage))))
          (testing "it navigates to edit-mailserver view"
            (is (= :edit-mailserver
                   (:status-im.ui.screens.navigation/navigate-to actual))))))
      (testing "when the wnode is not in the list"
        (let [actual (mailserver/edit cofx "not-existing")]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value nil
                           :error false}
                    :url  {:value ""
                           :error true}
                    :name {:value ""
                           :error true}}
                   (-> actual :db :mailservers/manage))))
          (testing "it navigates to edit-mailserver view"
            (is (= :edit-mailserver
                   (:status-im.ui.screens.navigation/navigate-to actual)))))))))

(deftest connected-mailserver
  (testing "it returns true when set in inbox/current-id"
    (let [cofx {:db {:inbox/current-id "a"}}]
      (is (mailserver/connected? cofx "a"))))
  (testing "it returns false otherwise"
    (is (not (mailserver/connected? {:db {}} "a")))))

(deftest fetch-mailserver
  (testing "it fetches the mailserver from the db"
    (let [cofx {:db {:inbox/wnodes {:eth.beta {"a" {:id      "a"
                                                    :name    "old-name"
                                                    :address "enode://old-id:old-password@url:port"}}}}}]
      (is (mailserver/fetch cofx "a")))))

(deftest fetch-current-mailserver
  (testing "it fetches the mailserver from the db with corresponding id"
    (let [cofx {:db {:inbox/current-id "a"
                     :inbox/wnodes {:eth.beta {"a" {:id      "a"
                                                    :name    "old-name"
                                                    :address "enode://old-id:old-password@url:port"}}}}}]
      (is (mailserver/fetch-current cofx)))))

(deftest set-current-mailserver
  (with-redefs [rand-nth (comp last sort)]
    (let [cofx {:db {:inbox/wnodes {:eth.beta {"a" {}
                                               "b" {}
                                               "c" {}
                                               "d" {}}}}}]
      (testing "the user has already a preference"
        (let [cofx (assoc-in cofx
                             [:db :account/account :settings]
                             {:wnode {:eth.beta "a"}})]
          (testing "the mailserver exists"
            (testing "it sets the preferred mailserver"
              (is (= "a" (-> (mailserver/set-current-mailserver cofx)
                             :db
                             :inbox/current-id)))))
          (testing "the mailserver does not exists"
            (let [cofx (update-in cofx [:db :inbox/wnodes :eth.beta] dissoc "a")]
              (testing "sets a random mailserver"
                (is (= "d" (-> (mailserver/set-current-mailserver cofx)
                               :db
                               :inbox/current-id))))))))
      (testing "the user has not set an explicit preference"
        (testing "current-id is not set"
          (testing "it sets a random mailserver"
            (is (= "d" (-> (mailserver/set-current-mailserver cofx)
                           :db
                           :inbox/current-id)))))
        (testing "current-id is set"
          (testing "it sets the next mailserver"
            (is (= "c" (-> (mailserver/set-current-mailserver (assoc-in
                                                               cofx
                                                               [:db :inbox/current-id]
                                                               "b"))
                           :db
                           :inbox/current-id)))
            (is (= "a" (-> (mailserver/set-current-mailserver (assoc-in
                                                               cofx
                                                               [:db :inbox/current-id]
                                                               "d"))
                           :db
                           :inbox/current-id)))
            (is (= "a" (-> (mailserver/set-current-mailserver (assoc-in
                                                               cofx
                                                               [:db :inbox/current-id]
                                                               "non-existing"))
                           :db
                           :inbox/current-id)))))))))

(deftest delete-mailserver
  (testing "the user is not connected to the mailserver"
    (let [cofx {:random-id "random-id"
                :db {:inbox/wnodes {:eth.beta {"a" {:id           "a"
                                                    :name         "old-name"
                                                    :user-defined true
                                                    :address      "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/delete cofx "a")]
      (testing "it removes the mailserver from the list"
        (is (not (mailserver/fetch actual "a"))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))))
  (testing "the mailserver is not user-defined"
    (let [cofx {:random-id "random-id"
                :db {:inbox/wnodes {:eth.beta {"a" {:id      "a"
                                                    :name    "old-name"
                                                    :address "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/delete cofx "a")]
      (testing "it does not delete the mailserver"
        (is (= {:dispatch [:navigate-back]} actual)))))
  (testing "the user is connected to the mailserver"
    (let [cofx {:random-id "random-id"
                :db {:inbox/wnodes {:eth.beta {"a" {:id      "a"
                                                    :name    "old-name"
                                                    :address "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/delete cofx "a")]
      (testing "it does not remove the mailserver from the list"
        (is (= {:dispatch [:navigate-back]} actual))))))

(deftest upsert-mailserver
  (testing "new mailserver"
    (let [cofx {:random-id "random-id"
                :db {:mailservers/manage {:name {:value "test-name"}
                                          :url  {:value "enode://test-id:test-password@url:port"}}

                     :inbox/wnodes {}}}
          actual (mailserver/upsert cofx)]

      (testing "it adds the enode to inbox/wnodes"
        (is (= {:eth.beta {:randomid {:password "test-password"
                                      :address "enode://test-id@url:port"
                                      :name "test-name"
                                      :id :randomid
                                      :user-defined true}}}
               (get-in actual [:db :inbox/wnodes]))))
      (testing "it navigates back"
        (is (= [:navigate-back]
               (:dispatch actual))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))))
  (testing "existing mailserver"
    (let [cofx {:random-id "random-id"
                :db {:mailservers/manage {:id   {:value  :a}
                                          :name {:value "new-name"}
                                          :url  {:value "enode://new-id:new-password@url:port"}}

                     :inbox/wnodes {:eth.beta {:a {:id      :a
                                                   :name    "old-name"
                                                   :address "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/upsert cofx)]
      (testing "it navigates back"
        (is (= [:navigate-back]
               (:dispatch actual))))
      (testing "it updates the enode to inbox/wnodes"
        (is (= {:eth.beta {:a {:password "new-password"
                               :address "enode://new-id@url:port"
                               :name "new-name"
                               :id :a
                               :user-defined true}}}
               (get-in actual [:db :inbox/wnodes]))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))
      (testing "it logs the user out if connected to the current mailserver"
        (let [actual (mailserver/upsert (assoc-in cofx
                                                  [:db :inbox/current-id] :a))]
          (is (= [:accounts.logout.ui/logout-confirmed]
                 (-> actual :data-store/tx first :success-event))))))))
