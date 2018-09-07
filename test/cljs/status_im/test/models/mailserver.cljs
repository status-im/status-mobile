(ns status-im.test.models.mailserver
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.models.mailserver :as model]))

(def enode-id "1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40")
(def password "password")
(def host "167.99.209.61:30504")

(def valid-enode-address (str "enode://" enode-id "@" host))
(def valid-enode-url (str "enode://" enode-id ":" password "@" host))

(deftest valid-enode-address-test
  (testing "url without password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40@167.99.209.61:30504"]
      (is (model/valid-enode-address? address))))
  (testing "url with password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40:somepasswordwith@and:@@167.99.209.61:30504"]
      (is (not (model/valid-enode-address? address)))))
  (testing "invalid url"
    (is (not (model/valid-enode-address? "something not valid")))))

(deftest valid-enode-url-test
  (testing "url without password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40@167.99.209.61:30504"]
      (is (not (model/valid-enode-url? address)))))
  (testing "url with password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40:somepasswordwith@and:@@167.99.209.61:30504"]
      (is (model/valid-enode-url? address))))
  (testing "invalid url"
    (is (not (model/valid-enode-url? "something not valid")))))

(deftest address->mailserver
  (testing "with password"
    (let [address "enode://some-id:the-password@206.189.56.154:30504"]
      (is (= {:address "enode://some-id@206.189.56.154:30504"
              :password "the-password"
              :user-defined true}
             (model/address->mailserver address)))))
  (testing "without password"
    (let [address "enode://some-id@206.189.56.154:30504"]
      (is (= {:address "enode://some-id@206.189.56.154:30504"
              :user-defined true}
             (model/address->mailserver address))))))

(deftest set-input
  (testing "it validates names"
    (testing "correct name"
      (is (= {:db {:mailservers/manage {:name {:value "value"
                                               :error false}}}}
             (model/set-input :name "value" {}))))
    (testing "blank name"
      (is (= {:db {:mailservers/manage {:name {:value ""
                                               :error true}}}}
             (model/set-input :name "" {})))))
  (testing "it validates enodes url"
    (testing "correct url"
      (is (= {:db {:mailservers/manage {:url {:value valid-enode-url
                                              :error false}}}}
             (model/set-input :url valid-enode-url {}))))
    (testing "broken url"
      (is (= {:db {:mailservers/manage {:url {:value "broken"
                                              :error true}}}}
             (model/set-input :url "broken" {}))))))

(deftest edit-mailserver
  (let [db {:inbox/wnodes
            {:eth.beta {"a" {:id      "a"
                             :address valid-enode-address
                             :password password
                             :name    "name"}}}}
        cofx {:db db}]
    (testing "when no id is given"
      (let [actual (model/edit nil cofx)]
        (testing "it resets :mailserver/manage"
          (is (= {:id   {:value nil
                         :error false}
                  :url  {:value ""
                         :error true}
                  :name {:value ""
                         :error true}}
                 (-> actual :db :mailservers/manage))))
        (testing "it navigates to edit-mailserver view"
          (is (= [:navigate-to :edit-mailserver]
                 (-> actual :dispatch))))))
    (testing "when an id is given"
      (testing "when the wnode is in the list"
        (let [actual (model/edit "a" cofx)]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value "a"
                           :error false}
                    :url  {:value valid-enode-url
                           :error false}
                    :name {:value "name"
                           :error false}}
                   (-> actual :db :mailservers/manage))))
          (testing "it navigates to edit-mailserver view"
            (is (= [:navigate-to :edit-mailserver]
                   (-> actual :dispatch))))))
      (testing "when the wnode is not in the list"
        (let [actual (model/edit "not-existing" cofx)]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value nil
                           :error false}
                    :url  {:value ""
                           :error true}
                    :name {:value ""
                           :error true}}
                   (-> actual :db :mailservers/manage))))
          (testing "it navigates to edit-mailserver view"
            (is (= [:navigate-to :edit-mailserver]
                   (-> actual :dispatch)))))))))

(deftest connected-mailserver
  (testing "it returns true when set in inbox/current-id"
    (let [cofx {:db {:inbox/current-id "a"}}]
      (is (model/connected? "a" cofx))))
  (testing "it returns false otherwise"
    (is (not (model/connected? "a" {})))))

(deftest fetch-mailserver
  (testing "it fetches the mailserver from the db"
    (let [cofx {:db {:inbox/wnodes {:eth.beta {"a" {:id      "a"
                                                    :name    "old-name"
                                                    :address "enode://old-id:old-password@url:port"}}}}}]
      (is (model/fetch "a" cofx)))))

(deftest fetch-current-mailserver
  (testing "it fetches the mailserver from the db with corresponding id"
    (let [cofx {:db {:inbox/current-id "a"
                     :inbox/wnodes {:eth.beta {"a" {:id      "a"
                                                    :name    "old-name"
                                                    :address "enode://old-id:old-password@url:port"}}}}}]
      (is (model/fetch-current cofx)))))

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
              (is (= "a" (-> (model/set-current-mailserver cofx)
                             :db
                             :inbox/current-id)))))
          (testing "the mailserver does not exists"
            (let [cofx (update-in cofx [:db :inbox/wnodes :eth.beta] dissoc "a")]
              (testing "sets a random mailserver"
                (is (= "d" (-> (model/set-current-mailserver cofx)
                               :db
                               :inbox/current-id))))))))
      (testing "the user has not set an explicit preference"
        (testing "current-id is not set"
          (testing "it sets a random mailserver"
            (is (= "d" (-> (model/set-current-mailserver cofx)
                           :db
                           :inbox/current-id)))))
        (testing "current-id is set"
          (testing "it sets the next mailserver"
            (is (= "c" (-> (model/set-current-mailserver (assoc-in
                                                          cofx
                                                          [:db :inbox/current-id]
                                                          "b"))
                           :db
                           :inbox/current-id)))
            (is (= "a" (-> (model/set-current-mailserver (assoc-in
                                                          cofx
                                                          [:db :inbox/current-id]
                                                          "d"))
                           :db
                           :inbox/current-id)))
            (is (= "a" (-> (model/set-current-mailserver (assoc-in
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
          actual (model/delete "a" cofx)]
      (testing "it removes the mailserver from the list"
        (is (not (model/fetch "a" actual))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))))
  (testing "the mailserver is not user-defined"
    (let [cofx {:random-id "random-id"
                :db {:inbox/wnodes {:eth.beta {"a" {:id      "a"
                                                    :name    "old-name"
                                                    :address "enode://old-id:old-password@url:port"}}}}}
          actual (model/delete "a" cofx)]
      (testing "it does not delete the mailserver"
        (is (nil? actual)))))
  (testing "the user is connected to the mailserver"
    (let [cofx {:random-id "random-id"
                :db {:inbox/wnodes {:eth.beta {"a" {:id      "a"
                                                    :name    "old-name"
                                                    :address "enode://old-id:old-password@url:port"}}}}}
          actual (model/delete "a" cofx)]
      (testing "it does not remove the mailserver from the list"
        (is (nil? actual))))))

(deftest upsert-mailserver
  (testing "new mailserver"
    (let [cofx {:random-id "random-id"
                :db {:mailservers/manage {:name {:value "test-name"}
                                          :url  {:value "enode://test-id:test-password@url:port"}}

                     :inbox/wnodes {}}}
          actual (model/upsert cofx)]

      (testing "it adds the enode to inbox/wnodes"
        (is (= {:eth.beta {"randomid" {:password "test-password"
                                       :address "enode://test-id@url:port"
                                       :name "test-name"
                                       :id "randomid"
                                       :user-defined true}}}
               (get-in actual [:db :inbox/wnodes]))))
      (testing "it navigates back"
        (is (= [:navigate-back]
               (:dispatch actual))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))))
  (testing "existing mailserver"
    (let [cofx {:random-id "random-id"
                :db {:mailservers/manage {:id   {:value "a"}
                                          :name {:value "new-name"}
                                          :url  {:value "enode://new-id:new-password@url:port"}}

                     :inbox/wnodes {:eth.beta {"a" {:id      "a"
                                                    :name    "old-name"
                                                    :address "enode://old-id:old-password@url:port"}}}}}
          actual (model/upsert cofx)]
      (testing "it navigates back"
        (is (= [:navigate-back]
               (:dispatch actual))))
      (testing "it updates the enode to inbox/wnodes"
        (is (= {:eth.beta {"a" {:password "new-password"
                                :address "enode://new-id@url:port"
                                :name "new-name"
                                :id "a"
                                :user-defined true}}}
               (get-in actual [:db :inbox/wnodes]))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))
      (testing "it logs the user out if connected to the current mailserver"
        (let [actual (model/upsert (assoc-in cofx
                                             [:db :inbox/current-id] "a"))]
          (is (= [:logout]
                 (-> actual :data-store/tx first :success-event))))))))
