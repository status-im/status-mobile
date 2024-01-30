(ns legacy.status-im.bootnodes.core-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.bootnodes.core :as model]))

(def bootnode-id
  "1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40")
(def host "167.99.209.61:30504")

(def valid-bootnode-address (str "enode://" bootnode-id "@" host))

(deftest upsert-bootnode
  (testing "adding a new bootnode"
    (let [new-bootnode {:name {:value "name"}
                        :url  {:value "url"}}
          expected     {"mainnet_rpc" {"someid" {:name    "name"
                                                 :address "url"
                                                 :chain   "mainnet_rpc"
                                                 :id      "someid"}}}
          actual       (model/upsert
                        {:random-id-generator (constantly "some-id")
                         :db                  {:bootnodes/manage new-bootnode
                                               :networks/current-network "mainnet_rpc"
                                               :profile/profile {:not-empty
                                                                 "would throw an error if was empty"}}})]
      (is (= expected (get-in actual [:db :profile/profile :custom-bootnodes])))))
  (testing "adding an existing bootnode"
    (let [new-bootnode {:id   {:value "a"}
                        :name {:value "new-name"}
                        :url  {:value "new-url"}}
          expected     {"mainnet_rpc" {"a" {:name    "new-name"
                                            :address "new-url"
                                            :chain   "mainnet_rpc"
                                            :id      "a"}}}
          actual       (model/upsert
                        {:random-id-generator (constantly "some-id")
                         :db                  {:bootnodes/manage         new-bootnode
                                               :networks/current-network "mainnet_rpc"
                                               :profile/profile          {:custom-bootnodes
                                                                          {"mainnet_rpc"
                                                                           {"a" {:name    "name"
                                                                                 :address "url"
                                                                                 :chain   "mainnet_rpc"
                                                                                 :id      "a"}}}}}})]
      (is (= expected (get-in actual [:db :profile/profile :custom-bootnodes]))))))

(deftest set-input-bootnode
  (testing "it validates names"
    (testing "correct name"
      (is (= {:db {:bootnodes/manage {:name {:value "value"
                                             :error false}}}}
             (model/set-input {:db {}} :name "value"))))
    (testing "blank name"
      (is (= {:db {:bootnodes/manage {:name {:value ""
                                             :error true}}}}
             (model/set-input {:db {}} :name "")))))
  (testing "it validates bootnode url"
    (testing "correct url"
      (is (= {:db {:bootnodes/manage {:url {:value valid-bootnode-address
                                            :error false}}}}
             (model/set-input {:db {}} :url valid-bootnode-address))))
    (testing "broken url"
      (is (= {:db {:bootnodes/manage {:url {:value "broken"
                                            :error true}}}}
             (model/set-input {:db {}} :url "broken"))))))

(deftest set-bootnode-from-qr
  (testing "correct name"
    (is (= {:dispatch [:navigate-back]
            :db       {:bootnodes/manage {:url {:value valid-bootnode-address
                                                :error false}}}}
           (model/set-bootnodes-from-qr {:db {}} (str valid-bootnode-address "   "))))))

(deftest edit-bootnode
  (let [db   {:networks/current-network "mainnet_rpc"
              :profile/profile
              {:custom-bootnodes
               {"mainnet_rpc"
                {"a" {:id      "a"
                      :name    "name"
                      :address valid-bootnode-address}}}}}
        cofx {:db db}]
    (testing "when no id is given"
      (let [actual (model/edit cofx nil)]
        (testing "it resets :bootnodes/manage"
          (is (= {:id   {:value nil
                         :error false}
                  :url  {:value ""
                         :error true}
                  :name {:value ""
                         :error true}}
                 (-> actual :db :bootnodes/manage))))
        (testing "it navigates to edit-bootnode view"
          (is (= [:navigate-to :edit-bootnode]
                 (-> actual :dispatch))))))
    (testing "when an id is given"
      (testing "when the bootnode is in the list"
        (let [actual (model/edit cofx "a")]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value "a"
                           :error false}
                    :url  {:value valid-bootnode-address
                           :error false}
                    :name {:value "name"
                           :error false}}
                   (-> actual :db :bootnodes/manage))))
          (testing "it navigates to edit-bootnode view"
            (is (= [:navigate-to :edit-bootnode]
                   (-> actual :dispatch))))))
      (testing "when the bootnode is not in the list"
        (let [actual (model/edit cofx "not-existing")]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value nil
                           :error false}
                    :url  {:value ""
                           :error true}
                    :name {:value ""
                           :error true}}
                   (-> actual :db :bootnodes/manage))))
          (testing "it navigates to edit-bootnode view"
            (is (= [:navigate-to :edit-bootnode]
                   (-> actual :dispatch)))))))))

(deftest fetch-bootnode
  (testing "it fetches the bootnode from the db"
    (let [cofx {:db {:networks/current-network "mainnet_rpc"
                     :profile/profile          {:custom-bootnodes
                                                {"mainnet_rpc"
                                                 {"a" {:id "a"
                                                       :name "name"
                                                       :address
                                                       "enode://old-id:old-password@url:port"}}}}}}]
      (is (model/fetch cofx "a")))))

(deftest custom-bootnodes-in-use?
  (testing "is on the same network"
    (testing "it returns false when not enabled"
      (is (not (model/custom-bootnodes-in-use? {:db {:networks/current-network "mainnet_rpc"}}))))
    (testing "it returns true when enabled"
      (is (model/custom-bootnodes-in-use?
           {:db {:networks/current-network "mainnet_rpc"
                 :profile/profile          {:custom-bootnodes-enabled?
                                            {"mainnet_rpc" true}}}}))))
  (testing "is on a different network"
    (testing "it returns false when not enabled"
      (is (not (model/custom-bootnodes-in-use? {:db {:networks/current-network "goerli_rpc"}}))))
    (testing "it returns true when enabled"
      (is (not (model/custom-bootnodes-in-use?
                {:db {:networks/current-network "goerli_rpc"
                      :profile/profile          {:custom-bootnodes-enabled?
                                                 {"mainnnet_rpc" true}}}}))))))

(deftest delete-bootnode
  (testing "non existing bootnode"
    (let [cofx   {:db {:networks/current-network "mainnet_rpc"
                       :profile/profile          {:custom-bootnodes
                                                  {"mainnet_rpc"
                                                   {"a" {:id "a"
                                                         :name "name"
                                                         :address
                                                         "enode://old-id:old-password@url:port"}}}
                                                  :custom-bootnodes-enabled?
                                                  {"mainnnet_rpc" true}}}}
          actual (model/delete cofx "b")]
      (testing "it does not removes the bootnode"
        (is (model/fetch actual "a")))))
  (testing "existing bootnode"
    (let [cofx   {:db {:networks/current-network "mainnet_rpc"
                       :profile/profile          {:custom-bootnodes
                                                  {"mainnet_rpc"
                                                   {"a" {:id "a"
                                                         :name "name"
                                                         :address
                                                         "enode://old-id:old-password@url:port"}}}
                                                  :custom-bootnodes-enabled?
                                                  {"mainnnet_rpc" true}}}}
          actual (model/delete cofx "a")]

      (testing "it removes the bootnode"
        (is (not (model/fetch actual "a")))))))
