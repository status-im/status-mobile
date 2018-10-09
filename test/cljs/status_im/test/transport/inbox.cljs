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

#_(deftest request-messages
    (let [db {:mailserver-status :connected
              :inbox/current-id "wnodeid"
              :inbox/wnodes {:eth.beta {"wnodeid" {:address    "wnode-address"
                                                   :sym-key-id "something"
                                                   :password   "wnode-password"}}}
              :account/account {:settings {:fleet :eth.beta}}
              :transport/chats
              {:dont-fetch-history {:topic "dont-fetch-history"}
               :fetch-history      {:topic "fetch-history"}}}
          cofx {:db db :now 1000000000}]
      (testing "inbox is ready"
        (testing "last request is > the 7 days ago"
          (let [cofx-with-last-request (assoc-in cofx [:db :account/account :last-request] 400000)
                actual (inbox/request-messages cofx-with-last-request nil)]
            (testing "it uses last request"
              (is (= 400000 (get-in actual [:transport.inbox/request-messages :requests]))))))
        (testing "last request is < the 7 days ago"
          (let [cofx-with-last-request (assoc-in cofx [:db :account/account :last-request] 2)
                actual (inbox/request-messages cofx-with-last-request nil)]
            (testing "it uses last 7 days for catching up"
              (is (= 395200 (get-in actual [:transport.inbox/request-messages :requests]))))
            (testing "it only uses topics that dont have fetch history set"
              (is (= ["0xf8946aac" "dont-fetch-history"]
                     (get-in actual [:transport.inbox/request-messages :requests]))))
            (testing "it uses the last 24 hours to request history"
              (is (= 913600
                     (get-in actual [:transport.inbox/request-messages :requests]))))
            (testing "it fetches the right topic for history"
              (is (= ["fetch-history"]
                     (get-in actual [:transport.inbox/request-messages :requests])))))))
      (testing "inbox is not ready"
        (testing "it does not do anything"
          (is (nil? (inbox/request-messages {:db {}} nil)))))))

#_(deftest initialize-offline-inbox
    (let [db {:mailserver-status :connected
              :account/account {:settings {:fleet :eth.beta}}
              :inbox/current-id "wnodeid"
              :inbox/wnodes {:eth.beta {"wnodeid" {:address    "wnode-address"
                                                   :sym-key-id "something"
                                                   :password   "wnode-password"}}}}]
      (testing "last-request is not set"
        (testing "it sets it to now in seconds"
          (is (= 10
                 (get-in
                  (inbox/initialize-offline-inbox {:now 10000 :db db} [])
                  [:db :account/account :last-request])))))
      (testing "last-request is set"
        (testing "leaves it unchanged"
          (is (= "sometimeago"
                 (get-in
                  (inbox/initialize-offline-inbox
                   {:now "now"
                    :db (assoc-in db [:account/account :last-request] "sometimeago")}
                   [])
                  [:db :account/account :last-request])))))))
