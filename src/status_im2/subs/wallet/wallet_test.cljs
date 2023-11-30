(ns status-im2.subs.wallet.wallet-test
  (:require [cljs.test :refer [is testing use-fixtures]]
            [re-frame.db :as rf-db]
            status-im2.subs.root
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

(use-fixtures :each
              {:before #(reset! rf-db/app-db {})})

(def tokens-0x1
  [{:decimals                   1
    :symbol                     "ETH"
    :name                       "Ether"
    :balances-per-chain         {1 {:raw-balance "20" :has-error false}
                                 2 {:raw-balance "10" :has-error false}}
    :market-values-per-currency {:usd {:price 1000}}}
   {:decimals                   2
    :symbol                     "DAI"
    :name                       "Dai Stablecoin"
    :balances-per-chain         {1 {:raw-balance "100" :has-error false}
                                 2 {:raw-balance "150" :has-error false}}
    :market-values-per-currency {:usd {:price 100}}}])

(def tokens-0x2
  [{:decimals                   3
    :symbol                     "ETH"
    :name                       "Ether"
    :balances-per-chain         {1 {:raw-balance "2500" :has-error false}
                                 2 {:raw-balance "3000" :has-error false}
                                 3 {:raw-balance "<nil>" :has-error false}}
    :market-values-per-currency {:usd {:price 200}}}
   {:decimals                   10
    :symbol                     "DAI"
    :name                       "Dai Stablecoin"
    :balances-per-chain         {1 {:raw-balance "10000000000" :has-error false}
                                 2 {:raw-balance "0" :has-error false}
                                 3 {:raw-balance "<nil>" :has-error false}}
    :market-values-per-currency {:usd {:price 1000}}}])

(def accounts
  {"0x1" {:path                     "m/44'/60'/0'/0/0"
          :emoji                    "😃"
          :key-uid                  "0x2f5ea39"
          :address                  "0x1"
          :wallet                   false
          :name                     "Account One"
          :type                     :generated
          :chat                     false
          :test-preferred-chain-ids #{5 420 421613}
          :color                    :blue
          :hidden                   false
          :prod-preferred-chain-ids #{1 10 42161}
          :position                 0
          :clock                    1698945829328
          :created-at               1698928839000
          :operable                 "fully"
          :mixedcase-address        "0x7bcDfc75c431"
          :public-key               "0x04371e2d9d66b82f056bc128064"
          :removed                  false
          :tokens                   tokens-0x1}
   "0x2" {:path                     "m/44'/60'/0'/0/1"
          :emoji                    "💎"
          :key-uid                  "0x2f5ea39"
          :address                  "0x2"
          :wallet                   false
          :name                     "Account Two"
          :type                     :generated
          :chat                     false
          :test-preferred-chain-ids #{5 420 421613}
          :color                    :purple
          :hidden                   false
          :prod-preferred-chain-ids #{1 10 42161}
          :position                 1
          :clock                    1698945829328
          :created-at               1698928839000
          :operable                 "fully"
          :mixedcase-address        "0x7bcDfc75c431"
          :public-key               "0x04371e2d9d66b82f056bc128064"
          :removed                  false
          :tokens                   tokens-0x2}})

(h/deftest-sub :wallet/balances
  [sub-name]
  (testing "a map: address->balance"
    (swap! rf-db/app-db #(assoc-in % [:wallet :accounts] accounts))

    (is (= {"0x1" 3250 "0x2" 2100}
           (rf/sub [sub-name])))))

(h/deftest-sub :wallet/accounts
  [sub-name]
  (testing "returns all accounts without balance"
    (swap! rf-db/app-db #(assoc-in % [:wallet :accounts] accounts))

    (is
     (= (list {:path                     "m/44'/60'/0'/0/0"
               :emoji                    "😃"
               :key-uid                  "0x2f5ea39"
               :address                  "0x1"
               :wallet                   false
               :name                     "Account One"
               :type                     :generated
               :chat                     false
               :test-preferred-chain-ids #{5 420 421613}
               :color                    :blue
               :hidden                   false
               :prod-preferred-chain-ids #{1 10 42161}
               :position                 0
               :clock                    1698945829328
               :created-at               1698928839000
               :operable                 "fully"
               :mixedcase-address        "0x7bcDfc75c431"
               :public-key               "0x04371e2d9d66b82f056bc128064"
               :removed                  false
               :tokens                   tokens-0x1}
              {:path                     "m/44'/60'/0'/0/1"
               :emoji                    "💎"
               :key-uid                  "0x2f5ea39"
               :address                  "0x2"
               :wallet                   false
               :name                     "Account Two"
               :type                     :generated
               :chat                     false
               :test-preferred-chain-ids #{5 420 421613}
               :color                    :purple
               :hidden                   false
               :prod-preferred-chain-ids #{1 10 42161}
               :position                 1
               :clock                    1698945829328
               :created-at               1698928839000
               :operable                 "fully"
               :mixedcase-address        "0x7bcDfc75c431"
               :public-key               "0x04371e2d9d66b82f056bc128064"
               :removed                  false
               :tokens                   tokens-0x2})
        (rf/sub [sub-name])))))

(h/deftest-sub :wallet/current-viewing-account
  [sub-name]
  (testing "returns current account with balance base"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")))

    (is
     (= {:path                     "m/44'/60'/0'/0/0"
         :emoji                    "😃"
         :key-uid                  "0x2f5ea39"
         :address                  "0x1"
         :wallet                   false
         :name                     "Account One"
         :type                     :generated
         :chat                     false
         :test-preferred-chain-ids #{5 420 421613}
         :color                    :blue
         :hidden                   false
         :prod-preferred-chain-ids #{1 10 42161}
         :position                 0
         :clock                    1698945829328
         :created-at               1698928839000
         :operable                 "fully"
         :mixedcase-address        "0x7bcDfc75c431"
         :public-key               "0x04371e2d9d66b82f056bc128064"
         :removed                  false
         :balance                  3250
         :tokens                   tokens-0x1}
        (rf/sub [sub-name])))))


(h/deftest-sub :wallet/addresses
  [sub-name]
  (testing "returns all addresses"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")))

    (is
     (= (set ["0x1" "0x2"])
        (rf/sub [sub-name])))))

(h/deftest-sub :wallet/watch-address-activity-state
  [sub-name]
  (testing "watch address activity state with nil value"
    (is (= nil (rf/sub [sub-name]))))

  (testing "watch address activity state with no-activity value"
    (swap! rf-db/app-db #(assoc-in % [:wallet :ui :watch-address-activity-state] :no-activity))
    (is (= :no-activity (rf/sub [sub-name]))))

  (testing "watch address activity state with has-activity value"
    (swap! rf-db/app-db #(assoc-in % [:wallet :ui :watch-address-activity-state] :has-activity))
    (is (= :has-activity (rf/sub [sub-name])))))
