(ns status-im.subs.wallet.send-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.contexts.wallet.common.activity-tab.constants :as constants]
    [status-im.subs.root]
    [status-im.subs.wallet.send]
    [test-helpers.unit :as h]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(def ^:private token-mock
  {:decimals                   8
   :symbol                     "ETH"
   :balances-per-chain         {1 {:raw-balance "100"}}
   :market-values-per-currency {:usd {:price 10000}}})

(h/deftest-sub :wallet/send-tab
  [sub-name]
  (testing "returns active tab for selecting address"
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :select-address-tab] :tabs/recent)
    (is (= :tabs/recent (rf/sub [sub-name])))))

(h/deftest-sub :wallet/send-transaction-ids
  [sub-name]
  (testing "returns the transaction ids attached the last send flow"
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :transaction-ids] ["0x123" "0x321"])
    (is (= ["0x123" "0x321"] (rf/sub [sub-name])))))

(h/deftest-sub :wallet/send-transaction-progress
  [sub-name]
  (testing "returns transaction data for a transaction with multiple transactions"
    (swap! rf-db/app-db assoc-in
      [:wallet :transactions]
      {"0x123" {:status   :pending
                :id       240
                :chain-id 5}
       "0x321" {:status   :pending
                :id       240
                :chain-id 1}})
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :transaction-ids] ["0x123" "0x321"])
    (is (= {"0x123" {:status   :pending
                     :id       240
                     :chain-id 5}
            "0x321" {:status   :pending
                     :id       240
                     :chain-id 1}}
           (rf/sub [sub-name]))))

  (testing "returns transaction data for a transaction with a single transaction"
    (swap! rf-db/app-db assoc-in
      [:wallet :transactions]
      {"0x123" {:status   :pending
                :id       100
                :chain-id 5}
       "0x321" {:status   :pending
                :id       240
                :chain-id 1}})
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :transaction-ids] ["0x123"])
    (is (= {"0x123" {:status   :pending
                     :id       100
                     :chain-id 5}}
           (rf/sub [sub-name])))))

(h/deftest-sub :wallet/recent-recipients
  [sub-name]
  (testing "returns recent tab for selecting address"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :activities]
                      {"acc1" {1 {:activity-type constants/wallet-activity-type-send
                                  :amount-out    "0x1"
                                  :sender        "acc1"
                                  :recipient     "acc2"
                                  :timestamp     1588291200}
                               2 {:activity-type constants/wallet-activity-type-receive
                                  :amount-in     "0x1"
                                  :sender        "acc2"
                                  :recipient     "acc1"
                                  :timestamp     1588377600}
                               3 {:activity-type constants/wallet-activity-type-send
                                  :amount-out    "0x1"
                                  :sender        "acc1"
                                  :recipient     "acc4"
                                  :timestamp     1588464000}}
                       "acc3" {4 {:activity-type constants/wallet-activity-type-receive
                                  :amount-in     "0x1"
                                  :sender        "acc4"
                                  :recipient     "acc3"
                                  :timestamp     1588464000}}})
            (assoc-in [:wallet :current-viewing-account-address] "acc1"))))
    (is (match? ["acc2" "acc4"] (rf/sub [sub-name])))))

(h/deftest-sub :wallet/send-token-decimals
  [sub-name]
  (testing "returns the decimals for the chosen token"
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :token] token-mock)
    (is (= 8 (rf/sub [sub-name]))))

  (testing "returns 0 if sending collectibles"
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :collectible] {})
    (is (match? 0 (rf/sub [sub-name])))))

(h/deftest-sub :wallet/send-display-token-decimals
  [sub-name]
  (testing "returns the decimals based on the token price for the chosen token"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :send :token]
      {:symbol                     "ETH"
       :balances-per-chain         {1 {:raw-balance "100"}}
       :market-values-per-currency {:usd {:price 10000}}})
    (is (match? 6 (rf/sub [sub-name]))))

  (testing "returns 0 if sending collectibles"
    (swap! rf-db/app-db assoc-in [:wallet :ui :send :collectible] {})
    (is (match? 0 (rf/sub [sub-name])))))

(h/deftest-sub :wallet/send-native-token?
  [sub-name]
  (testing "returns true if the chosen token is native (ETH)"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :ui :send :token] {})
            (assoc-in [:wallet :ui :send :token-display-name] "ETH"))))
    (is (rf/sub [sub-name])))

  (testing "returns false if the chosen token is not native"
    (swap! rf-db/app-db
      (swap! rf-db/app-db
        (fn [db]
          (-> db
              (assoc-in [:wallet :ui :send :token] {})
              (assoc-in [:wallet :ui :send :token-display-name] "SNT")))))
    (is (not (rf/sub [sub-name])))))

(h/deftest-sub :wallet/total-amount
  [sub-name]
  (testing "returns the total SEND amount when the route is present"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :ui :send :token] token-mock)
            (assoc-in [:wallet :ui :send :token-display-name] "ETH")
            (assoc-in [:wallet :ui :send :route]
                      [{:amount-out "0x1bc16d674ec80000"
                        :to         {:chain-id 1}}]))))
    (is (match? (money/bignumber 2) (rf/sub [sub-name]))))

  (testing "returns the total BRIDGE amount when the route is present"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :ui :send :token] token-mock)
            (assoc-in [:wallet :ui :send :token-display-name] "ETH")
            (assoc-in [:wallet :ui :send :route]
                      [{:bridge-name "Hop"
                        :amount-in   "0xde0b6b3a7640000"
                        :token-fees  (money/bignumber "230000000000000")
                        :to          {:chain-id 1}}]))))
    (is (match? (money/bignumber 0.99977) (rf/sub [sub-name]))))

  (testing "returns the default total amount (0) when the route is not present"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :ui :send :token] token-mock)
            (assoc-in [:wallet :ui :send :token-display-name] "ETH"))))
    (is (match? (money/bignumber 0) (rf/sub [sub-name])))))

(h/deftest-sub :wallet/send-total-amount-formatted
  [sub-name]
  (testing "returns the formatted total amount"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :ui :send :token] token-mock)
            (assoc-in [:wallet :ui :send :token-display-name] "ETH")
            (assoc-in [:wallet :ui :send :route]
                      [{:amount-out "0x2b139f68a611c00" ;; 193999990000000000
                        :to         {:chain-id 1}}]))))
    (is (match? "0.194 ETH" (rf/sub [sub-name])))))

(h/deftest-sub :wallet/send-amount-fixed
  [sub-name]
  (testing "returns the fixed value when the amount is a string"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :ui :send :token] token-mock))))
    (is (match? "2" (rf/sub [sub-name "1.9999999"]))))

  (testing "returns the fixed value when the amount is a number"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :ui :send :token] token-mock))))
    (is (match? "2" (rf/sub [sub-name 1.9999999]))))

  (testing "returns the fixed value when the amount is a bignumber"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :ui :send :token] token-mock))))
    (is (match? "2" (rf/sub [sub-name (money/bignumber "1.9999999")])))))

(def ^:private mainnet-network
  {:short-name       "eth"
   :network-name     :mainnet
   :abbreviated-name "Eth."
   :full-name        "Mainnet"
   :chain-id         1
   :related-chain-id 1
   :layer            1})

(h/deftest-sub :wallet/bridge-to-network-details
  [sub-name]
  (testing "returns the network details for bridge transactions"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :networks] {:prod [mainnet-network]})
            (assoc-in [:profile/profile :test-networks-enabled?] false)
            (assoc-in [:wallet :ui :send] {:bridge-to-chain-id 1}))))
    (is (match? mainnet-network (rf/sub [sub-name]))))

  (testing "returns nil if not on the bridge flow"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in [:wallet :networks] {:prod [mainnet-network]})
            (assoc-in [:profile/profile :test-networks-enabled?] false))))
    (is (match? nil (rf/sub [sub-name])))))
