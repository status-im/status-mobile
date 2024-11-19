(ns status-im.subs.wallet.swap-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.subs.root]
    [status-im.subs.wallet.collectibles]
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def ^:private currencies
  {:usd {:id         :usd
         :short-name "USD"
         :symbol     "$"
         :emoji      "🇺🇸"
         :name       "US Dollar"
         :popular?   true
         :token?     false}})

(def ^:private accounts-with-tokens
  {:0x1 {:tokens                    [{:symbol                     "ETH"
                                      :balances-per-chain         {1 {:raw-balance "100"}}
                                      :market-values-per-currency {:usd {:price 10000}}}
                                     {:symbol                     "SNT"
                                      :balances-per-chain         {1 {:raw-balance "100"}}
                                      :market-values-per-currency {:usd {:price 10000}}}]
         :network-preferences-names #{}
         :customization-color       nil
         :operable?                 true
         :operable                  :fully
         :address                   "0x1"}
   :0x2 {:tokens                    [{:symbol                     "SNT"
                                      :balances-per-chain         {1 {:raw-balance "200"}}
                                      :market-values-per-currency {:usd {:price 10000}}}]
         :network-preferences-names #{}
         :customization-color       nil
         :operable?                 true
         :operable                  :partially
         :address                   "0x2"}})

(def networks
  {:mainnet-network
   {:full-name        "Mainnet"
    :network-name     :mainnet
    :chain-id         1
    :related-chain-id 5
    :layer            1}
   :layer-2-networks
   [{:full-name        "Optimism"
     :network-name     :optimism
     :chain-id         10
     :related-chain-id 420
     :layer            2}
    {:full-name        "Arbitrum"
     :network-name     :arbitrum
     :chain-id         42161
     :related-chain-id 421613
     :layer            2}]})

(def swap-data
  {:asset-to-pay
   {:description "Status Network Token (SNT)"
    :decimals 18
    :symbol "SNT"
    :name "Status"
    :total-balance 1
    :balances-per-chain
    {1
     {:raw-balance 1000000000000000000
      :balance     "1"
      :chain-id    1}
     10
     {:raw-balance 0
      :balance     "0"
      :chain-id    10}
     42161
     {:raw-balance 0
      :balance     "0"
      :chain-id    42161}}
    :networks (concat [(networks :mainnet-network)] (networks :layer-2-networks))
    :chain-id 0
    :market-values-per-currency
    {:usd
     {:change-24hour     -0.00109422754667007
      :change-pct-day    -5.57352274163899
      :change-pct-24hour -4.177805426737527
      :high-day          0.0271858672171352
      :market-cap        170783296.1155821
      :has-error         false
      :change-pct-hour   -0.0160462113709363
      :low-day           0.02473516779550377
      :price             0.0251}}
    :asset-website-url "https://status.im/"
    :available-balance 1
    :token-list-id ""
    :built-on "ETH"
    :verified true}
   :asset-to-receive
   {:description "Dai Stablecoin"
    :decimals 18
    :symbol "DAI"
    :name "Dai Stablecoin"
    :total-balance 1
    :balances-per-chain
    {1
     {:raw-balance 1000000000000000000
      :balance     "1"
      :chain-id    1}
     10
     {:raw-balance 0
      :balance     "0"
      :chain-id    10}
     42161
     {:raw-balance 0
      :balance     "0"
      :chain-id    42161}}
    :networks (concat [(networks :mainnet-network)] (networks :layer-2-networks))
    :chain-id 0
    :market-values-per-currency
    {:usd
     {:change-24hour     -0.00109422754667007
      :change-pct-day    -5.57352274163899
      :change-pct-24hour -4.177805426737527
      :high-day          0.0271858672171352
      :market-cap        170783296.1155821
      :has-error         false
      :change-pct-hour   -0.0160462113709363
      :low-day           0.02473516779550377
      :price             0.0251}}
    :asset-website-url "https://status.im/"
    :available-balance 1
    :token-list-id ""
    :built-on "ETH"
    :verified true}
   :network (networks :mainnet-network)
   :swap-proposal {:amount-out               "0x10000"
                   :amount-in                "0x10000"
                   :approval-required        true
                   :approval-amount-required "0x10000"
                   :gas-amount               "25000"
                   :gas-fees                 {:max-fee-per-gas-medium "4"
                                              :eip-1559-enabled       true
                                              :l-1-gas-fee            "0"}}
   :error-response "Error"
   :loading-swap-proposal? false
   :max-slippage 0.5})

(h/deftest-sub :wallet/swap
  [sub-name]
  (testing "Return the wallet/ui/swap node"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? swap-data (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-asset-to-pay
  [sub-name]
  (testing "Return swap asset-to-pay"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? (swap-data :asset-to-pay) (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-asset-to-receive
  [sub-name]
  (testing "Return swap asset-to-receive"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? (swap-data :asset-to-receive) (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-asset-to-pay-token-symbol
  [sub-name]
  (testing "Return asset-to-pay token symbol"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? "SNT" (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-asset-to-pay-networks
  [sub-name]
  (testing "Return the available networks for the swap asset-to-pay"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? networks (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-asset-to-pay-network-balance
  [sub-name]
  (testing "Return swap asset-to-pay"
    (swap! rf-db/app-db
      #(-> %
           (assoc :currencies currencies)
           (assoc-in [:wallet :ui :swap] swap-data)))
    (is (match? {:crypto "1 SNT" :fiat "$0.03"} (rf/sub [sub-name 1])))))

(h/deftest-sub :wallet/swap-network
  [sub-name]
  (testing "Return the current swap network"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? (swap-data :network) (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-error-response
  [sub-name]
  (testing "Return the swap error response"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? (swap-data :error-response) (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-max-slippage
  [sub-name]
  (testing "Return the max slippage for the swap"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? 0.5 (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-loading-swap-proposal?
  [sub-name]
  (testing "Return if swap is loading the swap proposal"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (false? (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-proposal
  [sub-name]
  (testing "Return the swap proposal"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? (swap-data :swap-proposal) (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-proposal-amount-out
  [sub-name]
  (testing "Return the amount out in the swap proposal"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? "0x10000" (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-proposal-approval-required
  [sub-name]
  (testing "Return if approval is required in the swap proposal"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (true? (rf/sub [sub-name])))))

(h/deftest-sub :wallet/swap-proposal-approval-amount-required
  [sub-name]
  (testing "Return the approval amount required in the swap proposal"
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? "0x10000" (rf/sub [sub-name])))))

(h/deftest-sub :wallet/wallet-swap-proposal-fee-fiat
  [sub-name]
  (testing "wallet send fee calculated and formatted in fiat"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts-with-tokens)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")
           (assoc-in [:wallet :ui :swap] swap-data)
           (assoc-in [:currencies] currencies)
           (assoc-in [:profile/profile :currency] :usd)))

    (let [token-symbol-for-fees "ETH"
          result                (rf/sub [sub-name token-symbol-for-fees])]
      (is (match? result 1)))))
