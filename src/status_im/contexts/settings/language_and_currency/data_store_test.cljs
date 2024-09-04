(ns status-im.contexts.settings.language-and-currency.data-store-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.contexts.settings.language-and-currency.data-store :as sut]))

(def raw-currency-popular
  {:id          "usd"
   :shortName   "USD"
   :name        "US Dollar"
   :symbol      "$"
   :emoji       "🇺🇸"
   :isPopular   true
   :isToken     false
   :imageSource "https://example.com/image.png"})

(def raw-currency-token
  {:id          "btc"
   :shortName   "BTC"
   :name        "Bitcoin"
   :symbol      ""
   :emoji       ""
   :isPopular   false
   :isToken     true
   :imageSource "https://example.com/image.png"})

(deftest rpc->currency-test
  (testing "transforms a currency"
    (is
     (match? {:id         :usd
              :short-name "USD"
              :symbol     "$"
              :emoji      "🇺🇸"
              :name       "US Dollar"
              :popular?   true
              :token?     false}
             (sut/rpc->currency raw-currency-popular)))))

(deftest rpc->currencies-test
  (testing "transforms and sorts raw keypairs"
    (is
     (match? [(sut/rpc->currency raw-currency-popular)
              (sut/rpc->currency raw-currency-token)]
             (sut/rpc->currencies [raw-currency-popular
                                   raw-currency-token])))))

(deftest get-formatted-currency-data-test
  (testing "returns formatted currency data"
    (is
     (match? [{:title "Popular currencies"
               :data  [(sut/rpc->currency raw-currency-popular)]}
              {:title "Crypto"
               :data  [(sut/rpc->currency raw-currency-token)]}]
             (sut/get-formatted-currency-data {:popular [(sut/rpc->currency raw-currency-popular)]
                                               :crypto  [(sut/rpc->currency raw-currency-token)]
                                               :other   []})))))
