(ns status-im.contexts.wallet.send.events-test
  (:require
    [cljs.test :refer-macros [is testing]]
    matcher-combinators.test
    [re-frame.db :as rf-db]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    status-im.contexts.wallet.send.events
    [status-im.contexts.wallet.send.utils :as send-utils]
    [test-helpers.unit :as h]
    [utils.money :as money]))

(defn collectible-with-balance
  [balance]
  {:name "DOG #1"
   :description
   "dogs are cute and this one is the cutestdogs are cute and this one is the cutest"
   :ownership [{:address "0x01"
                :balance balance}]
   :id {:contract-id {:address  "0x11"
                      :chain-id 1}
        :token-id    "some-id"}})

(h/deftest-event :wallet/update-receiver-networks
  [event-id dispatch]
  (testing "reciever networks changed"
    (let [selected-networks-before [:ethereum :optimism :arbitrum]
          selected-networks-after  [:ethereum :optimism]
          expected-db              {:wallet {:ui {:send {:receiver-networks selected-networks-after}}}}]
      (reset! rf-db/app-db {:wallet {:ui {:send {:receiver-networks selected-networks-before}}}})
      (is (match? expected-db (:db (dispatch [event-id selected-networks-after])))))))

(h/deftest-event :wallet/set-token-to-send
  [event-id dispatch]
  (let [token-symbol      "ETH"
        token             {:symbol   "ETH"
                           :name     "Ether"
                           :networks #{{:chain-id 421614}
                                       {:chain-id 11155420}
                                       {:chain-id 11155111}}}
        receiver-networks [421614 11155420]]
    (testing "can be called with :token"
      (let [initial-db  {:wallet {:ui {:send {:receiver-networks receiver-networks}}}}
            expected-db {:wallet {:ui {:send {:token-display-name                        token-symbol
                                              :token-not-supported-in-receiver-networks? false}}}}
            _ (reset! rf-db/app-db initial-db)
            result      (dispatch [event-id {:token token}])]
        (is (match? expected-db (:db result)))))
    (testing "can be called with :token-symbol"
      (testing "can be called with :token"
        (let [initial-db  {:wallet {:ui {:send {:receiver-networks receiver-networks}}}}
              expected-db {:wallet {:ui {:send {:token-symbol                              token-symbol
                                                :token-not-supported-in-receiver-networks? true}}}}
              _ (reset! rf-db/app-db initial-db)
              result      (dispatch [event-id {:token-symbol token-symbol}])]
          (is (match? expected-db (:db result))))))
    (testing "shouldn't have changes if called without :token or :token-symbol")
    (let [initial-db  {:wallet {:ui {:send {:receiver-networks receiver-networks}}}}
          expected-db nil
          _ (reset! rf-db/app-db initial-db)
          result      (dispatch [event-id {}])]
      (is (match? expected-db (:db result))))
    (testing "should clean :collectible set"
      (let [initial-db  {:wallet {:ui {:send {:receiver-networks receiver-networks
                                              :collectible       "some-collectible"}}}}
            expected-db {:wallet {:ui {:send {:token-display-name                        token-symbol
                                              :token-not-supported-in-receiver-networks? false}}}}
            _ (reset! rf-db/app-db initial-db)
            result      (dispatch [event-id {:token token}])]
        (is (match? expected-db (:db result)))
        (is (match? nil (get-in result [:db :wallet :ui :send :collectible])))))
    (testing "should set :token-not-supported-in-receiver-networks?"
      (let [initial-db  {:wallet {:ui {:send {:receiver-networks []}}}}
            expected-db {:wallet {:ui {:send {:token-display-name                        token-symbol
                                              :token-not-supported-in-receiver-networks? true}}}}
            _ (reset! rf-db/app-db initial-db)
            result      (dispatch [event-id {:token token}])]
        (is (match? expected-db (:db result)))))))

(h/deftest-event :wallet/edit-token-to-send
  [event-id dispatch]
  (let [token-symbol      "ETH"
        token             {:symbol             "ETH"
                           :name               "Ether"
                           :networks           #{{:chain-id 421614}
                                                 {:chain-id 11155420}
                                                 {:chain-id 11155111}}
                           :supported-networks #{{:chain-id 421614}
                                                 {:chain-id 11155420}
                                                 {:chain-id 11155111}}}
        receiver-networks [421614 11155420]]
    (testing "can be called with :token"
      (let [initial-db  {:wallet {:ui {:send {:receiver-networks receiver-networks
                                              :token-display-name "DAI"
                                              :token-not-supported-in-receiver-networks? true}}}}
            expected-db {:wallet {:ui {:send {:token-display-name                        token-symbol
                                              :token-not-supported-in-receiver-networks? false}}}}
            _ (reset! rf-db/app-db initial-db)
            result      (dispatch [event-id token])]
        (is (match? expected-db (:db result)))))
    (testing "should set :token-not-supported-in-receiver-networks?"
      (let [initial-db  {:wallet {:ui {:send {:receiver-networks                         []
                                              :token-display-name                        "DAI"
                                              :token-not-supported-in-receiver-networks? false}}}}
            expected-db {:wallet {:ui {:send {:token-display-name                        token-symbol
                                              :token-not-supported-in-receiver-networks? true}}}}
            _ (reset! rf-db/app-db initial-db)
            result      (dispatch [event-id token])]
        (is (match? expected-db (:db result)))))))

(h/deftest-event :wallet/set-collectible-to-send
  [event-id dispatch]
  (let
    [collectible
     {:data-type 2
      :id
      {:contract-id
       {:chain-id 11155111
        :address  "0x1ed60fedff775d500dde21a974cd4e92e0047cc8"}
       :token-id "15"}
      :contract-type 3
      :collectible-data
      {:name "DOG #1"
       :description
       "dogs are cute and this one is the cutestdogs are cute and this one is the cutest"}
      :collection-data
      {:name "ERC-1155 Faucet"}
      :ownership
      [{:address      "0xf90014b2027e584fc96e6f6c8078998fe46c5ccb"
        :balance      "1"
        :tx-timestamp 1710331776}]
      :preview-url
      {:uri
       "https://ipfs.io/ipfs/bafybeie7b7g7iibpac4k6ydw4m5ivgqw5vov7oyzlf4v5zoor57wokmsxy/isolated-happy-smiling-dog-white-background-portrait-4_1562-693.avif"}}
     initial-db {:wallet {:ui {:send {:token {:symbol "ETH"}}}}}
     _ (reset! rf-db/app-db initial-db)
     result (dispatch [event-id {:collectible collectible}])]
    (testing ":collectible field assigned"
      (is (match? collectible (get-in result [:db :wallet :ui :send :collectible]))))
    (testing ":token should be removed"
      (is (match? nil (get-in result [:db :wallet :ui :send :token]))))
    (testing ":token-display-name assigned"
      (is (match? "DOG #1" (get-in result [:db :wallet :ui :send :token-display-name]))))
    (testing ":tx-type assigned"
      (is (match? :tx/collectible-erc-1155 (get-in result [:db :wallet :ui :send :tx-type]))))
    (testing "amount set if collectible was single"
      (is (match? 1 (get-in result [:db :wallet :ui :send :amount]))))))

(h/deftest-event :wallet/set-collectible-amount-to-send
  [event-id dispatch]
  (let [initial-db  {:wallet {:ui {:send nil}}}
        expected-fx [[:dispatch [:wallet/start-get-suggested-routes {:amount 10}]]
                     [:dispatch
                      [:wallet/wizard-navigate-forward
                       {:current-screen nil :flow-id :wallet-send-flow}]]]
        amount      10
        _ (reset! rf-db/app-db initial-db)
        result      (dispatch [event-id {:amount amount}])]
    (testing "amount set"
      (is (match? amount (get-in result [:db :wallet :ui :send :amount]))))
    (testing "effects match"
      (is (match? expected-fx (:fx result))))))

(h/deftest-event :wallet/set-token-amount-to-send
  [event-id dispatch]
  (let [initial-db {:wallet {:ui {:send {:token {:symbol "ETH"}}}}}
        amount     10
        _ (reset! rf-db/app-db initial-db)
        result     (dispatch [event-id {:amount amount}])]
    (testing "amount set"
      (is (match? amount (get-in result [:db :wallet :ui :send :amount]))))))

(h/deftest-event :wallet/clean-send-data
  [event-id dispatch]
  (let [token-symbol      "ETH"
        token             {:symbol   "ETH"
                           :name     "Ether"
                           :networks #{{:chain-id 421614}
                                       {:chain-id 11155420}
                                       {:chain-id 11155111}}}
        receiver-networks [421614 11155420]
        expected-db       {:wallet {:ui {:other-props :value}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send        {:token-display-name token-symbol
                                   :token              token
                                   :receiver-networks  receiver-networks}
                     :other-props :value}}})
    (is (match-strict? expected-db (:db (dispatch [event-id]))))))

(h/deftest-event :wallet/select-address-tab
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:select-address-tab "tab"}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send nil}}})
    (is (match? expected-db (:db (dispatch [event-id "tab"]))))))

(h/deftest-event :wallet/clean-send-address
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:to-address  "0x01"
                            :recipient   {:recipient-type :saved-address
                                          :label          "label"}
                            :other-props :value}}}})
    (is (match-strict? expected-db (:db (dispatch [event-id]))))))

(h/deftest-event :wallet/clean-send-amount
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:amount      10
                            :other-props :value}}}})
    (is (match-strict? expected-db (:db (dispatch [event-id]))))))

(h/deftest-event :wallet/clean-disabled-from-networks
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:disabled-from-chain-ids [:optimism]
                            :other-props             :value}}}})
    (is (match-strict? expected-db (:db (dispatch [event-id]))))))

(h/deftest-event :wallet/clean-from-locked-amounts
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:from-locked-amounts "value"
                            :other-props         :value}}}})
    (is (match-strict? expected-db (:db (dispatch [event-id]))))))

(h/deftest-event :wallet/disable-from-networks
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:disabled-from-chain-ids [:optimism]
                                          :other-props             :value}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:other-props :value}}}})
    (is (match? expected-db (:db (dispatch [event-id [:optimism]]))))))

(h/deftest-event :wallet/unlock-from-amount
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props         :value
                                          :from-locked-amounts {}}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:other-props         :value
                            :from-locked-amounts {:chain-id [1 10]}}}}})
    (is (match? expected-db (:db (dispatch [event-id :chain-id]))))))

(h/deftest-event :wallet/lock-from-amount
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props         :value
                                          :from-locked-amounts {:amount 10}}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:other-props :value}}}})
    (is (match? expected-db (:db (dispatch [event-id :amount 10]))))))

(h/deftest-event :wallet/clean-selected-token
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:other-props        :value
                            :token              "ETH"
                            :token-display-name "ETH"
                            :tx-type            :tx/collectible-erc-721}}}})
    (is (match-strict? expected-db (:db (dispatch [event-id]))))))

(h/deftest-event :wallet/clean-selected-collectible
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:other-props        :value
                            :collectible        "ETH"
                            :token-display-name "ETH"
                            :amount             10}}}})
    (is (match-strict? expected-db (:db (dispatch [event-id]))))))

(h/deftest-event :wallet/suggested-routes-error
  [event-id dispatch]
  (let [sender-network-amounts   [{:chain-id 1 :total-amount (money/bignumber "100") :type :loading}
                                  {:chain-id 10 :total-amount (money/bignumber "200") :type :default}]
        receiver-network-amounts [{:chain-id 1 :total-amount (money/bignumber "100") :type :loading}]
        expected-result          {:db {:wallet {:ui {:send
                                                     {:sender-network-values
                                                      (send-utils/reset-loading-network-amounts-to-zero
                                                       sender-network-amounts)
                                                      :receiver-network-values
                                                      (send-utils/reset-loading-network-amounts-to-zero
                                                       receiver-network-amounts)
                                                      :loading-suggested-routes? false
                                                      :suggested-routes {:best []}}}}}
                                  :fx [[:dispatch
                                        [:toasts/upsert
                                         {:id   :send-transaction-error
                                          :type :negative
                                          :text "error"}]]]}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:sender-network-values     sender-network-amounts
                            :receiver-network-values   receiver-network-amounts
                            :route                     :values
                            :loading-suggested-routes? true}}}})
    (is (match? expected-result (dispatch [event-id "error"])))))

(h/deftest-event :wallet/reset-network-amounts-to-zero
  [event-id dispatch]
  (let [sender-network-values   [{:chain-id 1 :total-amount (money/bignumber "100") :type :default}
                                 {:chain-id 10 :total-amount (money/bignumber "200") :type :default}]
        receiver-network-values [{:chain-id 1 :total-amount (money/bignumber "100") :type :loading}]
        disabled-from-chain-ids [:ethereum]
        sender-network-zero     (send-utils/reset-network-amounts-to-zero
                                 {:network-amounts    sender-network-values
                                  :disabled-chain-ids disabled-from-chain-ids})
        receiver-network-zero   (send-utils/reset-network-amounts-to-zero
                                 {:network-amounts    receiver-network-values
                                  :disabled-chain-ids []})]
    (testing "if sender-network-value and receiver-network-value are not empty"
      (let [expected-db {:wallet {:ui {:send {:other-props             :value
                                              :sender-network-values   sender-network-zero
                                              :receiver-network-values receiver-network-zero}}}}]
        (reset! rf-db/app-db
          {:wallet {:ui {:send {:other-props             :value
                                :sender-network-values   sender-network-values
                                :receiver-network-values receiver-network-values
                                :network-links           [{:from-chain-id 1
                                                           :to-chain-id   10
                                                           :position-diff 1}]}}}})
        (is (match? expected-db (:db (dispatch [event-id]))))))
    (testing "if only receiver-network-value is empty"
      (let [expected-db {:wallet {:ui {:send {:other-props           :value
                                              :sender-network-values sender-network-zero}}}}]
        (reset! rf-db/app-db
          {:wallet {:ui {:send {:other-props             :value
                                :sender-network-values   sender-network-values
                                :receiver-network-values []
                                :network-links           [{:from-chain-id 1
                                                           :to-chain-id   10
                                                           :position-diff 1}]}}}})
        (is (match? expected-db (:db (dispatch [event-id]))))))
    (testing "if receiver-network-value and sender-network-values are empty"
      (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
        (reset! rf-db/app-db
          {:wallet {:ui {:send {:other-props             :value
                                :sender-network-values   []
                                :receiver-network-values []
                                :network-links           [{:from-chain-id 1
                                                           :to-chain-id   10
                                                           :position-diff 1}]}}}})
        (is (match? expected-db (:db (dispatch [event-id]))))))))

(h/deftest-event :wallet/select-send-address
  [event-id dispatch]
  (let [address     "eth:arb1:0x01"
        prefix      "eth:arb1:"
        to-address  "0x01"
        recipient   {:type  :saved-address
                     :label "0x01...23f"}
        stack-id    :screen/wallet.select-address
        start-flow? false
        tx-type     :tx/collectible-erc-721]
    (testing "testing when collectible balance is more than 1"
      (let [collectible       (collectible-with-balance 2)
            testnet-enabled?  false
            receiver-networks (network-utils/resolve-receiver-networks
                               {:prefix           prefix
                                :testnet-enabled? testnet-enabled?})
            expected-result   {:db {:wallet          {:ui {:send {:other-props :value
                                                                  :recipient recipient
                                                                  :to-address to-address
                                                                  :address-prefix prefix
                                                                  :receiver-preferred-networks
                                                                  receiver-networks
                                                                  :receiver-networks receiver-networks
                                                                  :tx-type tx-type
                                                                  :collectible collectible}}}
                                    :profile/profile {:test-networks-enabled? false}}
                               :fx [nil
                                    [:dispatch
                                     [:wallet/wizard-navigate-forward
                                      {:current-screen stack-id
                                       :start-flow?    start-flow?
                                       :flow-id        :wallet-send-flow}]]]}]
        (reset! rf-db/app-db
          {:wallet          {:ui {:send {:other-props :value
                                         :tx-type     tx-type
                                         :collectible collectible}}}
           :profile/profile {:test-networks-enabled? testnet-enabled?}})
        (is (match? expected-result
                    (dispatch [event-id
                               {:address     address
                                :recipient   recipient
                                :stack-id    stack-id
                                :start-flow? start-flow?}])))))
    (testing "testing when collectible balance is 1"
      (let [collectible       (collectible-with-balance 1)
            testnet-enabled?  false
            receiver-networks (network-utils/resolve-receiver-networks
                               {:prefix           prefix
                                :testnet-enabled? testnet-enabled?})
            expected-result   {:db {:wallet          {:ui {:send {:other-props :value
                                                                  :recipient recipient
                                                                  :to-address to-address
                                                                  :address-prefix prefix
                                                                  :receiver-preferred-networks
                                                                  receiver-networks
                                                                  :receiver-networks receiver-networks
                                                                  :tx-type tx-type
                                                                  :collectible collectible}}}
                                    :profile/profile {:test-networks-enabled? false}}
                               :fx [[:dispatch [:wallet/start-get-suggested-routes {:amount 1}]]
                                    [:dispatch
                                     [:wallet/wizard-navigate-forward
                                      {:current-screen stack-id
                                       :start-flow?    start-flow?
                                       :flow-id        :wallet-send-flow}]]]}]
        (reset! rf-db/app-db
          {:wallet          {:ui {:send {:other-props :value
                                         :tx-type     tx-type
                                         :collectible collectible}}}
           :profile/profile {:test-networks-enabled? testnet-enabled?}})
        (is (match? expected-result
                    (dispatch [event-id
                               {:address     address
                                :recipient   recipient
                                :stack-id    stack-id
                                :start-flow? start-flow?}])))))))

(h/deftest-event :wallet/suggested-routes-success
  [event-id dispatch]
  (let [timestamp                     :timestamp
        suggested-routes              {:Best
                                       [{:From           {:isTest    false
                                                          :chainName "Arbitrum"
                                                          :chainId   42161}
                                         :AmountInLocked false
                                         :AmountIn       "0x5af3107a4000"
                                         :MaxAmountIn    "0x4f7920c6831d6"
                                         :GasFees        {:gasPrice       "0.01"
                                                          :baseFee        "0.008750001"
                                                          :eip1559Enabled true}
                                         :BridgeName     "Transfer"
                                         :AmountOut      "0x5af3107a4000"
                                         :To             {:isTest    false
                                                          :chainName "Arbitrum"
                                                          :chainId   42161}
                                         :Cost           "0.006539438247064285301"
                                         :GasAmount      108197}]
                                       :Candidates
                                       [{:From           {:isTest    false
                                                          :chainName "Arbitrum"
                                                          :chainId   42161}
                                         :AmountInLocked false
                                         :AmountIn       "0x5af3107a4000"
                                         :MaxAmountIn    "0x4f7920c6831d6"
                                         :GasFees        {:gasPrice       "0.01"
                                                          :baseFee        "0.008750001"
                                                          :eip1559Enabled true}
                                         :BridgeName     "Transfer"
                                         :AmountOut      "0x5af3107a4000"
                                         :To             {:isTest    false
                                                          :chainName "Arbitrum"
                                                          :chainId   42161}
                                         :Cost           "0.006539438247064285301"
                                         :GasAmount      108197}
                                        {:From           {:isTest    false
                                                          :chainName "Ethereum"
                                                          :chainId   1}
                                         :AmountInLocked false
                                         :AmountIn       "0x0"
                                         :MaxAmountIn    "0x245aa392272e6"
                                         :GasFees        {:gasPrice       "1.01"
                                                          :baseFee        "1.008750001"
                                                          :eip1559Enabled true}
                                         :BridgeName     "Transfer"
                                         :AmountOut      "0x0"
                                         :To             {:isTest    false
                                                          :chainName "Arbitrum"
                                                          :chainId   42161}
                                         :Cost           "1.906539438247064285301"
                                         :GasAmount      23487}]
                                       :NativeChainTokenPrice 123
                                       :TokenPrice 123}
        suggested-routes-data         suggested-routes
        chosen-route                  (:best suggested-routes-data)
        token-symbol                  "ETH"
        token                         {:symbol   "ETH"
                                       :name     "Ether"
                                       :networks #{{:chain-id 421614}
                                                   {:chain-id 11155420}
                                                   {:chain-id 11155111}}}
        token-networks                (:networks token)
        routes-available?             (pos? (count chosen-route))
        sender-network-values         [1 10]
        receiver-network-values       [1 10]
        receiver-networks             [1 10 421614]
        token-decimals                (if (collectible-with-balance 1) 0 (:decimals token))
        native-token?                 (and token (= token-symbol "ETH"))
        from-network-amounts-by-chain (send-utils/network-amounts-by-chain {:route chosen-route
                                                                            :token-decimals
                                                                            token-decimals
                                                                            :native-token?
                                                                            native-token?
                                                                            :receiver? false})
        to-network-amounts-by-chain   (send-utils/network-amounts-by-chain {:route chosen-route
                                                                            :token-decimals
                                                                            token-decimals
                                                                            :native-token?
                                                                            native-token?
                                                                            :receiver? true})
        to-network-values-for-ui      (send-utils/network-values-for-ui to-network-amounts-by-chain)
        tx-type                       :tx/collectible-erc-1155
        from-network-values-for-ui    (send-utils/network-values-for-ui from-network-amounts-by-chain)
        disabled-from-chain-ids       [:421614]
        token-networks-ids            (when token-networks (mapv #(:chain-id %) token-networks))
        sender-possible-chain-ids     (mapv :chain-id sender-network-values)
        receiver-network-values       (if routes-available?
                                        (send-utils/network-amounts
                                         {:network-values     to-network-values-for-ui
                                          :disabled-chain-ids disabled-from-chain-ids
                                          :receiver-networks  receiver-networks
                                          :token-networks-ids token-networks-ids
                                          :tx-type            tx-type
                                          :receiver?          true})
                                        (cond->
                                          (send-utils/reset-loading-network-amounts-to-zero
                                           receiver-network-values)

                                          (not= tx-type :tx/bridge)
                                          send-utils/safe-add-type-edit))
        sender-network-values         (if routes-available?
                                        (send-utils/network-amounts
                                         {:network-values
                                          (if (= tx-type :tx/bridge)
                                            from-network-values-for-ui
                                            (send-utils/add-zero-values-to-network-values
                                             from-network-values-for-ui
                                             sender-possible-chain-ids))
                                          :disabled-chain-ids disabled-from-chain-ids
                                          :receiver-networks receiver-networks
                                          :token-networks-ids token-networks-ids
                                          :from-locked-amounts {}
                                          :tx-type tx-type
                                          :receiver? false})
                                        (send-utils/reset-loading-network-amounts-to-zero
                                         sender-network-values))
        expected-db                   {:wallet {:ui {:send
                                                     {:other-props :value
                                                      :suggested-routes suggested-routes-data
                                                      :route chosen-route
                                                      :token token
                                                      :disabled-from-chain-ids disabled-from-chain-ids
                                                      :suggested-routes-call-timestamp timestamp
                                                      :collectible (collectible-with-balance 1)
                                                      :token-display-name token-symbol
                                                      :receiver-networks receiver-networks
                                                      :tx-type tx-type
                                                      :from-values-by-chain from-network-values-for-ui
                                                      :to-values-by-chain to-network-values-for-ui
                                                      :sender-network-values sender-network-values
                                                      :receiver-network-values receiver-network-values
                                                      :network-links (when routes-available?
                                                                       (send-utils/network-links
                                                                        chosen-route
                                                                        sender-network-values
                                                                        receiver-network-values))
                                                      :loading-suggested-routes? false
                                                      :from-locked-amounts {}}}}}]
    (reset! rf-db/app-db
      {:wallet {:ui {:send {:other-props                     :value
                            :suggested-routes-call-timestamp timestamp
                            :token                           token
                            :collectible                     (collectible-with-balance 1)
                            :token-display-name              token-symbol
                            :receiver-networks               receiver-networks
                            :receiver-network-values         [1 10]
                            :sender-network-values           [1 10]
                            :tx-type                         tx-type
                            :disabled-from-chain-ids         disabled-from-chain-ids
                            :from-locked-amounts             {}}}}})
    (is (match? expected-db (:db (dispatch [event-id suggested-routes timestamp]))))))

(h/deftest-event :wallet/add-authorized-transaction
  [event-id dispatch]
  (let [hashes          {:chain-1 ["tx-1" "tx-2" "tx-3"]
                         :chain-2 ["tx-4" "tx-5"]
                         :chain-3 ["tx-6" "tx-7" "tx-8" "tx-9"]}
        transaction-id  "txid-1"
        expected-result {:db {:wallet {:ui           {:send {:transaction-ids ["tx-1" "tx-2" "tx-3"
                                                                               "tx-4" "tx-5" "tx-6"
                                                                               "tx-7" "tx-8" "tx-9"]}}
                                       :transactions (send-utils/map-multitransaction-by-ids
                                                      transaction-id
                                                      hashes)}}
                         :fx [[:dispatch
                               [:wallet/stop-and-clean-suggested-routes]]
                              [:dispatch [:wallet/end-transaction-flow]]
                              [:dispatch-later
                               [{:ms       2000
                                 :dispatch [:wallet/clean-just-completed-transaction]}]]]}]
    (is (match? expected-result
                (dispatch [event-id
                           {:id     transaction-id
                            :hashes hashes}])))))

(h/deftest-event :wallet/select-from-account
  [event-id dispatch]
  (let [stack-id    :screen/stack
        start-flow? false
        address     "0x01"]
    (testing "when tx-type is :tx/bridge and token-symbol is nil"
      (let [flow-id         :wallet-bridge-flow
            tx-type         :tx/bridge
            expected-result {:db {:wallet {:ui {:send {:to-address address
                                                       :tx-type    tx-type}}}}
                             :fx [[:dispatch [:wallet/switch-current-viewing-account address]]
                                  [:dispatch
                                   [:wallet/wizard-navigate-forward
                                    {:current-screen stack-id
                                     :start-flow?    start-flow?
                                     :flow-id        flow-id}]]]}]
        (reset! rf-db/app-db {:wallet {:ui {:send {:tx-type tx-type}}}})
        (is (match? expected-result
                    (dispatch [event-id
                               {:address     address
                                :stack-id    stack-id
                                :start-flow? start-flow?}])))))
    (testing "when tx-type is not :tx/bridge and token-symbol is nil"
      (let [flow-id         :wallet-send-flow
            tx-type         :tx/collectible-erc-721
            expected-result {:db {:wallet {:ui {:send {:tx-type tx-type}}}}
                             :fx [[:dispatch [:wallet/switch-current-viewing-account address]]
                                  [:dispatch
                                   [:wallet/wizard-navigate-forward
                                    {:current-screen stack-id
                                     :start-flow?    start-flow?
                                     :flow-id        flow-id}]]]}]
        (reset! rf-db/app-db {:wallet {:ui {:send {:tx-type tx-type}}}})
        (is (match? expected-result
                    (dispatch [event-id
                               {:address     address
                                :stack-id    stack-id
                                :start-flow? start-flow?}])))))
    (testing "when tx-type is :tx/bridge and token-symbol is not nil"
      (let [flow-id         :wallet-bridge-flow
            tx-type         :tx/bridge
            tokens          [{:symbol             "ETH"
                              :chain-id           1
                              :balances-per-chain {1     {:raw-balance (money/bignumber 100)}
                                                   10    {:raw-balance (money/bignumber 200)}
                                                   42161 {:raw-balance (money/bignumber 300)}}
                              :decimals           2}]
            network-details #{{:chain-id 1}
                              {:chain-id 10}
                              {:chain-id 42161}}
            expected-result {:db {:wallet {:ui       {:send {:to-address   address
                                                             :tx-type      tx-type
                                                             :token-symbol "ETH"
                                                             :token        (assoc (first tokens)
                                                                                  :networks #{nil}
                                                                                  :total-balance
                                                                                  (money/bignumber 6))}}
                                           :accounts {address {:tokens tokens}}}}
                             :fx [[:dispatch [:wallet/switch-current-viewing-account address]]
                                  [:dispatch
                                   [:wallet/wizard-navigate-forward
                                    {:current-screen stack-id
                                     :start-flow?    start-flow?
                                     :flow-id        flow-id}]]]}]
        (reset! rf-db/app-db {:wallet {:ui       {:send {:tx-type      tx-type
                                                         :token-symbol "ETH"}}
                                       :accounts {address {:tokens tokens}}}})
        (is (match? expected-result
                    (dispatch [event-id
                               {:address        address
                                :stack-id       stack-id
                                :start-flow?    start-flow?
                                :netork-details network-details}])))))
    (testing "when tx-type is not :tx/bridge and token-symbol is not nil"
      (let [flow-id         :wallet-send-flow
            tx-type         :tx/collectible-erc-721
            tokens          [{:symbol             "ETH"
                              :chain-id           1
                              :balances-per-chain {1     {:raw-balance (money/bignumber 100)}
                                                   10    {:raw-balance (money/bignumber 200)}
                                                   42161 {:raw-balance (money/bignumber 300)}}
                              :decimals           2}]
            network-details #{{:chain-id 1}
                              {:chain-id 10}
                              {:chain-id 42161}}
            expected-result {:db {:wallet {:ui       {:send {:tx-type      tx-type
                                                             :token-symbol "ETH"
                                                             :token        (assoc (first tokens)
                                                                                  :networks #{nil}
                                                                                  :total-balance
                                                                                  (money/bignumber 6))}}
                                           :accounts {address {:tokens tokens}}}}
                             :fx [[:dispatch [:wallet/switch-current-viewing-account address]]
                                  [:dispatch
                                   [:wallet/wizard-navigate-forward
                                    {:current-screen stack-id
                                     :start-flow?    start-flow?
                                     :flow-id        flow-id}]]]}]
        (reset! rf-db/app-db {:wallet {:ui       {:send {:tx-type      tx-type
                                                         :token-symbol "ETH"}}
                                       :accounts {address {:tokens tokens}}}})
        (is (match? expected-result
                    (dispatch [event-id
                               {:address        address
                                :stack-id       stack-id
                                :start-flow?    start-flow?
                                :netork-details network-details}])))))))
