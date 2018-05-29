(ns status-im.test.offline-messaging-settings.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.offline-messaging-settings.edit-mailserver.events :as events]))

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
