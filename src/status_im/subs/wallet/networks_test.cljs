(ns status-im.subs.wallet.networks-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.root
    status-im.subs.wallet.networks
    [test-helpers.unit :as h]
    [tests.wallet-test-data :as test-data]
    [utils.re-frame :as rf]))

(h/deftest-sub :wallet/chain-ids
  [sub-name]
  (testing "returns chain-ids for prod"
    (swap! rf-db/app-db #(test-data/add-networks-to-db %))
    (is
     (match? #{test-data/mainnet-chain-id test-data/arbitrum-chain-id test-data/optimism-chain-id}
             (rf/sub [sub-name]))))

  (testing "returns chain-ids for test"
    (swap! rf-db/app-db #(-> %
                             (test-data/add-testnet-enabled-to-db)
                             (test-data/add-networks-to-db)))
    (is
     (match? #{test-data/sepolia-chain-id test-data/arbitrum-sepolia-chain-id
               test-data/optimism-sepolia-chain-id}
             (rf/sub [sub-name])))))

(h/deftest-sub :wallet/networks
  [sub-name]
  (testing "returns networks collection correctly"
    (swap! rf-db/app-db #(test-data/add-networks-to-db %))
    (is
     (match? [test-data/mainnet test-data/arbitrum test-data/optimism]
             (rf/sub [sub-name])))))

(h/deftest-sub :wallet/active-chain-ids
  [sub-name]
  (testing "returns active chain-ids only"
    (swap! rf-db/app-db #(test-data/add-active-networks-to-db
                          %
                          [test-data/mainnet-chain-id]))
    (is
     (match? #{test-data/mainnet-chain-id}
             (rf/sub [sub-name])))))

(h/deftest-sub :wallet/active-networks
  [sub-name]
  (testing "returns active networks only"
    (swap! rf-db/app-db #(test-data/add-active-networks-to-db
                          %
                          [test-data/optimism-chain-id]))
    (is
     (match? [test-data/optimism]
             (rf/sub [sub-name])))))
