(ns status-im.test.ui.screens.bootnodes-settings.edit-bootnode.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.bootnodes-settings.edit-bootnode.events :as events]))

(deftest add-new-bootnode
  (testing "adding a bootnode"
    (let [new-bootnode {:name {:value "name"}
                        :url  {:value "url"}}
          expected     {"mainnet_rpc" {"someid" {:name    "name"
                                                 :address "url"
                                                 :chain   "mainnet_rpc"
                                                 :id      "someid"}}}
          actual       (events/save-new-bootnode
                        {:random-id "some-id"
                         :db {:bootnodes/manage new-bootnode
                              :network          "mainnet_rpc"
                              :account/account  {}}}
                        nil)]
      (is (= expected (get-in actual [:db :account/account :bootnodes]))))))
