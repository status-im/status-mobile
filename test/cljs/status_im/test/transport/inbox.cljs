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

(defn cofx-fixtures [sym-key peers-count registered-peer?]
  {:db {:mailserver-status :connected
        :inbox/sym-key-id sym-key
        :network "mainnet_rpc"
        :peers-count peers-count
        :peers-summary (if registered-peer?
                         [{:id "wnode-id"}]
                         [])
        :account/account {:networks constants/default-networks
                          :settings {:wnode {:mainnet "mailserver-a"}}}
        :inbox/wnodes {:mainnet {"mailserver-a" {:address "enode://wnode-id@ip"}}}}})

(defn peers-summary-change-fx-result [sym-key peers-count registered-peer?]
  (update-in (inbox/peers-summary-change-fx (cofx-fixtures sym-key peers-count registered-peer?))
             [:db] dissoc :account/account :inbox/wnodes :peers-summary :peers-count))

(deftest peers-summary-change-fx
  (testing "Mailserver is connected and sym-key doesn't exists"
    (let [result (peers-summary-change-fx-result false 1 true)]
      (is (= (into #{} (keys result))
             #{:db :status-im.transport.inbox/mark-trusted-peer :shh/generate-sym-key-from-password}))))
  (testing "Mailserver is connected and sym-key exists"
    (let [result (peers-summary-change-fx-result true 1 true)]
      (is (= (into #{} (keys result))
             #{:db :status-im.transport.inbox/mark-trusted-peer}))))
  (testing "Mailserver is not connected and sym-key doesn't exists"
    (let [result (peers-summary-change-fx-result false 1 false)]
      (is (= (into #{} (keys result))
             #{:db :status-im.transport.inbox/add-peer :utils/dispatch-later :shh/generate-sym-key-from-password}))
      (is (= (get-in result [:db :mailserver-status])
             :connecting))))
  (testing "Mailserver is not connected and sym-key exists"
    (let [result (peers-summary-change-fx-result true 1 false)]
      (is (= (into #{} (keys result))
             #{:db :status-im.transport.inbox/add-peer :utils/dispatch-later}))
      (is (= (get-in result [:db :mailserver-status])
             :connecting))))
  (testing "App is not connected to any peer"
    (is (= (get-in (peers-summary-change-fx-result true 0 true)
                   [:db :mailserver-status])
           :disconnected))))
