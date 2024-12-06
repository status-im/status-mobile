(ns status-im.contexts.wallet.events-test
  (:require
    [cljs.test :refer-macros [is testing]]
    [matcher-combinators.matchers :as matchers]
    matcher-combinators.test
    [re-frame.db :as rf-db]
    [status-im.constants :as constants]
    status-im.contexts.wallet.events
    [test-helpers.unit :as h]))

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
   :operable? true
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

(h/deftest-event :wallet/scan-address-success
  [event-id dispatch]
  (is (match? {:wallet {:ui {:scanned-address address}}}
              (:db (dispatch [event-id address])))))

(h/deftest-event :wallet/clean-scanned-address
  [event-id dispatch]
  (reset! rf-db/app-db {:wallet {:ui {:scanned-address address}}})
  (is (match? {:wallet {:ui {}}}
              (:db (dispatch [event-id])))))

(h/deftest-event :wallet/reset-selected-networks
  [event-id dispatch]
  (reset! rf-db/app-db {:wallet {}})
  (is (match? {:wallet
               {:ui {:network-filter {:selector-state :default
                                      :selected-networks
                                      (set constants/default-network-names)}}}}
              (:db (dispatch [event-id])))))

(h/deftest-event :wallet/update-selected-networks
  [event-id dispatch]
  (testing "update-selected-networks"
    (let [network-name constants/arbitrum-network-name
          expected-db  {:wallet {:ui {:network-filter {:selected-networks
                                                       #{constants/optimism-network-name
                                                         network-name}
                                                       :selector-state :changed}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:network-filter {:selected-networks
                                        #{constants/optimism-network-name}
                                        :selector-state :changed}}}})
      (is (match? expected-db (:db (dispatch [event-id network-name]))))))

  (testing "update-selected-networks > if all networks is already selected, update to incoming network"
    (let [network-name constants/arbitrum-network-name
          expected-db  {:wallet {:ui {:network-filter {:selected-networks #{network-name}
                                                       :selector-state    :changed}}}}]
      (reset! rf-db/app-db
        {:wallet {:ui {:network-filter {:selector-state :default
                                        :selected-networks
                                        (set constants/default-network-names)}}}})
      (is (match? expected-db (:db (dispatch [event-id network-name]))))))

  (testing "update-selected-networks > reset on removing last network"
    (let [expected-fx [[:dispatch [:wallet/reset-selected-networks]]]]
      (reset! rf-db/app-db
        {:wallet {:ui {:network-filter {:selected-networks
                                        #{constants/optimism-network-name}
                                        :selector-state :changed}}}})
      (is (match? expected-fx
                  (:fx (dispatch [event-id constants/optimism-network-name])))))))

(h/deftest-event :wallet/get-wallet-token-for-all-accounts
  [event-id dispatch]
  (let [address-1 "0x1"
        address-2 "0x2"]
    (reset! rf-db/app-db {:wallet {:accounts {address-1 {:address address-1}
                                              address-2 {:address address-2}}}})
    (is (match? [[:dispatch [:wallet/get-wallet-token-for-account address-1]]
                 [:dispatch [:wallet/get-wallet-token-for-account address-2]]]
                (:fx (dispatch [event-id]))))))

(h/deftest-event :wallet/get-wallet-token-for-account
  [event-id dispatch]
  (let [expected-effects {:db {:wallet {:ui {:tokens-loading {address true}}}}
                          :fx [[:json-rpc/call
                                [{:method     "wallet_fetchOrGetCachedWalletBalances"
                                  :params     [[address]]
                                  :on-success [:wallet/store-wallet-token address]
                                  :on-error   [:wallet/get-wallet-token-for-account-failed
                                               address]}]]]}]
    (is (match? expected-effects (dispatch [event-id address])))))

(h/deftest-event :wallet/reconcile-keypairs
  [event-id dispatch]
  (let [keypair-key-uid (:key-uid raw-account)]
    (testing "event adds new key pairs"
      (reset! rf-db/app-db {:wallet {:accounts {}
                                     :keypairs {}}})
      (is
       (match?
        (matchers/match-with
         [set? matchers/set-equals
          vector? matchers/equals
          map? matchers/equals]
         {:db {:wallet {:accounts {(:address account) account}
                        :keypairs {keypair-key-uid {:key-uid            keypair-key-uid
                                                    :type               :seed
                                                    :lowest-operability :fully
                                                    :accounts           [account]}}}}
          :fx [[:dispatch [:wallet/fetch-assets-for-address address]]]})
        (dispatch [event-id
                   [{:key-uid  keypair-key-uid
                     :type     "seed"
                     :accounts [raw-account]}]]))))
    (testing "event removes key pairs and accounts that are marked as removed"
      (reset! rf-db/app-db {:wallet {:accounts {(:address account) account}
                                     :keypairs {keypair-key-uid
                                                {:key-uid            keypair-key-uid
                                                 :type               :seed
                                                 :lowest-operability :fully
                                                 :accounts           [account]}}}})

      (is
       (match?
        (matchers/match-with
         [set? matchers/set-equals
          vector? matchers/equals
          map? matchers/equals]
         {:db {:wallet {:accounts {}
                        :keypairs {}}}})
        (dispatch [event-id
                   [{:key-uid  keypair-key-uid
                     :type     "seed"
                     :removed  true
                     :accounts [raw-account]}]]))))
    (testing "event removes accounts not present with key pair"
      (reset! rf-db/app-db {:wallet {:accounts {(:address account) account
                                                "1x001"            (assoc account
                                                                          :address "1x001"
                                                                          :key-uid "0x001")}
                                     :keypairs {keypair-key-uid
                                                {:key-uid            keypair-key-uid
                                                 :type               :seed
                                                 :lowest-operability :fully
                                                 :accounts           [account]}
                                                "0x001"
                                                {:key-uid            "0x001"
                                                 :type               :seed
                                                 :lowest-operability :fully
                                                 :accounts           [(assoc account
                                                                             :address "1x001"
                                                                             :key-uid "0x001")]}}}})
      (is
       (match?
        (matchers/match-with
         [set? matchers/set-equals
          vector? matchers/equals
          map? matchers/equals]
         {:db {:wallet {:accounts {(:address account) account}
                        :keypairs {keypair-key-uid
                                   {:key-uid            keypair-key-uid
                                    :type               :seed
                                    :lowest-operability :fully
                                    :accounts           [account]}
                                   "0x001"
                                   {:key-uid            "0x001"
                                    :type               :seed
                                    :lowest-operability :fully
                                    :accounts           []}}}}})
        (dispatch [event-id
                   [{:key-uid  "0x001"
                     :type     "seed"
                     :accounts []}]]))))
    (testing "event updates existing key pairs"
      (reset! rf-db/app-db {:wallet
                            {:accounts {(:address account)
                                        (assoc account :operable :no)}
                             :keypairs {keypair-key-uid
                                        {:key-uid            keypair-key-uid
                                         :type               :seed
                                         :lowest-operability :no
                                         :accounts           [(assoc account :operable :no)]}}}})
      (is
       (match?
        (matchers/match-with
         [set? matchers/set-equals
          vector? matchers/equals
          map? matchers/equals]
         {:db {:wallet {:accounts {(:address account) account}
                        :keypairs {keypair-key-uid
                                   {:key-uid            keypair-key-uid
                                    :type               :seed
                                    :lowest-operability :fully
                                    :accounts           [account]}}}}})
        (dispatch [event-id
                   [{:key-uid  keypair-key-uid
                     :type     "seed"
                     :accounts [raw-account]}]]))))
    (testing "event ignores chat accounts for key pairs"
      (reset! rf-db/app-db {:wallet {:accounts {(:address account) account}
                                     :keypairs {keypair-key-uid
                                                {:key-uid            keypair-key-uid
                                                 :type               :profile
                                                 :lowest-operability :fully
                                                 :accounts           [account
                                                                      (assoc account
                                                                             :address "1x001"
                                                                             :chat    true)]}}}})
      (is
       (match?
        (matchers/match-with
         [set? matchers/set-equals
          vector? matchers/equals
          map? matchers/equals]
         {:db {:wallet {:accounts {(:address account) account}
                        :keypairs {keypair-key-uid
                                   {:key-uid            keypair-key-uid
                                    :type               :profile
                                    :lowest-operability :fully
                                    :accounts           [account
                                                         (assoc account
                                                                :address "1x001"
                                                                :chat    true)]}}}}})
        (dispatch [event-id
                   [{:key-uid  keypair-key-uid
                     :type     "profile"
                     :accounts [raw-account
                                (assoc raw-account
                                       :address "1x001"
                                       :chat    true)]}]]))))))

(h/deftest-event :wallet/reconcile-watch-only-accounts
  [event-id dispatch]
  (testing "event adds new watch-only accounts"
    (reset! rf-db/app-db {:wallet {:accounts {}}})
    (is
     (match?
      (matchers/match-with
       [set? matchers/set-equals
        vector? matchers/equals
        map? matchers/equals]
       {:db {:wallet {:accounts {(:address account) account}}}
        :fx [[:dispatch [:wallet/fetch-assets-for-address address]]]})
      (dispatch [event-id [raw-account]]))))
  (testing "event removes watch-only accounts that are marked as removed"
    (reset! rf-db/app-db {:wallet {:accounts {(:address account) account}}})
    (is
     (match?
      (matchers/match-with
       [set? matchers/set-equals
        vector? matchers/equals
        map? matchers/equals]
       {:db {:wallet {:accounts {}}}})
      (dispatch [event-id [(assoc raw-account :removed true)]]))))
  (testing "event updates existing watch-only accounts"
    (reset! rf-db/app-db {:wallet
                          {:accounts {address account}}})
    (is
     (match?
      (matchers/match-with
       [set? matchers/set-equals
        vector? matchers/equals
        map? matchers/equals]
       {:db {:wallet {:accounts {address (assoc account :name "Test")}}}})
      (dispatch [event-id
                 [(assoc raw-account
                         :address address
                         :name    "Test")]])))))
