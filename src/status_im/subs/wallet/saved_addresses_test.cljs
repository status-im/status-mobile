(ns status-im.subs.wallet.saved-addresses-test
  (:require [clojure.test :refer [is testing]]
            [re-frame.db :as rf-db]
            [test-helpers.unit :as h]
            [utils.re-frame :as rf]))

(def saved-addresses-db
  {:test {"0x1" {:test?                     true
                 :address                   "0x1"
                 :mixedcase-address         "0x1"
                 :chain-short-names         "eth:arb1:oeth:"
                 :ens?                      false
                 :network-preferences-names #{:arbitrum
                                              :optimism
                                              :mainnet}
                 :name                      "Alice"
                 :created-at                1716826714
                 :ens                       ""
                 :customization-color       :blue
                 :removed?                  false}
          "0x2" {:test?                     true
                 :address                   "0x2"
                 :mixedcase-address         "0x2"
                 :chain-short-names         "eth:"
                 :ens?                      false
                 :network-preferences-names #{:mainnet}
                 :name                      "Bob"
                 :created-at                1716828825
                 :ens                       ""
                 :customization-color       :purple
                 :removed?                  false}}
   :prod {"0x1" {:test?                     false
                 :address                   "0x1"
                 :mixedcase-address         "0x1"
                 :chain-short-names         "eth:"
                 :ens?                      false
                 :network-preferences-names #{:mainnet}
                 :name                      "Alice"
                 :created-at                1716826745
                 :ens                       ""
                 :customization-color       :blue
                 :removed?                  false}
          "0x2" {:test?                     false
                 :address                   "0x2"
                 :mixedcase-address         "0x2"
                 :chain-short-names         "eth:arb1:oeth:"
                 :ens?                      false
                 :network-preferences-names #{:arbitrum
                                              :optimism
                                              :mainnet}
                 :name                      "Bob"
                 :created-at                1716828561
                 :ens                       ""
                 :customization-color       :purple
                 :removed?                  false}}})

(def grouped-saved-addresses-data
  [{:title "A"
    :data
    [{:test?                     false
      :address                   "0x1"
      :mixedcase-address         "0x1"
      :chain-short-names         "eth:"
      :ens?                      false
      :network-preferences-names #{:mainnet}
      :name                      "Alice"
      :created-at                1716826745
      :ens                       ""
      :customization-color       :blue
      :removed?                  false}]}
   {:title "B"
    :data
    [{:test?                     false
      :address                   "0x2"
      :mixedcase-address         "0x2"
      :chain-short-names         "eth:arb1:oeth:"
      :ens?                      false
      :network-preferences-names #{:arbitrum
                                   :optimism
                                   :mainnet}
      :name                      "Bob"
      :created-at                1716828561
      :ens                       ""
      :customization-color       :purple
      :removed?                  false}]}])

(h/deftest-sub :wallet/saved-addresses-by-network-mode
  [sub-name]
  (testing "returns saved addresses by network mode"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :saved-addresses] saved-addresses-db)
           (assoc-in [:profile/profile :test-networks-enabled?] true)))
    (is (= (:test saved-addresses-db) (rf/sub [sub-name])))))

(h/deftest-sub :wallet/address-saved?
  [sub-name]
  (testing "returns boolean if given address is saved"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :saved-addresses] saved-addresses-db)
           (assoc-in [:profile/profile :test-networks-enabled?] false)))
    (is (true? (rf/sub [sub-name "0x1"])))
    (is (false? (rf/sub [sub-name "0x3"])))))

(h/deftest-sub :wallet/grouped-saved-addresses
  [sub-name]
  (testing "returns saved addresses grouped by name"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :saved-addresses] saved-addresses-db)
           (assoc-in [:profile/profile :test-networks-enabled?] false)))
    (is (= grouped-saved-addresses-data (rf/sub [sub-name])))))

(h/deftest-sub :wallet/saved-addresses-addresses
  [sub-name]
  (testing "returns saved addresses addresses set"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :saved-addresses] saved-addresses-db)
           (assoc-in [:profile/profile :test-networks-enabled?] true)))
    (is (= (-> saved-addresses-db :test keys set) (rf/sub [sub-name])))))

(h/deftest-sub :wallet/saved-address-by-address
  [sub-name]
  (testing "returns a saved address"
    (let [address "0x1"]
      (swap! rf-db/app-db
        #(-> %
             (assoc-in [:wallet :saved-addresses] saved-addresses-db)
             (assoc-in [:profile/profile :test-networks-enabled?] false)))
      (is (= (get-in saved-addresses-db [:prod address]) (rf/sub [sub-name address]))))))
