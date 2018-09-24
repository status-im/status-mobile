(ns status-im.test.node.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.config :as config]
            [status-im.node.core :as node]))

(defn- parse-node-config [config]
  (-> config
      :node/start
      (js/JSON.parse)
      (js->clj :keywordize-keys true)))

(deftest start-test
  (let [address "a"
        cofx {:db {:accounts/accounts {address {:installation-id "id"}}}}]
    (testing "installation-id"
      (testing "the user is not logged in"
        (let [actual (parse-node-config (node/start cofx nil))]
          (is (not (:InstallationID actual)))))
      (testing "the user is logged in"
        (let [actual (parse-node-config (node/start cofx address))]
          (is (= "id" (:InstallationID actual))))))
    (testing "pfs & group chats disabled"
      (with-redefs [config/pfs-encryption-enabled? false
                    config/group-chats-enabled? false]
        (testing "the user is not logged in"
          (let [actual (parse-node-config (node/start cofx nil))]
            (is (not (:PFSEnabled actual)))))
        (testing "the user is logged in"
          (let [actual (parse-node-config (node/start cofx address))]
            (is (not (:PFSEnabled actual))))))
      (testing "pfs is enabled"
        (with-redefs [config/pfs-encryption-enabled? true]
          (testing "the user is not logged in"
            (let [actual (parse-node-config (node/start cofx nil))]
              (is (not (:PFSEnabled actual)))))
          (testing "the user is logged in"
            (let [actual (parse-node-config (node/start cofx address))]
              (is (:PFSEnabled actual))))))
      (testing "group chats is enabled"
        (with-redefs [config/group-chats-enabled? true]
          (testing "the user is not logged in"
            (let [actual (parse-node-config (node/start cofx nil))]
              (is (not (:PFSEnabled actual)))))
          (testing "the user is logged in"
            (let [actual (parse-node-config (node/start cofx address))]
              (is (:PFSEnabled actual)))))))))
