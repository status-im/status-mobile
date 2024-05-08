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

(def tokens-0x1
  [{:decimals                   1
    :symbol                     "ETH"
    :name                       "Ether"
    :balances-per-chain         {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber "20")
                                                                      :has-error   false}
                                 constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber "10")
                                                                      :has-error   false}}
    :market-values-per-currency {:usd {:price 1000}}}
   {:decimals                   2
    :symbol                     "DAI"
    :name                       "Dai Stablecoin"
    :balances-per-chain         {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "100")
                                                                      :has-error   false}
                                 constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "150")
                                                                      :has-error   false}
                                 constants/arbitrum-mainnet-chain-id {:raw-balance nil :has-error false}}
    :market-values-per-currency {:usd {:price 100}}}])

(def tokens-0x2
  [{:decimals                   3
    :symbol                     "ETH"
    :name                       "Ether"
    :balances-per-chain         {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "2500")
                                                                      :has-error   false}
                                 constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "3000")
                                                                      :has-error   false}
                                 constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "<nil>")
                                                                      :has-error   false}}
    :market-values-per-currency {:usd {:price 200}}}
   {:decimals                   10
    :symbol                     "DAI"
    :name                       "Dai Stablecoin"
    :balances-per-chain         {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "10000000000")
                                                                      :has-error   false}
                                 constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber "0")
                                                                      :has-error   false}
                                 constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "<nil>")
                                                                      :has-error   false}}
    :market-values-per-currency {:usd {:price 1000}}}])

(def tokens-0x3
  [{:decimals                   3
    :symbol                     "ETH"
    :name                       "Ether"
    :balances-per-chain         {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "5000")
                                                                      :has-error   false}
                                 constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "2000")
                                                                      :has-error   false}
                                 constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "<nil>")
                                                                      :has-error   false}}
    :market-values-per-currency {:usd {:price 200}}}
   {:decimals                   10
    :symbol                     "DAI"
    :name                       "Dai Stablecoin"
    :balances-per-chain         {constants/ethereum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "10000000000")
                                                                      :has-error   false}
                                 constants/optimism-mainnet-chain-id {:raw-balance (money/bignumber "0")
                                                                      :has-error   false}
                                 constants/arbitrum-mainnet-chain-id {:raw-balance (money/bignumber
                                                                                    "<nil>")
                                                                      :has-error   false}}
    :market-values-per-currency {:usd {:price 1000}}}])

(def accounts
  {"0x1" {:path                     "m/44'/60'/0'/0/0"
          :emoji                    "😃"
          :key-uid                  "0x2f5ea39"
          :address                  "0x1"
          :wallet                   false
          :name                     "Account One"
          :type                     :generated
          :watch-only?              false
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
          :watch-only?              false
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
          :tokens                   tokens-0x2}
   "0x3" {:path                     ""
          :emoji                    "🎉"
          :key-uid                  "0x2f5ea39"
          :address                  "0x3"
          :wallet                   false
          :name                     "Watched Account 1"
          :type                     :watch
          :watch-only?              true
          :chat                     false
          :test-preferred-chain-ids #{0}
          :color                    :magenta
          :hidden                   false
          :prod-preferred-chain-ids #{0}
          :position                 2
          :clock                    1698945829328
          :created-at               1698928839000
          :operable                 "fully"
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
           :short-name       "opt"
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
           :short-name "opt"
           :chain-id   10
           :layer      2}]})

(h/deftest-sub :wallet/balances-in-selected-networks
  [sub-name]
  (testing "a map: address->balance"
    (swap! rf-db/app-db #(-> %
                             (assoc :wallet db/defaults)
                             (assoc-in [:wallet :accounts] accounts)))
    (let [result      (rf/sub [sub-name])
          balance-0x1 (money/bignumber 3250)
          balance-0x2 (money/bignumber 2100)
          balance-0x3 (money/bignumber 2400)]

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
     (=
      (list {:path                      "m/44'/60'/0'/0/0"
             :emoji                     "😃"
             :key-uid                   "0x2f5ea39"
             :address                   "0x1"
             :wallet                    false
             :name                      "Account One"
             :type                      :generated
             :watch-only?               false
             :chat                      false
             :test-preferred-chain-ids  #{5 420 421613}
             :color                     :blue
             :hidden                    false
             :prod-preferred-chain-ids  #{1 10 42161}
             :network-preferences-names #{:mainnet :arbitrum :optimism}
             :position                  0
             :clock                     1698945829328
             :created-at                1698928839000
             :operable                  "fully"
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
             :chat                      false
             :test-preferred-chain-ids  #{5 420 421613}
             :color                     :purple
             :hidden                    false
             :prod-preferred-chain-ids  #{1 10 42161}
             :network-preferences-names #{:mainnet :arbitrum :optimism}
             :position                  1
             :clock                     1698945829328
             :created-at                1698928839000
             :operable                  "fully"
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
             :chat                      false
             :test-preferred-chain-ids  #{0}
             :color                     :magenta
             :hidden                    false
             :prod-preferred-chain-ids  #{0}
             :network-preferences-names #{}
             :position                  2
             :clock                     1698945829328
             :created-at                1698928839000
             :operable                  "fully"
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
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")
           (assoc-in [:wallet :networks] network-data)))

    (let [result (rf/sub [sub-name])]
      (is
       (= {:path                      "m/44'/60'/0'/0/0"
           :emoji                     "😃"
           :key-uid                   "0x2f5ea39"
           :address                   "0x1"
           :wallet                    false
           :name                      "Account One"
           :type                      :generated
           :watch-only?               false
           :chat                      false
           :test-preferred-chain-ids  #{5 420 421613}
           :color                     :blue
           :hidden                    false
           :prod-preferred-chain-ids  #{1 10 42161}
           :network-preferences-names #{:mainnet :arbitrum :optimism}
           :position                  0
           :clock                     1698945829328
           :created-at                1698928839000
           :operable                  "fully"
           :mixedcase-address         "0x7bcDfc75c431"
           :public-key                "0x04371e2d9d66b82f056bc128064"
           :removed                   false
           :tokens                    tokens-0x1}
          (dissoc result :balance :formatted-balance)))

      (is (money/equal-to (:balance result) (money/bignumber 3250)))
      (is (match? (:formatted-balance result) "$3250.00")))))

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
     (= (list
         {:path                      "m/44'/60'/0'/0/0"
          :emoji                     "😃"
          :key-uid                   "0x2f5ea39"
          :address                   "0x1"
          :wallet                    false
          :name                      "Account One"
          :type                      :generated
          :watch-only?               false
          :chat                      false
          :test-preferred-chain-ids  #{5 420 421613}
          :color                     :blue
          :hidden                    false
          :prod-preferred-chain-ids  #{1 10 42161}
          :network-preferences-names #{:mainnet :arbitrum :optimism}
          :position                  0
          :clock                     1698945829328
          :created-at                1698928839000
          :operable                  "fully"
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
          :chat                      false
          :test-preferred-chain-ids  #{0}
          :color                     :magenta
          :hidden                    false
          :prod-preferred-chain-ids  #{0}
          :network-preferences-names #{}
          :position                  2
          :clock                     1698945829328
          :created-at                1698928839000
          :operable                  "fully"
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
     (=
      (list
       {:path                      "m/44'/60'/0'/0/0"
        :emoji                     "😃"
        :key-uid                   "0x2f5ea39"
        :address                   "0x1"
        :wallet                    false
        :name                      "Account One"
        :type                      :generated
        :watch-only?               false
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
        :operable                  "fully"
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
        :operable                  "fully"
        :mixedcase-address         "0x7bcDfc75c431"
        :public-key                "0x04371e2d9d66b82f056bc128064"
        :removed                   false
        :tokens                    tokens-0x2})
      (rf/sub [sub-name])))))

(h/deftest-sub :wallet/network-preference-details
  [sub-name]
  (testing "returns current viewing account address"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :current-viewing-account-address] "0x1")
           (assoc-in [:wallet :networks] network-data)))
    (is
     (match? [{:short-name       "eth"
               :network-name     :mainnet
               :abbreviated-name "Eth."
               :chain-id         1
               :related-chain-id nil
               :layer            1}
              {:short-name       "arb1"
               :network-name     :arbitrum
               :abbreviated-name "Arb1."
               :chain-id         42161
               :related-chain-id nil
               :layer            2}
              {:short-name       "opt"
               :network-name     :optimism
               :abbreviated-name "Opt."
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
      (is (money/equal-to (money/bignumber 2520) eth-mainnet-raw-balance)))))

(h/deftest-sub :wallet/aggregated-token-values-and-balance
  [sub-name]
  (testing "returns aggregated tokens (in quo/token-value props) and balances from all accounts"
    (swap! rf-db/app-db #(-> %
                             (assoc :wallet db/defaults)
                             (assoc-in [:wallet :accounts] accounts)))
    (let [{:keys [formatted-balance tokens]} (rf/sub [sub-name])]
      (is (match? 2 (count tokens)))
      (is (match? "$2106.00" formatted-balance)))))

(h/deftest-sub :wallet/accounts-with-customization-color
  [sub-name]
  (testing "returns all accounts with customization color"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)))
    (is
     (= [(-> accounts
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
     (= [(-> accounts
             (get "0x3")
             (assoc :network-preferences-names #{}))]
        (rf/sub [sub-name])))))

(def keypairs
  [{:key-uid "abc"}])

(h/deftest-sub :wallet/keypairs
  [sub-name]
  (testing "returns all keypairs"
    (swap! rf-db/app-db
      #(assoc-in % [:wallet :keypairs] keypairs))
    (is
     (= keypairs
        (rf/sub [sub-name])))))

(def local-suggestions ["a" "b"])

(h/deftest-sub :wallet/local-suggestions
  [sub-name]
  (testing "returns local suggestions:"
    (swap! rf-db/app-db
      #(assoc-in % [:wallet :ui :search-address :local-suggestions] local-suggestions))
    (is
     (= local-suggestions
        (rf/sub [sub-name])))))

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
           (assoc-in [:profile/profile :currency] :usd)))
    (is (match? (count (rf/sub [sub-name ""])) 2))
    (is (match? (count (rf/sub [sub-name "et"])) 1))))

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
           (assoc-in [:profile/profile :currency] :usd)))

    (let [result (rf/sub [sub-name])
          chains (keys result)]
      (is (match? (count chains) 3))
      (is (match? (get result constants/ethereum-mainnet-chain-id) "$1504.00"))
      (is (match? (get result constants/optimism-mainnet-chain-id) "$602.00")))))

(h/deftest-sub :wallet/current-viewing-account-fiat-balance-per-chain
  [sub-name]
  (testing "current viewing account fiat balance per chain"
    (swap! rf-db/app-db
      #(-> %
           (assoc-in [:wallet :accounts] accounts)
           (assoc-in [:wallet :networks] network-data)
           (assoc-in [:wallet :current-viewing-account-address] "0x2")
           (assoc-in [:profile/profile :currency] :usd)))

    (let [result (rf/sub [sub-name])
          chains (keys result)]
      (is (match? (count chains) 3))
      (is (match? (get result constants/ethereum-mainnet-chain-id) "$1500.00"))
      (is (match? (get result constants/optimism-mainnet-chain-id) "$600.00"))
      (is (match? (get result constants/arbitrum-mainnet-chain-id) "$0.00")))))
