(ns status-im.test.transport.inbox
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.inbox :as inbox]
            [status-im.constants :as constants]))

(defn cofx-fixtures [sym-key registered-peer?]
  {:db {:mailserver-status :connected
        :network "mainnet_rpc"
        :peers-summary (if registered-peer?
                         [{:id "wnode-id"}]
                         [])
        :account/account {:networks constants/default-networks}
        :inbox/current-id "mailserver-a"
        :inbox/wnodes {:mainnet {"mailserver-a" {:sym-key-id sym-key
                                                 :address "enode://wnode-id@ip"}}}}})

(defn peers-summary-change-fx-result [sym-key registered-peer? registered-peer-before?]
  (inbox/peers-summary-change-fx (if registered-peer-before?
                                   [{:id "wnode-id"}]
                                   [])
                                 (cofx-fixtures sym-key
                                                registered-peer?)))

(deftest peers-summary-change-fx
  (testing "Mailserver connected"
    (let [result (peers-summary-change-fx-result false true false)]
      (is (= (into #{} (keys result))
             #{:status-im.transport.inbox/mark-trusted-peer}))))
  (testing "Mailserver disconnected, sym-key exists"
    (let [result (peers-summary-change-fx-result true false true)]
      (is (= (into #{} (keys result))
             #{:db :status-im.transport.inbox/add-peer :utils/dispatch-later}))
      (is (= (get-in result [:db :mailserver-status])
             :connecting))))
  (testing "Mailserver disconnected, sym-key doesn't exists (unlikely situation in practice)"
    (let [result (peers-summary-change-fx-result false false true)]
      (is (= (into #{} (keys result))
             #{:db :status-im.transport.inbox/add-peer :utils/dispatch-later  :shh/generate-sym-key-from-password}))
      (is (= (get-in result [:db :mailserver-status])
             :connecting))))
  (testing "Mailserver isn't concerned by peer summary changes"
    (is (= (into #{} (keys (peers-summary-change-fx-result true true true)))
           #{}))
    (is (= (into #{} (keys (peers-summary-change-fx-result true false false)))
           #{}))))

(deftest connect-to-mailserver
  (let [db {:network "mainnet"
            :inbox/current-id "wnodeid"
            :inbox/wnodes
            {:mainnet {"wnodeid" {:address  "wnode-address"
                                  :password "wnode-password"}}}
            :account/account
            {:settings {:wnode {:mainnet "wnodeid"}}
             :networks {"mainnet" {:config {:NetworkId 1}}}}}]
    (testing "it adds the peer"
      (is (= {:wnode "wnode-address"}
             (::inbox/add-peer (inbox/connect-to-mailserver {:db db})))))
    (testing "it generates a sym key if hasn't been generated before"
      (is (= "wnode-password"
             (-> (inbox/connect-to-mailserver {:db db})
                 :shh/generate-sym-key-from-password
                 :password))))
    (let [wnode-with-sym-key-db (assoc-in db
                                          [:inbox/wnodes :mainnet "wnodeid" :sym-key-id]
                                          "somesymkeyid")]
      (testing "it does not generate a sym key if already present"
        (is (not (-> (inbox/connect-to-mailserver {:db wnode-with-sym-key-db})
                     :shh/generate-sym-key-from-password)))))))

(deftest request-messages
  (let [db {:network "mainnet"
            :mailserver-status :connected
            :inbox/current-id "wnodeid"
            :inbox/wnodes
            {:mainnet {"wnodeid" {:address    "wnode-address"
                                  :sym-key-id "something"
                                  :password   "wnode-password"}}}
            :account/account
            {:networks {"mainnet" {:config {:NetworkId 1}}}}}
        cofx {:db db :now 1000000000}]
    (testing "inbox is ready"
      (testing "last-request is set"
        (let [cofx-with-last-request (assoc-in cofx [:db :account/account :last-request] 2)
              actual (inbox/request-messages cofx-with-last-request)]
          (testing "it uses last request"
            (is (= 2 (get-in actual [::inbox/request-messages :from]))))))
      (testing "last-request is not set"
        (let [actual (inbox/request-messages cofx)]
          (testing "it defaults to the last 7 days"
            (is (= 395200 (get-in actual [::inbox/request-messages :from]))))))
      (testing "last-request is nil"
        (let [cofx-with-last-request (assoc-in cofx [:db :account/account :last-request] nil)
              actual (inbox/request-messages cofx-with-last-request)]
          (testing "it defaults to the last 7 days"
            (is (= 395200 (get-in actual [::inbox/request-messages :from])))))))
    (testing "inbox is not ready"
      (testing "it does not do anything"
        (is (nil? (inbox/request-messages {})))))))
