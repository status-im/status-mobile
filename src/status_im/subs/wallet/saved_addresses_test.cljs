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
                 :has-ens?                  false
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
                 :has-ens?                  false
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
                 :has-ens?                  false
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
                 :has-ens?                  false
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
      :has-ens?                  false
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
      :has-ens?                  false
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
    (is (= (vals (:test saved-addresses-db)) (rf/sub [sub-name])))))

(h/deftest-sub :wallet/grouped-saved-addresses
  [sub-name]
  (testing "returns saved addresses grouped by name"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :saved-addresses] saved-addresses-db)
           (assoc-in [:profile/profile :test-networks-enabled?] false)))
    (is (= grouped-saved-addresses-data (rf/sub [sub-name])))))
