(ns status-im.subs.general-test
  (:require
    [cljs.test :refer [is testing use-fixtures]]
    [re-frame.db :as rf-db]
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(use-fixtures :each
              {:before #(reset! rf-db/app-db {})
               :after  #(reset! rf-db/app-db {})})

(def currencies
  {:usd
   {:id :usd :short-name "USD" :symbol "$" :emoji "🇺🇸" :name "US Dollar" :popular? true :token? false}
   :eur {:id :eur :short-name "EUR" :symbol "€" :emoji "🇪🇺" :name "Euro" :popular? true :token? false}
   :btc
   {:id :btc :short-name "BTC" :symbol "₿" :emoji "🇧🇹" :name "Bitcoin" :popular? false :token? true}
   :eth
   {:id :eth :short-name "ETH" :symbol "Ξ" :emoji "🇪🇹" :name "Ethereum" :popular? false :token? true}
   :gbp {:id         :gbp
         :short-name "GBP"
         :symbol     "£"
         :emoji      "🇬🇧"
         :name       "British Pound"
         :popular?   false
         :token?     false}
   :jpy {:id         :jpy
         :short-name "JPY"
         :symbol     "¥"
         :emoji      "🇯🇵"
         :name       "Japanese Yen"
         :popular?   false
         :token?     false}})

(h/deftest-sub :currencies/categorized
  [sub-name]
  (swap! rf-db/app-db assoc :currencies currencies)

  (testing "all currencies categorized correctly"
    (is
     (= {:total   6
         :popular [{:id         :usd
                    :short-name "USD"
                    :symbol     "$"
                    :emoji      "🇺🇸"
                    :name       "US Dollar"
                    :popular?   true
                    :token?     false}
                   {:id         :eur
                    :short-name "EUR"
                    :symbol     "€"
                    :emoji      "🇪🇺"
                    :name       "Euro"
                    :popular?   true
                    :token?     false}]
         :crypto  [{:id         :btc
                    :short-name "BTC"
                    :symbol     "₿"
                    :emoji      "🇧🇹"
                    :name       "Bitcoin"
                    :popular?   false
                    :token?     true}
                   {:id         :eth
                    :short-name "ETH"
                    :symbol     "Ξ"
                    :emoji      "🇪🇹"
                    :name       "Ethereum"
                    :popular?   false
                    :token?     true}]
         :other   [{:id         :gbp
                    :short-name "GBP"
                    :symbol     "£"
                    :emoji      "🇬🇧"
                    :name       "British Pound"
                    :popular?   false
                    :token?     false}
                   {:id         :jpy
                    :short-name "JPY"
                    :symbol     "¥"
                    :emoji      "🇯🇵"
                    :name       "Japanese Yen"
                    :popular?   false
                    :token?     false}]}
        (rf/sub [sub-name ""]))))

  (testing "search query filters correctly"
    (is (= {:total   1
            :popular [{:id         :usd
                       :short-name "USD"
                       :symbol     "$"
                       :emoji      "🇺🇸"
                       :name       "US Dollar"
                       :popular?   true
                       :token?     false}]
            :crypto  []
            :other   []}
           (rf/sub [sub-name "usd"])))
    (is
     (=
      {:total 1
       :popular
       [{:id :eur :short-name "EUR" :symbol "€" :emoji "🇪🇺" :name "Euro" :popular? true :token? false}]
       :crypto []
       :other []}
      (rf/sub [sub-name "eur"])))
    (is
     (= {:total   3
         :popular []
         :crypto  [{:id         :btc
                    :short-name "BTC"
                    :symbol     "₿"
                    :emoji      "🇧🇹"
                    :name       "Bitcoin"
                    :popular?   false
                    :token?     true}
                   {:id         :eth
                    :short-name "ETH"
                    :symbol     "Ξ"
                    :emoji      "🇪🇹"
                    :name       "Ethereum"
                    :popular?   false
                    :token?     true}]
         :other   [{:id         :gbp
                    :short-name "GBP"
                    :symbol     "£"
                    :emoji      "🇬🇧"
                    :name       "British Pound"
                    :popular?   false
                    :token?     false}]}
        (rf/sub [sub-name "t"]))))

  (testing "case insensitive search query"
    (is (= {:total   1
            :popular [{:id         :usd
                       :short-name "USD"
                       :symbol     "$"
                       :emoji      "🇺🇸"
                       :name       "US Dollar"
                       :popular?   true
                       :token?     false}]
            :crypto  []
            :other   []}
           (rf/sub [sub-name "USD"])))
    (is
     (=
      {:total 1
       :popular
       [{:id :eur :short-name "EUR" :symbol "€" :emoji "🇪🇺" :name "Euro" :popular? true :token? false}]
       :crypto []
       :other []}
      (rf/sub [sub-name "eUr"])))
    (is
     (= {:total   3
         :popular []
         :crypto  [{:id         :btc
                    :short-name "BTC"
                    :symbol     "₿"
                    :emoji      "🇧🇹"
                    :name       "Bitcoin"
                    :popular?   false
                    :token?     true}
                   {:id         :eth
                    :short-name "ETH"
                    :symbol     "Ξ"
                    :emoji      "🇪🇹"
                    :name       "Ethereum"
                    :popular?   false
                    :token?     true}]
         :other   [{:id         :gbp
                    :short-name "GBP"
                    :symbol     "£"
                    :emoji      "🇬🇧"
                    :name       "British Pound"
                    :popular?   false
                    :token?     false}]}
        (rf/sub [sub-name "T"]))))

  (testing "search with no matching results"
    (is (= {:total   0
            :popular []
            :crypto  []
            :other   []}
           (rf/sub [sub-name "xyz"]))))

  (testing "all categories are included"
    (is (= [:total :popular :crypto :other]
           (keys (rf/sub [sub-name ""]))))))
