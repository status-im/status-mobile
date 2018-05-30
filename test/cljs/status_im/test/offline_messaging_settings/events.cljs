(ns status-im.test.offline-messaging-settings.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.offline-messaging-settings.edit-mailserver.events :as events]))

(def enode-id "1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40")
(def password "password")
(def host "167.99.209.61:30504")

(def valid-enode-address (str "enode://" enode-id "@" host))
(def valid-enode-url (str "enode://" enode-id ":" password "@" host))

(deftest set-input
  (testing "it validates names"
    (testing "correct name"
      (is (= {:db {:mailservers/manage {:name {:value "value"
                                               :error false}}}}
             (events/set-input :name "value" {}))))
    (testing "blank name"
      (is (= {:db {:mailservers/manage {:name {:value ""
                                               :error true}}}}
             (events/set-input :name "" {})))))
  (testing "it validates enodes url"
    (testing "correct url"
      (is (= {:db {:mailservers/manage {:url {:value valid-enode-url
                                              :error false}}}}
             (events/set-input :url valid-enode-url {}))))
    (testing "broken url"
      (is (= {:db {:mailservers/manage {:url {:value "broken"
                                              :error true}}}}
             (events/set-input :url "broken" {}))))))

(deftest edit-mailserver
  (let [db {:network "mainnet_rpc"
            :account/account {:networks {"mainnet_rpc"
                                         {:config {:NetworkId 1}}}}
            :inbox/wnodes
            {:mainnet {"a" {:id      "a"
                            :address valid-enode-address
                            :password password
                            :name    "name"}}}}
        cofx {:db db}]
    (testing "when no id is given"
      (let [actual (events/edit-mailserver nil cofx)]
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
        (let [actual (events/edit-mailserver "a" cofx)]
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
        (let [actual (events/edit-mailserver "not-existing" cofx)]
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

(deftest upsert-mailserver
  (testing "new mailserver"
    (let [cofx {:random-id "random-id"
                :db {:network "mainnet_rpc"
                     :account/account {:networks {"mainnet_rpc"
                                                  {:config {:NetworkId 1}}}}
                     :mailservers/manage {:name {:value "test-name"}
                                          :url  {:value "enode://test-id:test-password@url:port"}}

                     :inbox/wnodes {}}}
          actual (events/upsert-mailserver cofx nil)]

      (testing "it adds the enode to inbox/wnodes"
        (is (= {:mainnet {"randomid" {:password "test-password"
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
                :db {:network "mainnet_rpc"
                     :account/account {:networks {"mainnet_rpc"
                                                  {:config {:NetworkId 1}}}}
                     :mailservers/manage {:id   {:value "a"}
                                          :name {:value "new-name"}
                                          :url  {:value "enode://new-id:new-password@url:port"}}

                     :inbox/wnodes {:mainnet {"a" {:id      "a"
                                                   :name    "old-name"
                                                   :address "enode://old-id:old-password@url:port"}}}}}
          actual (events/upsert-mailserver cofx nil)]
      (testing "it navigates back"
        (is (= [:navigate-back]
               (:dispatch actual))))
      (testing "it updates the enode to inbox/wnodes"
        (is (= {:mainnet {"a" {:password "new-password"
                               :address "enode://new-id@url:port"
                               :name "new-name"
                               :id "a"
                               :user-defined true}}}
               (get-in actual [:db :inbox/wnodes]))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))
      (testing "it logs the user out if connected to the current mailserver"
        (let [actual (events/upsert-mailserver (assoc-in cofx
                                                         [:db :account/account :settings]
                                                         {:wnode
                                                          {:mainnet "a"}}) nil)]
          (is (= [:logout]
                 (-> actual :data-store/tx first :success-event))))))))
