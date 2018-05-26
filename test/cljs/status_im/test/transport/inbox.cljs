(ns status-im.test.transport.inbox
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.inbox :as inbox]
            [status-im.constants :as constants]))

(deftest request-messages
  (testing "mailserver not connected"
    (is (= (inbox/request-messages {} {:db {:mailserver-status :connecting :inbox/sym-key-id :sym-key}})
           {:db
            {:mailserver-status :connecting,
             :inbox/sym-key-id :sym-key,
             :inbox/topics #{"0xf8946aac"}}}))
    (is (= (inbox/request-messages {:discover? false :topics ["Oxaaaaaaaa"]} {:db {:mailserver-status :connecting :inbox/sym-key-id :sym-key}})
           {:db
            {:mailserver-status :connecting,
             :inbox/sym-key-id :sym-key,
             :inbox/topics #{"Oxaaaaaaaa"}}})))
  (testing "mailserver is connected"
    (is (= (inbox/request-messages {} {:db {:mailserver-status :connected :inbox/sym-key-id :sym-key}})
           {:status-im.transport.inbox/request-messages
            {:wnode nil, :topics ["0xf8946aac"], :sym-key-id :sym-key, :web3 nil},
            :db
            {:mailserver-status :connected,
             :inbox/sym-key-id :sym-key,
             :inbox/fetching? true,
             :inbox/topics #{}},
            :dispatch-later
            [{:ms 5000, :dispatch [:inbox/remove-fetching-notification]}]})
        (= (inbox/request-messages {:discover? false :topics ["Oxaaaaaaaa"]} {:db {:mailserver-status :connected :inbox/sym-key-id :sym-key}})
           {:status-im.transport.inbox/request-messages
            {:wnode nil, :topics ["Oxaaaaaaaa"], :sym-key-id :sym-key, :web3 nil},
            :db
            {:mailserver-status :connected,
             :inbox/sym-key-id :sym-key,
             :inbox/fetching? true,
             :inbox/topics #{}},
            :dispatch-later
            [{:ms 5000, :dispatch [:inbox/remove-fetching-notification]}]}))))

(defn cofx-fixtures [sym-key registered-peer?]
  {:db {:mailserver-status :connected
        :inbox/sym-key-id sym-key
        :network "mainnet_rpc"
        :peers-summary (if registered-peer?
                         [{:id "wnode-id"}]
                         [])
        :account/account {:networks constants/default-networks
                          :settings {:wnode {:mainnet "mailserver-a"}}}
        :inbox/wnodes {:mainnet {"mailserver-a" {:address "enode://wnode-id@ip"}}}}})

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
