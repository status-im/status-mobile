(ns status-im.subs.wallet.swap-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.subs.root]
    [status-im.subs.wallet.collectibles]
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

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
   :network nil})

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
    (swap! rf-db/app-db assoc-in
      [:wallet :ui :swap]
      swap-data)
    (is (match? {:crypto "1 SNT" :fiat "$0.03"} (rf/sub [sub-name 1])))))
