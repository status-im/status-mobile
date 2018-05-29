(ns status-im.test.offline-messaging-settings.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.offline-messaging-settings.edit-mailserver.events :as events]))

(def valid-enode-url "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40:somepassword@167.99.209.61:30504")

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

(deftest save-new-mailserver
  (testing "save a new mailserver"
    (let [cofx {:random-id "random-id"
                :db {:network "mainnet_rpc"
                     :account/account {:networks {"mainnet_rpc"
                                                  {:config {:NetworkId 1}}}}
                     :mailservers/manage {:name {:value "test-name"}
                                          :url  {:value "enode://test-id:test-password@url:port"}}

                     :inbox/wnodes {}}}
          actual (events/save-new-mailserver cofx nil)]
      (testing "it adds the enode to inbox/wnodes"
        (is (= {:mainnet {"randomid" {:password "test-password"
                                      :address "enode://test-id@url:port"
                                      :name "test-name"
                                      :id "randomid"
                                      :user-defined true}}}
               (get-in actual [:db :inbox/wnodes]))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual))))))))
