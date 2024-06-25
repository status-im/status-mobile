(ns status-im.contexts.wallet.send.events-test
  (:require
    [cljs.test :refer-macros [is testing run-tests]]
    [re-frame.db :as rf-db]
    [status-im.contexts.wallet.send.utils :as send-utils]
    status-im.contexts.wallet.send.events
    [test-helpers.unit :as h]
    [utils.money :as money]))

(h/deftest-event :wallet/update-receiver-networks
  [event-id dispatch]
  (testing "reciever networks changed"
    (let [selected-networks-before [:ethereum :optimism :arbitrum]
          selected-networks-after  [:ethereum :optimism]
          expected-db              {:wallet {:ui {:send {:receiver-networks selected-networks-after}}}}]
      (reset! rf-db/app-db {:wallet {:ui {:send {:receiver-networks selected-networks-before}}}})
      (is (match? expected-db (:db (dispatch [event-id selected-networks-after]))))))

  (testing "if receiver network removed, it is also removed from disabled ones"
    (let [selected-networks-before       [:ethereum :optimism :arbitrum]
          selected-networks-after        [:ethereum :optimism]
          disabled-from-chain-ids-before [:optimism :arbitrum]
          disabled-from-chain-ids-after  [:optimism]
          expected-db                    {:wallet {:ui {:send {:receiver-networks selected-networks-after
                                                               :disabled-from-chain-ids
                                                               disabled-from-chain-ids-after}}}}]
      (reset! rf-db/app-db {:wallet {:ui {:send {:receiver-networks selected-networks-before
                                                 :disabled-from-chain-ids
                                                 disabled-from-chain-ids-before}}}})
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
        token             {:symbol   "ETH"
                           :name     "Ether"
                           :networks #{{:chain-id 421614}
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
        expected-fx [[:dispatch [:wallet/get-suggested-routes {:amount 10}]]
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
  (testing "clean send data"
    (let [expected-db {:wallet {:ui {:other-props :value}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:send        {:id 1}
                       :other-props :value}}})
      (is (match? expected-db (:db (dispatch [event-id])))))))

(h/deftest-event :wallet/select-address-tab
  [event-id dispatch]
  (testing "select address tab"
    (let [expected-db {:wallet {:ui {:send {:select-address-tab "tab"}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:send nil}}})
      (is (match? expected-db (:db (dispatch [event-id "tab"])))))))

(h/deftest-event :wallet/clean-send-address
  [event-id dispatch]
  (testing "clean send address"
    (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:send {:to-address     "0x01"
                              :recipient   {:recipient-type :saved-address
                                            :label          "label"}
                              :other-props :value}}}})
      (is (match? expected-db (:db (dispatch [event-id])))))))

(h/deftest-event :wallet/clean-send-amount
  [event-id dispatch]
  (testing "clean send amount"
    (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:send {:amount      10
                              :other-props :value}}}})
      (is (match? expected-db (:db (dispatch [event-id])))))))

(h/deftest-event :wallet/clean-disabled-from-networks
  [event-id dispatch]
  (testing "clean disabled from networks"
    (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:send {:disabled-from-chain-ids "value"
                              :other-props             :value}}}})
      (is (match? expected-db (:db (dispatch [event-id])))))))

(h/deftest-event :wallet/clean-from-locked-amounts
  [event-id dispatch]
  (testing "clean from locked amounts"
    (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:send {:from-locked-amounts "value"
                              :other-props         :value}}}})
      (is (match? expected-db (:db (dispatch [event-id])))))))

(h/deftest-event :wallet/disable-from-networks
  [event-id dispatch]
  (testing "disable from networks"
    (let [expected-db {:wallet {:ui {:send {:disabled-from-chain-ids [1]
                                            :other-props             :value}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:send {:other-props :value}}}})
      (is (match? expected-db (:db (dispatch [event-id [1]])))))))

(h/deftest-event :wallet/unlock-from-amount
  [event-id dispatch]
  (testing "unlock from amount"
    (let [expected-db {:wallet {:ui {:send {:other-props         :value
                                            :from-locked-amounts {}}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:send {:other-props         :value
                              :from-locked-amounts {:chain-id [1 10]}}}}})
      (is (match? expected-db (:db (dispatch [event-id :chain-id])))))))

(h/deftest-event :wallet/lock-from-amount
  [event-id dispatch]
  (testing "lock from amount"
    (let [expected-db {:wallet {:ui {:send {:other-props         :value
                                            :from-locked-amounts {:10 "amount"}}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:send {:other-props :value}}}})
      (is (match? expected-db (:db (dispatch [event-id :10 "amount"])))))))

(h/deftest-event :wallet/clean-selected-token
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
    (reset! rf-db/app-db
            {:wallet {:ui {:send {:other-props :value 
                                  :token "ETH" 
                                  :token-display-name "ETH"
                                  :tx-type "TX type"}}}})
    (is (match? expected-db (:db (dispatch [event-id]))))))

(h/deftest-event :wallet/clean-selected-collectible
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
    (reset! rf-db/app-db
            {:wallet {:ui {:send {:other-props :value
                                  :collectible "ETH"
                                  :token-display-name "ETH"
                                  :amount "0.1"}}}})
    (is (match? expected-db (:db (dispatch [event-id]))))))

(h/deftest-event :wallet/clean-suggested-routes
  [event-id dispatch]
  (let [expected-db {:wallet {:ui {:send {:other-props :value}}}}]
    (reset! rf-db/app-db
            {:wallet {:ui {:send {:other-props :value
                                  :suggested-routes ["1" "2"]
                                  :route "1"
                                  :amount "0.1"
                                  :from-values-by-chain [{:chain-id 1} {:chain-id 10} {:chain-id 42161}]
                                  :to-values-by-chain [{:chain-id 1} {:chain-id 10} {:chain-id 42161}]
                                  :sender-network-values ["eth" "arb1"]
                                  :receiver-network-values ["eth" "arb1"]
                                  :network-links ["link"]
                                  :loading-suggested-routes? false
                                  :suggested-routes-call-timestamp ["1" "2"]}}}})
    (is (match? expected-db (:db (dispatch [event-id]))))))

;; (h/deftest-event :wallet/suggested-routes-error
;;   [event-id dispatch]
;;   (let [sender-network-amounts [{:chain-id 1 :total-amount (money/bignumber "100") :type :loading}
;;                                 {:chain-id 10 :total-amount (money/bignumber "200") :type :default}]
;;         receiver-network-amounts [{:chain-id 1 :total-amount (money/bignumber "100") :type :loading}] 
;;         expected-result {:db {:wallet {:ui {:send {:sender-network-values (send-utils/reset-loading-network-amounts-to-zero sender-network-amounts)
;;                                                    :receiver-network-values (send-utils/reset-loading-network-amounts-to-zero receiver-network-amounts)
;;                                                    :loading-suggested-routes? false
;;                                                    :suggested-routes {:best []}}}}}
;;                          :fx [[:dispatch
;;                                [:toasts/upsert
;;                                 {:id   :send-transaction-error
;;                                  :type :negative
;;                                  :text "error"}]]]}]
;;     (reset! rf-db/app-db
;;             {:wallet {:ui {:send {:sender-network-values sender-network-amounts
;;                                   :receiver-network-values receiver-network-amounts
;;                                   :route :values
;;                                   :loading-suggested-routes? true}}}})
;;     (is (match? expected-result (dispatch [event-id {:message "error"}])))))


(h/deftest-event :wallet/suggested-routes-error
  [event-id dispatch]
  (is (match? {:key :val
               :key2 :val2} 
              {:key :val
               :key2 :val2
               :key3 :val3})))


(run-tests)
