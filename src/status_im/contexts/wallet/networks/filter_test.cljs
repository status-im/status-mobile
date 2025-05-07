(ns status-im.contexts.wallet.networks.filter-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.wallet.networks.filter :as sut]
    [tests.wallet-test-data :as test-data]))

(deftest by-id-test
  (testing "networks filtered by id"
    (is (match? [test-data/arbitrum]
                (sut/by-id [test-data/mainnet test-data/arbitrum]
                           {:by-id #{test-data/mainnet-chain-id}}))))

  (testing "networks filtered by id, but the filter map is empty"
    (is (match? [test-data/mainnet test-data/arbitrum]
                (sut/by-id [test-data/mainnet test-data/arbitrum]
                           {}))))

  (testing "networks filtered by id, but the id filter is empty"
    (is (match? [test-data/mainnet test-data/arbitrum]
                (sut/by-id [test-data/mainnet test-data/arbitrum]
                           {:by-id #{}})))))

(deftest toggle-test
  (testing "returns the new filter if the current one is nil"
    (is (match? {:by-id #{test-data/mainnet-chain-id}}
                (sut/toggle nil {:by-id test-data/mainnet-chain-id}))))

  (testing "removes the chain-id if already present in the filter"
    (is (match? {:by-id #{test-data/mainnet-chain-id}}
                (sut/toggle {:by-id #{test-data/mainnet-chain-id test-data/arbitrum-chain-id}}
                            {:by-id test-data/arbitrum-chain-id}))))

  (testing "adds the chain-id if not already filtered"
    (is (match? {:by-id #{test-data/mainnet-chain-id test-data/arbitrum-chain-id}}
                (sut/toggle {:by-id #{test-data/mainnet-chain-id}}
                            {:by-id test-data/arbitrum-chain-id})))))

(deftest has-filters?-test
  (testing "returns true if there is a filter"
    (is (match? true (sut/has-filters? {:by-id #{test-data/arbitrum-chain-id}}))))

  (testing "returns false if filter is not defined"
    (is (match? false (sut/has-filters? {:by-id nil})))
    (is (match? false (sut/has-filters? {:by-id #{}})))
    (is (match? false (sut/has-filters? {})))
    (is (match? false (sut/has-filters? nil)))))
