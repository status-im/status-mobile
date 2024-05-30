(ns status-im.contexts.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    matcher-combinators.test
    [status-im.constants :as constants]
    [status-im.contexts.wallet.events :as events]))

(def address "0x2ee6138eb9344a8b76eca3cf7554a06c82a1e2d8")

(def raw-account
  {:path "m/44'/60'/0'/0/0"
   :emoji "🛃"
   :key-uid "0xf9b4dc40911638052ef9cbed6e8ac689198d8f11d2235c5d62e2457c1503dc4f"
   :address address
   :wallet true
   :name "Ethereum account"
   :createdAt 1716548742000
   :type "generated"
   :chat false
   :prodPreferredChainIds "1:42161"
   :hidden false
   :position 0
   :clock 1712315009484
   :testPreferredChainIds "11155111:421614"
   :colorId "purple"
   :operable "fully"
   :mixedcase-address "0x2Ee6138eb9344a8b76Eca3cf7554A06C82A1e2D8"
   :public-key
   "0x04ee7c47e4b68cc05dcd3377cbd5cde6be3c89fcf20a981e55e0285ed63a50f51f8b423465eee134c51bb0255e6041e9e5b006054b0fa72a7c76942a5a1a3f4e7e"
   :removed false})

(def account
  {:path "m/44'/60'/0'/0/0"
   :emoji "🛃"
   :key-uid "0xf9b4dc40911638052ef9cbed6e8ac689198d8f11d2235c5d62e2457c1503dc4f"
   :address address
   :color :purple
   :wallet true
   :default-account? true
   :name "Ethereum account"
   :type :generated
   :chat false
   :test-preferred-chain-ids #{11155111 421614}
   :watch-only? false
   :hidden false
   :prod-preferred-chain-ids #{1 42161}
   :position 0
   :clock 1712315009484
   :created-at 1716548742000
   :operable :fully
   :mixedcase-address "0x2Ee6138eb9344a8b76Eca3cf7554A06C82A1e2D8"
   :public-key
   "0x04ee7c47e4b68cc05dcd3377cbd5cde6be3c89fcf20a981e55e0285ed63a50f51f8b423465eee134c51bb0255e6041e9e5b006054b0fa72a7c76942a5a1a3f4e7e"
   :removed false})

(deftest scan-address-success-test
  (let [db {}]
    (testing "scan-address-success"
      (let [expected-db {:wallet {:ui {:scanned-address address}}}
            effects     (events/scan-address-success {:db db} address)
            result-db   (:db effects)]
        (is (match? result-db expected-db))))))

(deftest clean-scanned-address-test
  (let [db {:wallet {:ui {:scanned-address address}}}]
    (testing "clean-scanned-address"
      (let [expected-db {:wallet {:ui {:send            nil
                                       :scanned-address nil}}}
            effects     (events/clean-scanned-address {:db db})
            result-db   (:db effects)]
        (is (match? result-db expected-db))))))

(deftest reset-selected-networks-test
  (testing "reset-selected-networks"
    (let [db          {:wallet {}}
          expected-db {:wallet {:ui {:network-filter {:selector-state :default
                                                      :selected-networks
                                                      (set constants/default-network-names)}}}}
          effects     (events/reset-selected-networks {:db db})
          result-db   (:db effects)]
      (is (match? result-db expected-db)))))

(deftest update-selected-networks-test
  (testing "update-selected-networks"
    (let [db           {:wallet {:ui {:network-filter {:selected-networks
                                                       #{constants/optimism-network-name}
                                                       :selector-state :changed}}}}
          network-name constants/arbitrum-network-name
          expected-db  {:wallet {:ui {:network-filter {:selected-networks
                                                       #{constants/optimism-network-name
                                                         network-name}
                                                       :selector-state :changed}}}}
          props        [network-name]
          effects      (events/update-selected-networks {:db db} props)
          result-db    (:db effects)]
      (is (match? result-db expected-db))))

  (testing "update-selected-networks > if all networks is already selected, update to incoming network"
    (let [db           {:wallet {:ui {:network-filter {:selector-state :default
                                                       :selected-networks
                                                       (set constants/default-network-names)}}}}
          network-name constants/arbitrum-network-name
          expected-db  {:wallet {:ui {:network-filter {:selected-networks #{network-name}
                                                       :selector-state    :changed}}}}
          props        [network-name]
          effects      (events/update-selected-networks {:db db} props)
          result-db    (:db effects)]
      (is (match? result-db expected-db))))

  (testing "update-selected-networks > reset on removing last network"
    (let [db          {:wallet {:ui {:network-filter {:selected-networks
                                                      #{constants/optimism-network-name}
                                                      :selector-state :changed}}}}
          expected-fx [[:dispatch [:wallet/reset-selected-networks]]]
          props       [constants/optimism-network-name]
          effects     (events/update-selected-networks {:db db} props)
          result-fx   (:fx effects)]
      (is (match? result-fx expected-fx)))))

(deftest get-wallet-token-for-all-accounts-test
  (testing "get wallet token for all accounts"
    (let [address-1   "0x1"
          address-2   "0x2"
          cofx        {:db {:wallet {:accounts {address-1 {:address address-1}
                                                address-2 {:address address-2}}}}}
          effects     (events/get-wallet-token-for-all-accounts cofx)
          result-fx   (:fx effects)
          expected-fx [[:dispatch [:wallet/get-wallet-token-for-account address-1]]
                       [:dispatch [:wallet/get-wallet-token-for-account address-2]]]]
      (is (match? expected-fx result-fx)))))

(deftest get-wallet-token-for-account-test
  (testing "get wallet token for account"
    (let [cofx             {:db {}}
          effects          (events/get-wallet-token-for-account cofx [address])
          expected-effects {:db {:wallet {:ui {:tokens-loading {address true}}}}
                            :fx [[:json-rpc/call
                                  [{:method     "wallet_getWalletToken"
                                    :params     [[address]]
                                    :on-success [:wallet/store-wallet-token address]
                                    :on-error   [:wallet/get-wallet-token-for-account-failed
                                                 address]}]]]}]
      (is (match? expected-effects effects)))))

(deftest check-recent-history-for-all-accounts-test
  (testing "check recent history for all accounts"
    (let [address-1   "0x1"
          address-2   "0x2"
          cofx        {:db {:wallet {:accounts {address-1 {:address address-1}
                                                address-2 {:address address-2}}}}}
          effects     (events/check-recent-history-for-all-accounts cofx)
          result-fx   (:fx effects)
          expected-fx [[:dispatch [:wallet/check-recent-history-for-account address-1]]
                       [:dispatch [:wallet/check-recent-history-for-account address-2]]]]
      (is (match? expected-fx result-fx)))))

(deftest process-account-from-signal-test
  (testing "process account from signal"
    (let [cofx             {:db {:wallet {:accounts {}}}}
          effects          (events/process-account-from-signal cofx [raw-account])
          expected-effects {:db {:wallet {:accounts {address account}}}
                            :fx [[:dispatch [:wallet/get-wallet-token-for-account address]]
                                 [:dispatch
                                  [:wallet/request-new-collectibles-for-account-from-signal address]]
                                 [:dispatch [:wallet/check-recent-history-for-account address]]]}]
      (is (match? expected-effects effects)))))
