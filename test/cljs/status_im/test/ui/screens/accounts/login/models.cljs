(ns status-im.test.ui.screens.accounts.login.models
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.config :as config]
            [status-im.ui.screens.accounts.login.models :as models]))

(deftest login-account-internal
  (let [initial-db {:db {:node/after-start "something"}}]
    (defn test-func [save-password]
      (testing (str "login-account-interal test, save password: " save-password)
        (let [actual (models/login-account-internal "address" "password" save-password initial-db)]
          (testing "it resets :node/after-start"
            (is (= (get-in actual [:db :node/after-start]) nil))
            (testing "it causes :login effect and preserves save-password flag"
              (is (= ["address" "password" save-password] (get-in actual [:login]))))))))
    (map test-func [true false])))

(deftest login-account
  (let [mainnet-account {:network "mainnet_rpc"
                         :networks {"mainnet_rpc" {:config  {:NetworkId 1}}}}
        testnet-account {:network "testnet_rpc"
                         :networks {"testnet_rpc" {:config  {:NetworkId 3}}}}
        accounts        {"mainnet" mainnet-account
                         "testnet" testnet-account}
        initial-db      {:db {:network "mainnet_rpc"
                              :accounts/accounts accounts}}]

    (testing "save password"
      (testing "save password: status-go not started"
        (let [actual (models/login-account "testnet" "password" true initial-db)]
          (testing "it starts status-node if it has not started"
            (is (= {:NetworkId 3} (:initialize-geth-fx actual))))
          (testing "it logins the user after the node started"
            (is (= [:login-account-internal "testnet" "password" true] (get-in actual [:db :node/after-start]))))))
      (testing "status-go has started & the user is on mainnet"
        (let [db (assoc-in initial-db [:db :status-node-started?] true)
              actual (models/login-account "mainnet" "password" false db)]
          (testing "it does not start status-node if it has already started"
            (is (not (:initialize-geth-fx actual))))
          (testing "it logs in the user"
            (is (= ["mainnet" "password" false] (:login actual))))))

      (testing "the user has selected a different network"
        (testing "status-go has started"
          (let [db     (assoc-in initial-db [:db :status-node-started?] true)
                actual (models/login-account "testnet" "password" false db)]
            (testing "it dispatches start-node"
              (is (get-in actual [:db :node/after-stop] [:start-node "testnet" "password" true])))
            (testing "it stops status-node"
              (is (contains? actual :stop-node))))))

      (testing "status-go has not started"
        (let [actual (models/login-account "testnet" "password" false initial-db)]
          (testing "it starts status-node if it has not started"
            (is (= {:NetworkId 3} (:initialize-geth-fx actual))))
          (testing "it logins the user after the node started"
            (is (= [:login-account-internal "testnet" "password" false] (get-in actual [:db :node/after-start]))))))

      (testing "status-go has started & the user is on mainnet"
        (let [db (assoc-in initial-db [:db :status-node-started?] true)
              actual (models/login-account "mainnet" "password" false db)]
          (testing "it does not start status-node if it has already started"
            (is (not (:initialize-geth-fx actual))))
          (testing "it logs in the user"
            (is (= ["mainnet" "password" false] (:login actual))))))

      (testing "the user has selected a different network"
        (testing "status-go has started"
          (let [db     (assoc-in initial-db [:db :status-node-started?] true)
                actual (models/login-account "testnet" "password" false db)]
            (testing "it dispatches start-node"
              (is (get-in actual [:db :node/after-stop] [:start-node "testnet" "password" false])))
            (testing "it stops status-node"
              (is (contains? actual :stop-node)))))

        (testing "status-go has not started"
          (let [actual (models/login-account "testnet" "password" false initial-db)]
            (testing "it starts status-node"
              (is (= {:NetworkId 3} (:initialize-geth-fx actual))))
            (testing "it logins the user after the node started"
              (is (= [:login-account-internal "testnet" "password" false] (get-in actual [:db :node/after-start])))))))

      (testing "custom bootnodes"
        (let [custom-bootnodes {"a" {:id "a"
                                     :name "name-a"
                                     :address "address-a"}
                                "b" {:id "b"
                                     :name "name-b"
                                     :address "address-b"}}
              bootnodes-db     (assoc-in
                                initial-db
                                [:db :accounts/accounts "mainnet" :bootnodes]
                                {"mainnet_rpc" custom-bootnodes})]

          (testing "custom bootnodes enabled"
            (let [bootnodes-enabled-db (assoc-in
                                        bootnodes-db
                                        [:db :accounts/accounts "mainnet" :settings]
                                        {:bootnodes {"mainnet_rpc" true}})
                  actual (models/login-account "mainnet" "password" false bootnodes-enabled-db)]
              (testing "status-node has started"
                (let [db (assoc-in bootnodes-enabled-db [:db :status-node-started?] true)
                      actual (models/login-account "mainnet" "password" false db)]
                  (testing "it dispatches start-node"
                    (is (get-in actual [:db :node/after-stop] [:start-node "testnet" "password" false])))
                  (testing "it stops status-node"
                    (is (contains? actual :stop-node)))))
              (testing "status-node has not started"
                (let [actual (models/login-account "mainnet" "password" false bootnodes-enabled-db)]
                  (testing "it adds bootnodes to the config"
                    (is (= {:ClusterConfig {:Enabled   true
                                            :BootNodes ["address-a" "address-b"]}
                            :NetworkId 1} (:initialize-geth-fx actual))))
                  (testing "it logins the user after the node started"
                    (is (= [:login-account-internal "mainnet" "password" false] (get-in actual [:db :node/after-start]))))))))

          (testing "custom bootnodes not enabled"
            (testing "status-node has started"
              (let [db (assoc-in bootnodes-db [:db :status-node-started?] true)
                    actual (models/login-account "mainnet" "password" false db)]
                (testing "it does not start status-node if it has already started"
                  (is (not (:initialize-geth-fx actual))))
                (testing "it logs in the user"
                  (is (= ["mainnet" "password" false] (:login actual))))))
            (testing "status-node has not started"
              (let [actual (models/login-account "mainnet" "password" false bootnodes-db)]
                (testing "it starts status-node without custom bootnodes"
                  (is (= {:NetworkId 1} (:initialize-geth-fx actual))))
                (testing "it logins the user after the node started"
                  (is (= [:login-account-internal "mainnet" "password" false] (get-in actual [:db :node/after-start]))))))))))))

(deftest restart-node?
  (testing "custom bootnodes is toggled off"
    (with-redefs [config/bootnodes-settings-enabled? false]
      (testing "it returns true when the network is different"
        (is (models/restart-node? "mainnet_rpc" "mainnet" true)))
      (testing "it returns false when the network is the same"
        (is (not (models/restart-node? "mainnet" "mainnet" true))))))
  (testing "custom bootnodes is toggled on"
    (with-redefs [config/bootnodes-settings-enabled? true]
      (testing "the user is not using custom bootnodes"
        (testing "it returns true when the network is different"
          (is (models/restart-node? "mainnet_rpc" "mainnet" false)))
        (testing "it returns false when the network is the same"
          (is (not (models/restart-node? "mainnet" "mainnet" false)))))
      (testing "the user is using custom bootnodes"
        (testing "it returns true when the network is different"
          (is (models/restart-node? "mainnet" "mainnet" true)))
        (testing "it returns true when the network is the same"
          (is (models/restart-node? "mainnet_rpc" "mainnet" true)))))))
