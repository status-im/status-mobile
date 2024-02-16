(ns legacy.status-im.subs.wallet.wallet-test
  (:require
    [cljs.test :refer [deftest is testing]]
    [legacy.status-im.subs.wallet.transactions :as wallet.transactions]
    [legacy.status-im.subs.wallet.wallet :as wallet]
    [re-frame.db :as rf-db]
    [test-helpers.unit :as h]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(def money-zero (money/bignumber 0))
(def money-eth (money/bignumber 8000000000000000000))
(def money-snt (money/bignumber 756000000000000000000))
(def main-account-id "0x0Fbd")

(def accounts
  [{:address "0x0Fbd"
    :name    "Main account"
    :hidden  false
    :removed false}
   {:address "0x5B03"
    :name    "Secondary account"
    :hidden  false
    :removed false}])

(def wallet
  {:accounts {main-account-id
              {:balance      {:ETH money-eth :SNT money-snt}
               :transactions {}
               :max-block    0}
              "0x5B03"
              {:balance      {:ETH money-eth :SNT money-snt}
               :transactions {}
               :max-block    10}}})

(def prices
  {:ETH {:USD 1282.23}
   :SNT {:USD 0.0232}})

(def tokens
  {"0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
   {:address  "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
    :name     "Ether"
    :symbol   :ETH
    :decimals 18
    :chainId  1}
   "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
   {:address  "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
    :name     "Status Network Token"
    :symbol   :SNT
    :decimals 18
    :chainId  1}})

(h/deftest-sub :balances
  [sub-name]
  (swap! rf-db/app-db assoc
    :profile/wallet-accounts accounts
    :wallet-legacy           wallet)
  (is (= [{:ETH money-eth
           :SNT money-snt}
          {:ETH money-eth
           :SNT money-snt}]
         (rf/sub [sub-name]))))

(h/deftest-sub :wallet-legacy/token->decimals
  [sub-name]
  (swap! rf-db/app-db assoc :wallet-legacy/all-tokens tokens)
  (is (= {:SNT 18 :ETH 18}
         (rf/sub [sub-name]))))

(deftest get-balance-total-value-test
  (is (= 697.53
         (wallet/get-balance-total-value
          {:ETH (money/bignumber 1000000000000000000)
           :SNT (money/bignumber 100000000000000000000)
           :AST (money/bignumber 10000)}
          {:ETH {:USD 677.91}
           :SNT {:USD 0.1562}
           :AST {:USD 4}}
          :USD
          {:ETH 18
           :SNT 18
           :AST 4}))))

(h/deftest-sub :portfolio-value
  [sub-name]
  (testing "returns fallback value when balances and prices are not available"
    (is (= "..." (rf/sub [sub-name]))))

  (testing "returns zero when balance is not positive"
    (let [empty-wallet {:accounts {main-account-id
                                   {:balance {:ETH money-zero
                                              :SNT money-zero}}}}]
      (swap! rf-db/app-db assoc
        :profile/wallet-accounts  accounts
        :prices                   prices
        :wallet-legacy            empty-wallet
        :wallet-legacy/all-tokens tokens)
      (is (= "0" (rf/sub [sub-name])))))

  (testing "returns formatted value in the default USD currency"
    (swap! rf-db/app-db assoc
      :profile/wallet-accounts  accounts
      :prices                   prices
      :wallet-legacy            wallet
      :wallet-legacy/all-tokens tokens)
    (is (= "20,550.76" (rf/sub [sub-name])))))

(h/deftest-sub :account-portfolio-value
  [sub-name]
  (testing "returns fallback value when balances and prices are not available"
    (is (= "..." (rf/sub [sub-name]))))

  (testing "returns zero when balance is not positive"
    (let [empty-wallet {:accounts {main-account-id
                                   {:balance {:ETH money-zero
                                              :SNT money-zero}}}}]
      (swap! rf-db/app-db assoc
        :profile/wallet-accounts  accounts
        :prices                   prices
        :wallet-legacy            empty-wallet
        :wallet-legacy/all-tokens tokens)
      (is (= "0" (rf/sub [sub-name main-account-id])))))

  (testing "returns formatted value in the default USD currency"
    (swap! rf-db/app-db assoc
      :profile/wallet-accounts  accounts
      :prices                   prices
      :wallet-legacy            wallet
      :wallet-legacy/all-tokens tokens)
    (is (= "10,275.38" (rf/sub [sub-name main-account-id])))))

(def transactions
  [{:timestamp "1505912551000"}
   {:timestamp "1505764322000"}
   {:timestamp "1505750000000"}])

(def grouped-transactions
  '({:title "20 Sep"
     :key :20170920
     :data
     ({:timestamp "1505912551000"})}
    {:title "18 Sep"
     :key :20170918
     :data
     ({:timestamp "1505764322000"}
      {:timestamp "1505750000000"})}))

(deftest group-transactions-by-date
  (testing "Check if transactions are sorted by date"
    (is (= (wallet.transactions/group-transactions-by-date transactions)
           grouped-transactions))))
