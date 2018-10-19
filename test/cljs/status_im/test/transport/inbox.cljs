(ns status-im.test.transport.inbox
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.inbox :as inbox]))

(defn cofx-fixtures [sym-key registered-peer?]
  {:db {:mailserver-status :connected
        :peers-summary (if registered-peer?
                         [{:id "wnode-id"}]
                         [])
        :account/account {:settings {:fleet :eth.beta}}
        :inbox/current-id "mailserver-a"
        :inbox/wnodes {:eth.beta {"mailserver-a" {:sym-key-id sym-key
                                                  :address "enode://wnode-id@ip"}}}}})

(defn peers-summary-change-result [sym-key registered-peer? registered-peer-before?]
  (inbox/peers-summary-change (cofx-fixtures sym-key
                                             registered-peer?)
                              (if registered-peer-before?
                                [{:id "wnode-id"}]
                                [])))

(deftest peers-summary-change
  (testing "Mailserver added, sym-key doesn't exist"
    (let [result (peers-summary-change-result false true false)]
      (is (= (into #{} (keys result))
             #{:transport.inbox/mark-trusted-peer :shh/generate-sym-key-from-password :db}))))
  (testing "Mailserver disconnected, sym-key exists"
    (let [result (peers-summary-change-result true false true)]
      (is (= (into #{} (keys result))
             #{:db :transport.inbox/add-peer :utils/dispatch-later}))
      (is (= (get-in result [:db :mailserver-status])
             :connecting))))
  (testing "Mailserver disconnected, sym-key doesn't exists (unlikely situation in practice)"
    (let [result (peers-summary-change-result false false true)]
      (is (= (into #{} (keys result))
             #{:db :transport.inbox/add-peer :utils/dispatch-later  :shh/generate-sym-key-from-password}))
      (is (= (get-in result [:db :mailserver-status])
             :connecting))))
  (testing "Mailserver isn't concerned by peer summary changes"
    (is (= (into #{} (keys (peers-summary-change-result true true true)))
           #{}))
    (is (= (into #{} (keys (peers-summary-change-result true false false)))
           #{}))))

(deftest connect-to-mailserver
  (let [db {:inbox/current-id "wnodeid"
            :inbox/wnodes
            {:eth.beta {"wnodeid" {:address  "wnode-address"
                                   :password "wnode-password"}}}
            :account/account
            {:settings {:fleet :eth.beta
                        :wnode {:eth.beta "wnodeid"}}}}]
    (testing "it adds the peer"
      (is (= "wnode-address"
             (:transport.inbox/add-peer (inbox/connect-to-mailserver {:db db})))))
    (testing "it generates a sym key if hasn't been generated before"
      (is (= "wnode-password"
             (-> (inbox/connect-to-mailserver {:db db})
                 :shh/generate-sym-key-from-password
                 first
                 :password))))
    (let [wnode-with-sym-key-db (assoc-in db
                                          [:inbox/wnodes :eth.beta "wnodeid" :sym-key-id]
                                          "somesymkeyid")]
      (testing "it does not generate a sym key if already present"
        (is (not (-> (inbox/connect-to-mailserver {:db wnode-with-sym-key-db})
                     :shh/generate-sym-key-from-password
                     first)))))))
