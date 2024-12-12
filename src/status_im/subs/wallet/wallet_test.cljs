(ns status-im.subs.wallet.wallet-test
  (:require
    [cljs.test :refer [is testing use-fixtures]]
    [re-frame.db :as rf-db]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.db :as db]
    [status-im.subs.root]
    [test-helpers.unit :as h]
    [utils.money :as money]
    [utils.re-frame :as rf]))

(use-fixtures :each
              {:before #(reset! rf-db/app-db {})})

(def ^:private currencies
  {:usd {:id         :usd
         :short-name "USD"
         :symbol     "$"
         :emoji      "🇺🇸"
         :name       "US Dollar"
         :popular?   true
         :token?     false}})

(def ^:private accounts-with-tokens
  {:0x1 {:tokens                    [{:symbol             "ETH"
                                      :balances-per-chain {1 {:raw-balance "100"}}}
                                     {:symbol             "SNT"
                                      :balances-per-chain {1 {:raw-balance "100"}}}]
         :network-preferences-names #{}
         :customization-color       nil
         :operable?                 true
         :operable                  :fully
         :address                   "0x1"}
   :0x2 {:tokens                    [{:symbol             "SNT"
                                      :balances-per-chain {1 {:raw-balance "200"}}}]
         :network-preferences-names #{}
         :customization-color       nil
         :operable?                 true
         :operable                  :partially
         :address                   "0x2"}})

(def tokens-0x1
  [{:decimals           0
    :symbol             "ETH"
    :name               "Ether"
    :balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber "2")
                                                              :has-error   false}
                         constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber "1")
                                                              :has-error   false}}}
   {:decimals           0
    :symbol             "DAI"
    :name               "Dai Stablecoin"
    :balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "1")
                                                              :has-error   false}
                         constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "1.5")
                                                              :has-error   false}
                         constants/arbitrum-mainnet-chain-id {:raw-balance nil :has-error false}}}])

(def tokens-0x2
  [{:decimals           0
    :symbol             "ETH"
    :name               "Ether"
    :balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "2.5")
                                                              :has-error   false}
                         constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "3")
                                                              :has-error   false}
                         constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "<nil>")
                                                              :has-error   false}}}
   {:decimals           0
    :symbol             "DAI"
    :name               "Dai Stablecoin"
    :balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "1")
                                                              :has-error   false}
                         constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber "0")
                                                              :has-error   false}
                         constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "<nil>")
                                                              :has-error   false}}}])

(def tokens-0x3
  [{:decimals           0
    :symbol             "ETH"
    :name               "Ether"
    :balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "5")
                                                              :has-error   false}
                         constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "2")
                                                              :has-error   false}
                         constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "<nil>")
                                                              :has-error   false}}}
   {:decimals           0
    :symbol             "DAI"
    :name               "Dai Stablecoin"
    :balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "1")
                                                              :has-error   false}
                         constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber "0")
                                                              :has-error   false}
                         constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                            "<nil>")
                                                              :has-error   false}}}])

(def accounts
  {"0x1" {:path                     "m/44'/60'/0'/0/0"
          :emoji                    "😃"
          :key-uid                  "0x2f5ea39"
          :address                  "0x1"
          :wallet                   false
          :name                     "Account One"
          :type                     :generated
          :watch-only?              false
          :operable?                true
          :chat                     false
          :test-preferred-chain-ids #{5 420 421613}
          :color                    :blue
          :hidden                   false
          :prod-preferred-chain-ids #{1 10 42161}
          :position                 0
          :clock                    1698945829328
          :created-at               1698928839000
          :operable                 :fully
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
          :watch-only?              false
          :operable?                true
          :chat                     false
          :test-preferred-chain-ids #{5 420 421613}
          :color                    :purple
          :hidden                   false
          :prod-preferred-chain-ids #{1 10 42161}
          :position                 1
          :clock                    1698945829328
          :created-at               1698928839000
          :operable                 :fully
          :mixedcase-address        "0x7bcDfc75c431"
          :public-key               "0x04371e2d9d66b82f056bc128064"
          :removed                  false
          :tokens                   tokens-0x2}
   "0x3" {:path                     ""
          :emoji                    "🎉"
          :key-uid                  "0x2f5ea39"
          :address                  "0x3"
          :wallet                   false
          :name                     "Watched Account 1"
          :type                     :watch
          :watch-only?              true
          :operable?                true
          :chat                     false
          :test-preferred-chain-ids #{0}
          :color                    :magenta
          :hidden                   false
          :prod-preferred-chain-ids #{0}
          :position                 2
          :clock                    1698945829328
          :created-at               1698928839000
          :operable                 :fully
          :mixedcase-address        "0x7bcDfc75c431"
          :public-key               "0x"
          :removed                  false
          :tokens                   tokens-0x3}})

(def network-data
  {:test [{:test?            true
           :short-name       "eth"
           :network-name     :ethereum
           :related-chain-id 1
           :layer            1}
          {:test?            true
           :short-name       "arb1"
           :related-chain-id 42161
           :layer            2}
          {:test?            true
           :short-name       "oeth"
           :related-chain-id 10
           :layer            2}]
   :prod [{:test?      false
           :short-name "eth"
           :chain-id   1
           :layer      1}
          {:test?      false
           :short-name "arb1"
           :chain-id   42161
           :layer      2}
          {:test?      false
           :short-name "oeth"
           :chain-id   10
           :layer      2}]})

(def route-data
  [{:gas-amount "25000"
    :gas-fees   {:max-fee-per-gas-medium "4"
                 :eip-1559-enabled       true
                 :l-1-gas-fee            "0"}}])

(h/deftest-sub :wallet/balances-in-selected-networks
  [sub-name]
  (testing "a map: address->balance"
    (swap! rf-db/app-db #(-> %
                             (assoc :wallet db/defaults)
                             (assoc-in [:wallet :accounts] accounts)
                             (assoc-in [:wallet :tokens :prices-per-token]
                                       {:ETH {:usd 2000} :DAI {:usd 1}})))
    (let [result      (rf/sub [sub-name])
          balance-0x1 (money/bignumber 6002.5)
          balance-0x2 (money/bignumber 11001)
          balance-0x3 (money/bignumber 14001)]

      (is (money/equal-to balance-0x1 (get result "0x1")))
      (is (money/equal-to balance-0x2 (get result "0x2")))
      (is (money/equal-to balance-0x3 (get result "0x3"))))))

(h/deftest-sub :wallet/accounts
  [sub-name]
  (testing "returns all accounts without balance"
    (swap! rf-db/app-db
      #(-> %
           (assoc :wallet db/defaults)
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)))
    (is
     (match?
      (list {:path                      "m/44'/60'/0'/0/0"
             :emoji                     "😃"
             :key-uid                   "0x2f5ea39"
             :address                   "0x1"
             :wallet                    false
             :name                      "Account One"
             :type                      :generated
             :watch-only?               false
             :operable?                 true
             :chat                      false
             :test-preferred-chain-ids  #{5 420 421613}
             :color                     :blue
             :hidden                    false
             :prod-preferred-chain-ids  #{1 10 42161}
             :network-preferences-names #{:mainnet :arbitrum :optimism}
             :position                  0
             :clock                     1698945829328
             :created-at                1698928839000
             :operable                  :fully
             :mixedcase-address         "0x7bcDfc75c431"
             :public-key                "0x04371e2d9d66b82f056bc128064"
             :removed                   false
             :tokens                    tokens-0x1}
            {:path                      "m/44'/60'/0'/0/1"
             :emoji                     "💎"
             :key-uid                   "0x2f5ea39"
             :address                   "0x2"
             :wallet                    false
             :name                      "Account Two"
             :type                      :generated
             :watch-only?               false
             :operable?                 true
             :chat                      false
             :test-preferred-chain-ids  #{5 420 421613}
             :color                     :purple
             :hidden                    false
             :prod-preferred-chain-ids  #{1 10 42161}
             :network-preferences-names #{:mainnet :arbitrum :optimism}
             :position                  1
             :clock                     1698945829328
             :created-at                1698928839000
             :operable                  :fully
             :mixedcase-address         "0x7bcDfc75c431"
             :public-key                "0x04371e2d9d66b82f056bc128064"
             :removed                   false
             :tokens                    tokens-0x2}
            {:path                      ""
             :emoji                     "🎉"
             :key-uid                   "0x2f5ea39"
             :address                   "0x3"
             :wallet                    false
             :name                      "Watched Account 1"
             :type                      :watch
             :watch-only?               true
             :operable?                 true
             :chat                      false
             :test-preferred-chain-ids  #{0}
             :color                     :magenta
             :hidden                    false
             :prod-preferred-chain-ids  #{0}
             :network-preferences-names #{}
             :position                  2
             :clock                     1698945829328
             :created-at                1698928839000
             :operable                  :fully
             :mixedcase-address         "0x7bcDfc75c431"
             :public-key                "0x"
             :removed                   false
             :tokens                    tokens-0x3})
      (rf/sub [sub-name])))))

(h/deftest-sub :wallet/current-viewing-account-address
  [sub-name]
  (testing "returns the address of the current viewing account"
    (let [viewing-address "0x1"]
      (swap! rf-db/app-db #(assoc-in % [:wallet :current-viewing-account-address] viewing-address))
      (is (match? viewing-address (rf/sub [sub-name]))))))

(h/deftest-sub :wallet/current-viewing-account
  [sub-name]
  (testing "returns current account with balance base"
    (swap! rf-db/app-db
      #(-> %
           (assoc :wallet db/defaults)
           (assoc :currencies currencies)
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")
           (assoc-in [:wallet :tokens :prices-per-token]
                     {:ETH {:usd 2000} :DAI {:usd 1}})
           (assoc-in [:wallet :networks] network-data)))

    (let [result (rf/sub [sub-name])]
      (is
       (match? {:path                      "m/44'/60'/0'/0/0"
                :emoji                     "😃"
                :key-uid                   "0x2f5ea39"
                :address                   "0x1"
                :wallet                    false
                :name                      "Account One"
                :type                      :generated
                :watch-only?               false
                :operable?                 true
                :chat                      false
                :test-preferred-chain-ids  #{5 420 421613}
                :color                     :blue
                :hidden                    false
                :prod-preferred-chain-ids  #{1 10 42161}
                :network-preferences-names #{:mainnet :arbitrum :optimism}
                :position                  0
                :clock                     1698945829328
                :created-at                1698928839000
                :operable                  :fully
                :mixedcase-address         "0x7bcDfc75c431"
                :public-key                "0x04371e2d9d66b82f056bc128064"
                :removed                   false
                :tokens                    tokens-0x1}
               (dissoc result :balance :formatted-balance)))

      (is (money/equal-to (:balance result) (money/bignumber 6002.5)))
      (is (match? (:formatted-balance result) "$6002.50")))))

(h/deftest-sub :wallet/addresses
  [sub-name]
  (testing "returns all addresses"
    (swap! rf-db/app-db #(assoc-in % [:wallet :accounts] accounts))
    (is (match? #{"0x1" "0x2" "0x3"}
                (rf/sub [sub-name])))))

(h/deftest-sub :wallet/watch-address-activity-state
  [sub-name]
  (testing "watch address activity state with nil value"
    (is (nil? (rf/sub [sub-name]))))

  (testing "watch address activity state with no-activity value"
    (swap! rf-db/app-db #(assoc-in % [:wallet :ui :add-address-to-watch :activity-state] :no-activity))
    (is (match? :no-activity (rf/sub [sub-name]))))

  (testing "watch address activity state with has-activity value"
    (swap! rf-db/app-db #(assoc-in % [:wallet :ui :add-address-to-watch :activity-state] :has-activity))
    (is (match? :has-activity (rf/sub [sub-name]))))

  (testing "watch address activity state with invalid-ens value"
    (swap! rf-db/app-db #(assoc-in % [:wallet :ui :add-address-to-watch :activity-state] :invalid-ens))
    (is (match? :invalid-ens (rf/sub [sub-name])))))

(h/deftest-sub :wallet/accounts-without-current-viewing-account
  [sub-name]
  (testing "returns the accounts list without the current viewing account in it"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :current-viewing-account-address] "0x2")
           (assoc-in [:wallet :networks] network-data)))
    (is
     (match?
      (list
       {:path                      "m/44'/60'/0'/0/0"
        :emoji                     "😃"
        :key-uid                   "0x2f5ea39"
        :address                   "0x1"
        :wallet                    false
        :name                      "Account One"
        :type                      :generated
        :watch-only?               false
        :operable?                 true
        :chat                      false
        :test-preferred-chain-ids  #{5 420 421613}
        :color                     :blue
        :hidden                    false
        :prod-preferred-chain-ids  #{1 10 42161}
        :network-preferences-names #{:mainnet :arbitrum :optimism}
        :position                  0
        :clock                     1698945829328
        :created-at                1698928839000
        :operable                  :fully
        :mixedcase-address         "0x7bcDfc75c431"
        :public-key                "0x04371e2d9d66b82f056bc128064"
        :removed                   false
        :tokens                    tokens-0x1}
       {:path                      ""
        :emoji                     "🎉"
        :key-uid                   "0x2f5ea39"
        :address                   "0x3"
        :wallet                    false
        :name                      "Watched Account 1"
        :type                      :watch
        :watch-only?               true
        :operable?                 true
        :chat                      false
        :test-preferred-chain-ids  #{0}
        :color                     :magenta
        :hidden                    false
        :prod-preferred-chain-ids  #{0}
        :network-preferences-names #{}
        :position                  2
        :clock                     1698945829328
        :created-at                1698928839000
        :operable                  :fully
        :mixedcase-address         "0x7bcDfc75c431"
        :public-key                "0x"
        :removed                   false
        :tokens                    tokens-0x3})
      (rf/sub [sub-name])))))

(h/deftest-sub :wallet/accounts-without-watched-accounts
  [sub-name]
  (testing "returns the accounts list without the watched accounts in it"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)))
    (is
     (match?
      (list
       {:path                      "m/44'/60'/0'/0/0"
        :emoji                     "😃"
        :key-uid                   "0x2f5ea39"
        :address                   "0x1"
        :wallet                    false
        :name                      "Account One"
        :type                      :generated
        :watch-only?               false
        :operable?                 true
        :chat                      false
        :test-preferred-chain-ids  #{5 420 421613}
        :color                     :blue
        :customization-color       :blue
        :hidden                    false
        :prod-preferred-chain-ids  #{1 10 42161}
        :network-preferences-names #{:mainnet :arbitrum :optimism}
        :position                  0
        :clock                     1698945829328
        :created-at                1698928839000
        :operable                  :fully
        :mixedcase-address         "0x7bcDfc75c431"
        :public-key                "0x04371e2d9d66b82f056bc128064"
        :removed                   false
        :tokens                    tokens-0x1}
       {:path                      "m/44'/60'/0'/0/1"
        :emoji                     "💎"
        :key-uid                   "0x2f5ea39"
        :address                   "0x2"
        :wallet                    false
        :name                      "Account Two"
        :type                      :generated
        :watch-only?               false
        :operable?                 true
        :chat                      false
        :test-preferred-chain-ids  #{5 420 421613}
        :color                     :purple
        :customization-color       :purple
        :hidden                    false
        :prod-preferred-chain-ids  #{1 10 42161}
        :network-preferences-names #{:mainnet :arbitrum :optimism}
        :position                  1
        :clock                     1698945829328
        :created-at                1698928839000
        :operable                  :fully
        :mixedcase-address         "0x7bcDfc75c431"
        :public-key                "0x04371e2d9d66b82f056bc128064"
        :removed                   false
        :tokens                    tokens-0x2})
      (rf/sub [sub-name])))))

(h/deftest-sub :wallet/accounts-with-current-asset
  [sub-name]
  (testing "returns the accounts list with the current asset using token-symbol"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts-with-tokens)
           (assoc-in [:wallet :ui :send :token-symbol] "ETH")))
    (let [result (rf/sub [sub-name])]
      (is (match? result
                  [{:tokens                    [{:symbol             "ETH"
                                                 :balances-per-chain {1 {:raw-balance "100"}}}
                                                {:symbol             "SNT"
                                                 :balances-per-chain {1 {:raw-balance "100"}}}]
                    :network-preferences-names #{}
                    :customization-color       nil
                    :operable?                 true
                    :operable                  :fully
                    :address                   "0x1"}]))))

  (testing "returns the accounts list with the current asset using token"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts-with-tokens)
           (assoc-in [:wallet :ui :send :token] {:symbol "ETH"})))
    (let [result (rf/sub [sub-name])]
      (is (match? result
                  [{:tokens                    [{:symbol             "ETH"
                                                 :balances-per-chain {1 {:raw-balance "100"}}}
                                                {:symbol             "SNT"
                                                 :balances-per-chain {1 {:raw-balance "100"}}}]
                    :network-preferences-names #{}
                    :customization-color       nil
                    :operable?                 true
                    :operable                  :fully
                    :address                   "0x1"}]))))

  (testing
    "returns the full accounts list with the current asset using token-symbol if each account has the asset"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts-with-tokens)
           (assoc-in [:wallet :ui :send :token-symbol] "SNT")))
    (let [result (rf/sub [sub-name])]
      (is (match? result (vals accounts-with-tokens)))))

  (testing "returns the accounts list when there is no current asset"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts-with-tokens)))
    (let [result (rf/sub [sub-name])]
      (is (match? result (vals accounts-with-tokens))))))

(h/deftest-sub :wallet/network-preference-details
  [sub-name]
  (testing "returns current viewing account address"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")
           (assoc-in [:wallet :tokens :prices-per-token]
                     {:ETH {:usd 2000} :DAI {:usd 1}})
           (assoc-in [:wallet :networks] network-data)))
    (is
     (match? [{:short-name       "eth"
               :network-name     :mainnet
               :abbreviated-name "Eth."
               :full-name        "Mainnet"
               :chain-id         1
               :related-chain-id nil
               :layer            1}
              {:short-name       "arb1"
               :network-name     :arbitrum
               :abbreviated-name "Arb1."
               :full-name        "Arbitrum"
               :chain-id         42161
               :related-chain-id nil
               :layer            2}
              {:short-name       "oeth"
               :network-name     :optimism
               :abbreviated-name "Oeth."
               :full-name        "Optimism"
               :chain-id         10
               :related-chain-id nil
               :layer            2}]
             (->> (rf/sub [sub-name])
                  ;; Removed `#js source` property for correct compare
                  (map #(dissoc % :source)))))))

(h/deftest-sub :wallet/aggregated-tokens
  [sub-name]
  (testing "returns aggregated tokens from all accounts"
    (swap! rf-db/app-db #(assoc-in % [:wallet :accounts] accounts))
    (let [result                  (rf/sub [sub-name])
          eth-token               (some #(when (= (:symbol %) "ETH") %) result)
          eth-mainnet-raw-balance (get-in eth-token [:balances-per-chain 1 :raw-balance])]
      (is (match? 2 (count result)))
      (is (money/equal-to (money/bignumber 4.5) eth-mainnet-raw-balance)))))

(h/deftest-sub :wallet/aggregated-token-values-and-balance
  [sub-name]
  (testing "returns aggregated tokens (in quo/token-value props) and balances from all accounts"
    (swap! rf-db/app-db #(-> %
                             (assoc :wallet db/defaults)
                             (assoc :currencies currencies)
                             (assoc-in [:wallet :accounts] accounts)
                             (assoc-in [:wallet :tokens :prices-per-token]
                                       {:ETH {:usd 2000} :DAI {:usd 1}})))
    (let [{:keys [formatted-balance tokens]} (rf/sub [sub-name])]
      (is (match? 2 (count tokens)))
      (is (match? "$17003.50" formatted-balance)))))

(h/deftest-sub :wallet/accounts-with-customization-color
  [sub-name]
  (testing "returns all accounts with customization color"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)))
    (is
     (match? [(-> accounts
                  (get "0x1")
                  (assoc :customization-color :blue)
                  (assoc :network-preferences-names #{:mainnet :arbitrum :optimism}))
              (-> accounts
                  (get "0x2")
                  (assoc :customization-color :purple)
                  (assoc :network-preferences-names #{:mainnet :arbitrum :optimism}))
              (-> accounts
                  (get "0x3")
                  (assoc :customization-color :magenta)
                  (assoc :network-preferences-names #{}))]
             (rf/sub [sub-name])))))

(h/deftest-sub :wallet/watch-only-accounts
  [sub-name]
  (testing "returns only active (not watch-only?) accounts"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)))
    (is
     (match? [(-> accounts
                  (get "0x3")
                  (assoc :network-preferences-names #{}))]
             (rf/sub [sub-name])))))

(def chat-account
  {:path     "m/43'/60'/1581'/0'/0"
   :emoji    ""
   :key-uid  "abc"
   :address  "address-1"
   :color-id ""
   :wallet   false
   :name     "My Profile"
   :type     "generated"
   :chat     true
   :color    :blue
   :hidden   false
   :removed  false})

(def operable-wallet-account
  {:path     "m/44'/60'/0'/0/0"
   :emoji    "🤡"
   :key-uid  "abc"
   :address  "address-2"
   :wallet   true
   :name     "My Account"
   :type     "generated"
   :chat     false
   :color    :primary
   :hidden   false
   :operable :fully
   :removed  false})

(def inoperable-wallet-account
  {:path     "m/44'/60'/0'/0/0"
   :emoji    "🧠"
   :key-uid  "def"
   :address  "address-3"
   :wallet   true
   :name     "My Other Account"
   :type     "generated"
   :chat     false
   :color    :primary
   :hidden   false
   :operable :no
   :removed  false})

(def profile-key-pair-key-uid "abc")
(def profile-key-pair-name "My Profile")
(def seed-phrase-key-pair-key-uid "def")
(def seed-phrase-key-pair-name "My Key Pair")

(def profile-keypair
  {:key-uid            profile-key-pair-key-uid
   :name               profile-key-pair-name
   :type               :profile
   :lowest-operability :fully
   :accounts           []})

(def seed-phrase-keypair
  {:key-uid            seed-phrase-key-pair-key-uid
   :name               seed-phrase-key-pair-name
   :type               :seed
   :lowest-operability :no
   :accounts           []})

(h/deftest-sub :wallet/keypairs
  [sub-name]
  (testing "returns keypairs map"
    (swap! rf-db/app-db assoc-in [:wallet :keypairs] {profile-key-pair-key-uid profile-keypair})
    (is (match? {profile-key-pair-key-uid profile-keypair} (rf/sub [sub-name])))))

(h/deftest-sub :wallet/keypairs-list
  [sub-name]
  (swap! rf-db/app-db assoc-in
    [:wallet :keypairs]
    {profile-key-pair-key-uid     profile-keypair
     seed-phrase-key-pair-key-uid seed-phrase-keypair})
  (let [result   (rf/sub [sub-name])
        expected (list profile-keypair seed-phrase-keypair)]
    (is (= 2 (count result)))
    (is (match? expected result))))

(h/deftest-sub :wallet/keypair-names
  [sub-name]
  (swap! rf-db/app-db assoc-in
    [:wallet :keypairs]
    {profile-key-pair-key-uid     profile-keypair
     seed-phrase-key-pair-key-uid seed-phrase-keypair})
  (is (match? #{seed-phrase-key-pair-name profile-key-pair-name} (rf/sub [sub-name]))))

(h/deftest-sub :wallet/settings-keypairs-accounts
  [sub-name]
  (testing "returns formatted key-pairs and accounts"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in
             [:wallet :keypairs]
             {profile-key-pair-key-uid     (update profile-keypair
                                                   :accounts
                                                   conj
                                                   operable-wallet-account)
              seed-phrase-key-pair-key-uid (update seed-phrase-keypair
                                                   :accounts
                                                   conj
                                                   inoperable-wallet-account)})
            (assoc-in
             [:wallet :accounts]
             {(:address operable-wallet-account)   operable-wallet-account
              (:address inoperable-wallet-account) inoperable-wallet-account}))))

    (is
     (match?
      {:missing  [{:name     (:name seed-phrase-keypair)
                   :key-uid  (:key-uid seed-phrase-keypair)
                   :type     (:type seed-phrase-keypair)
                   :accounts [{:customization-color (:color inoperable-wallet-account)
                               :emoji               (:emoji inoperable-wallet-account)
                               :type                :default}]}]
       :operable [{:name     (:name profile-keypair)
                   :key-uid  (:key-uid profile-keypair)
                   :type     (:type profile-keypair)
                   :accounts [{:account-props {:customization-color (:color operable-wallet-account)
                                               :size                32
                                               :emoji               (:emoji operable-wallet-account)
                                               :type                :default
                                               :name                (:name operable-wallet-account)
                                               :address             (:address operable-wallet-account)}
                               :networks      []
                               :state         :default
                               :action        :none}]}]}
      (rf/sub [sub-name]))))

  (testing "allows for passing account format options"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in
             [:wallet :keypairs]
             {profile-key-pair-key-uid (update profile-keypair
                                               :accounts
                                               conj
                                               operable-wallet-account)})
            (assoc-in
             [:wallet :accounts]
             {(:address operable-wallet-account) operable-wallet-account}))))

    (let [{:keys [color
                  name
                  address
                  emoji]} operable-wallet-account
          network-options [{:network-name :ethereum :short-name "eth"}
                           {:network-name :optimism :short-name "oeth"}
                           {:network-name :arbitrum :short-name "arb1"}]
          size-option     20]
      (is
       (match? {:missing  []
                :operable [{:name     (:name profile-keypair)
                            :key-uid  (:key-uid profile-keypair)
                            :type     (:type profile-keypair)
                            :accounts [{:account-props {:customization-color color
                                                        :size                size-option
                                                        :emoji               emoji
                                                        :type                :default
                                                        :name                name
                                                        :address             address}
                                        :networks      network-options
                                        :state         :default
                                        :action        :none}]}]}
               (rf/sub [sub-name
                        {:networks network-options
                         :size     size-option}])))))

  (testing "filters non-wallet accounts"
    (swap! rf-db/app-db
      (fn [db]
        (-> db
            (assoc-in
             [:wallet :keypairs]
             {profile-key-pair-key-uid (update profile-keypair
                                               :accounts
                                               conj
                                               operable-wallet-account
                                               chat-account)})
            (assoc-in
             [:wallet :accounts]
             {(:address operable-wallet-account) operable-wallet-account
              (:address chat-account)            chat-account}))))
    (is
     (match?
      {:missing  []
       :operable [{:name     (:name profile-keypair)
                   :key-uid  (:key-uid profile-keypair)
                   :type     (:type profile-keypair)
                   :accounts [{:account-props {:customization-color (:color operable-wallet-account)
                                               :size                32
                                               :emoji               (:emoji operable-wallet-account)
                                               :type                :default
                                               :name                (:name operable-wallet-account)
                                               :address             (:address operable-wallet-account)}
                               :networks      []
                               :state         :default
                               :action        :none}]}]}
      (rf/sub [sub-name])))))

(def local-suggestions ["a" "b"])

(h/deftest-sub :wallet/local-suggestions
  [sub-name]
  (testing "returns local suggestions:"
    (swap! rf-db/app-db
      #(assoc-in % [:wallet :ui :search-address :local-suggestions] local-suggestions))
    (is (match? local-suggestions (rf/sub [sub-name])))))

(h/deftest-sub :wallet/valid-ens-or-address?
  [sub-name]
  (testing "returns local suggestions:"
    (swap! rf-db/app-db
      #(assoc-in % [:wallet :ui :search-address :valid-ens-or-address?] true))
    (is
     (rf/sub [sub-name]))))

(h/deftest-sub :wallet/selected-keypair-uid
  [sub-name]
  (testing "returns selected keypair uid"
    (swap! rf-db/app-db
      #(assoc-in % [:wallet :ui :create-account :selected-keypair-uid] "key-uid"))
    (is (= "key-uid" (rf/sub [sub-name])))))

(h/deftest-sub :wallet/current-viewing-account-tokens-filtered
  [sub-name]
  (testing "current viewing tokens filtered"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)
           (assoc-in [:wallet :current-viewing-account-address] "0x2")
           (assoc :currencies currencies)
           (assoc-in [:wallet :tokens :prices-per-token]
                     {:ETH {:usd 2000} :DAI {:usd 1}})
           (assoc-in [:profile/profile :currency] :usd)))
    (is (match? (count (rf/sub [sub-name ""])) 2))
    (is (match? (count (rf/sub [sub-name "et"])) 2))))

(h/deftest-sub :wallet/selected-networks->chain-ids
  [sub-name]
  (testing "selected networks -> chain-ids - All networks"
    (swap! rf-db/app-db #(assoc % :wallet db/defaults))
    (is
     (match? (sort [constants/ethereum-mainnet-chain-id constants/arbitrum-mainnet-chain-id
                    constants/optimism-mainnet-chain-id])
             (sort (rf/sub [sub-name])))))
  (testing "selected networks -> chain-ids - specific network"
    (swap! rf-db/app-db #(assoc-in %
                          [:wallet :ui :network-filter :selected-networks]
                          #{constants/optimism-network-name}))
    (is
     (match? (sort [constants/optimism-mainnet-chain-id])
             (sort (rf/sub [sub-name]))))))


(h/deftest-sub :wallet/current-viewing-account-tokens-in-selected-networks
  [sub-name]
  (testing "current account tokens in selected networks"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :ui :network-filter :selected-networks] #{constants/arbitrum-network-name})
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")
           (assoc-in [:wallet :tokens :prices-per-token]
                     {:ETH {:usd 2000} :DAI {:usd 1}})
           (assoc-in [:wallet :networks] network-data)))

    (let [result (rf/sub [sub-name])
          token  (nth result 1)
          chains (-> token
                     :balances-per-chain
                     keys)]
      (is (match? (count chains) 1))
      (is (match? (first chains) constants/arbitrum-mainnet-chain-id)))))

(h/deftest-sub :wallet/aggregated-tokens-in-selected-networks
  [sub-name]
  (testing "aggregated tokens in selected networks"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :ui :network-filter :selected-networks] #{constants/optimism-network-name})
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)))

    (let [result (rf/sub [sub-name])
          token  (first result)
          chains (-> token
                     :balances-per-chain
                     keys)]
      (is (match? (count chains) 1))
      (is (match? (first chains) constants/optimism-mainnet-chain-id)))))

(h/deftest-sub :wallet/aggregated-fiat-balance-per-chain
  [sub-name]
  (testing "aggregated fiat balance per chain"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)
           (assoc-in [:wallet :tokens :prices-per-token] {:ETH {:usd 2000} :DAI {:usd 1}})
           (assoc :currencies currencies)
           (assoc-in [:profile/profile :currency] :usd)))

    (let [result (rf/sub [sub-name])
          chains (keys result)]
      (is (match? (count chains) 3))
      (is (match? (get result constants/ethereum-mainnet-chain-id) "$9002.00"))
      (is (match? (get result constants/optimism-mainnet-chain-id) "$8001.50")))))

(h/deftest-sub :wallet/current-viewing-account-fiat-balance-per-chain
  [sub-name]
  (testing "current viewing account fiat balance per chain"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)
           (assoc-in [:wallet :current-viewing-account-address] "0x2")
           (assoc-in [:wallet :tokens :prices-per-token] {:ETH {:usd 2000} :DAI {:usd 1}})
           (assoc :currencies currencies)
           (assoc-in [:profile/profile :currency] :usd)))

    (let [result (rf/sub [sub-name])
          chains (keys result)]
      (is (match? (count chains) 3))
      (is (match? (get result constants/ethereum-mainnet-chain-id) "$5001.00"))
      (is (match? (get result constants/optimism-mainnet-chain-id) "$6000.00"))
      (is (match? (get result constants/arbitrum-mainnet-chain-id) "$0.00")))))

(h/deftest-sub :wallet/wallet-send-fee-fiat-formatted
  [sub-name]
  (testing "wallet send fee calculated and formatted in fiat"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts-with-tokens)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")
           (assoc-in [:wallet :ui :send :route] route-data)
           (assoc :currencies currencies)
           (assoc-in [:wallet :tokens :prices-per-token]
                     {:ETH {:usd 2000} :DAI {:usd 1}})
           (assoc-in [:profile/profile :currency] :usd)
           (assoc-in [:profile/profile :currency-symbol] "$")))

    (let [token-symbol-for-fees "ETH"
          result                (rf/sub [sub-name token-symbol-for-fees])]
      (is (match? result "$0.20")))))

(h/deftest-sub :wallet/has-partially-operable-accounts?
  [sub-name]
  (testing "returns false if there are no partially operable accounts"
    (swap! rf-db/app-db
      #(assoc-in % [:wallet :accounts] accounts))
    (is (false? (rf/sub [sub-name]))))

  (testing "returns true if there are partially operable accounts"
    (swap! rf-db/app-db
      #(assoc-in %
        [:wallet :accounts]
        (update accounts "0x2" assoc :operable :partially)))
    (is (true? (rf/sub [sub-name])))))

(h/deftest-sub :wallet/zero-balance-in-all-non-watched-accounts?
  [sub-name]
  (testing "returns true if the balance is zero in all non-watched accounts"
    (swap! rf-db/app-db
      #(assoc-in %
        [:wallet :accounts]
        {"0x1" {:address "0x1"
                :watch-only? false
                :tokens
                [{:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}}}
                 {:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}}}]}
         "0x2" {:address "0x2"
                :watch-only? false
                :tokens
                [{:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}}}
                 {:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}}}]}

         "0x3"
         {:address     "0x3"
          :watch-only? true
          :tokens      [{:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance
                                                                                   (money/bignumber
                                                                                    "2")
                                                                                   :has-error false}
                                              constants/optimism-mainnet-chain-id {:raw-balance
                                                                                   (money/bignumber
                                                                                    "1")
                                                                                   :has-error false}
                                              constants/arbitrum-mainnet-chain-id {:raw-balance
                                                                                   (money/bignumber
                                                                                    "0")
                                                                                   :has-error false}}}
                        {:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance
                                                                                   (money/bignumber
                                                                                    "0")
                                                                                   :has-error false}
                                              constants/optimism-mainnet-chain-id {:raw-balance
                                                                                   (money/bignumber
                                                                                    "2")
                                                                                   :has-error false}
                                              constants/arbitrum-mainnet-chain-id {:raw-balance
                                                                                   (money/bignumber
                                                                                    "0")
                                                                                   :has-error
                                                                                   false}}}]}}))
    (is (true? (rf/sub [sub-name]))))
  (testing "returns false if the balance is not zero in all non-watched accounts"
    (swap! rf-db/app-db
      #(assoc-in %
        [:wallet :accounts]
        {"0x1" {:address "0x1"
                :watch-only? false
                :tokens
                [{:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "2")
                                                                            :has-error   false}
                                       constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "1")
                                                                            :has-error   false}
                                       constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}}}
                 {:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "2")
                                                                            :has-error   false}
                                       constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}}}]}
         "0x2" {:address "0x2"
                :watch-only? true
                :tokens
                [{:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "2")
                                                                            :has-error   false}
                                       constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "1")
                                                                            :has-error   false}
                                       constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}}}
                 {:balances-per-chain {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}
                                       constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "2")
                                                                            :has-error   false}
                                       constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                          "0")
                                                                            :has-error   false}}}]}}))
    (is (false? (rf/sub [sub-name])))))

(h/deftest-sub :wallet/selected-keypair-keycard?
  [sub-name]
  (testing "returns true if the selected keypair has keycards"
    (swap! rf-db/app-db
      #(assoc-in %
        [:wallet :keypairs]
        {:keypair-1 {:id       :keypair-1
                     :keycards [:keycard-1 :keycard-2]}}))
    (swap! rf-db/app-db
      #(assoc-in % [:wallet :ui :create-account :selected-keypair-uid] :keypair-1))
    (is (true? (rf/sub [sub-name]))))

  (testing "returns false if the selected keypair has no keycards"
    (swap! rf-db/app-db
      #(assoc-in %
        [:wallet :keypairs]
        {:keypair-2 {:id       :keypair-2
                     :keycards []}}))
    (swap! rf-db/app-db
      #(assoc-in % [:wallet :ui :create-account :selected-keypair-uid] :keypair-2))
    (is (false? (rf/sub [sub-name])))))
