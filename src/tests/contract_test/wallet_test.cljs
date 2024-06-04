(ns tests.contract-test.wallet-test
  (:require
    [cljs.test :refer [deftest is use-fixtures]]
    legacy.status-im.events
    legacy.status-im.subs.root
    [promesa.core :as promesa]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.data-store :as data-store]
    status-im.events
    status-im.navigation.core
    status-im.subs.root
    [test-helpers.integration :as h]
    [tests.contract-test.utils :as contract-utils]))

(use-fixtures :each (h/fixture-session))

(def number-of-networks 3)

(defn assert-accounts-get-accounts
  [result]
  (is (true? (some :wallet result)))
  (is (true? (some :chat result)))
  (is (= 2 (count result))))

(deftest accounts-get-accounts-contract-test
  (h/test-async :contract/accounts-get-accounts
    (fn []
      (promesa/let [result (contract-utils/call-rpc "accounts_getAccounts")]
        (assert-accounts-get-accounts result)))))

(defn check-emoji-is-updated
  [test-emoji accounts]
  (let [default-account (contract-utils/get-default-account accounts)]
    (is (= (:emoji default-account) test-emoji))))

(deftest accounts-save-accounts-contract-test
  (h/test-async :contract/accounts-save-account
    (fn []
      (promesa/let [test-emoji      (emoji-picker.utils/random-emoji)
                    account         (contract-utils/call-rpc "accounts_getAccounts")
                    default-account (contract-utils/get-default-account account)
                    _ (contract-utils/call-rpc
                       "accounts_saveAccount"
                       (data-store/<-account (merge default-account {:emoji test-emoji})))
                    accounts        (contract-utils/call-rpc "accounts_getAccounts")]
        (check-emoji-is-updated test-emoji accounts)))))

(defn assert-ethereum-chains
  [response]
  (is (= number-of-networks (count response)))
  (is (some #(= constants/ethereum-mainnet-chain-id (get-in % [:Prod :chainId])) response))
  (is (some #(= constants/optimism-mainnet-chain-id (get-in % [:Prod :chainId])) response))
  (is (some #(= constants/arbitrum-mainnet-chain-id (get-in % [:Prod :chainId])) response))
  (is (some #(= constants/ethereum-sepolia-chain-id (get-in % [:Test :chainId])) response))
  (is (some #(= constants/arbitrum-sepolia-chain-id (get-in % [:Test :chainId])) response))
  (is (some #(= constants/optimism-sepolia-chain-id (get-in % [:Test :chainId])) response)))

(deftest accounts-get-chains-contract-test
  (h/test-async :contract/wallet_get-ethereum-chains
    (fn []
      (promesa/let [response (contract-utils/call-rpc "wallet_getEthereumChains")]
        (assert-ethereum-chains response)))))

(defn assert-wallet-tokens
  [tokens]
  (let [flattened-tokens (mapcat val tokens)]
    (doseq [token flattened-tokens]
      (is (not-empty (:symbol token)))
      (is (:decimals token))
      (is (contains? token :balancesPerChain))
      (is (contains? token :marketValuesPerCurrency))
      (is (contains? (:marketValuesPerCurrency token) :usd))
      (let [balances-per-chain (:balancesPerChain token)]
        (doseq [[_ balance] balances-per-chain]
          (is (contains? balance :rawBalance))
          (let [raw-balance (:rawBalance balance)]
            (is (not-empty raw-balance))
            (is (re-matches #"\d+" raw-balance))))))))

(deftest wallet-get-walet-token-test
  (h/test-async :wallet/get-wallet-token
    (fn []
      (promesa/let [accounts        (contract-utils/call-rpc "accounts_getAccounts")
                    default-address (contract-utils/get-default-address accounts)
                    response        (contract-utils/call-rpc
                                     "wallet_getWalletToken"
                                     [default-address])]
        (assert-wallet-tokens response)))))

(defn assert-address-details
  [result]
  (is (contains? result :address))
  (is (contains? result :path))
  (is (boolean? (:hasActivity result)))
  (is (false? (:alreadyCreated result))))

(deftest wallet-get-address-details-contract-test
  (h/test-async :wallet/get-address-details
    (fn []
      (promesa/let [input       "test.eth"
                    chain-id    constants/ethereum-mainnet-chain-id
                    ens-address (contract-utils/call-rpc "ens_addressOf" chain-id input)
                    response    (contract-utils/call-rpc "wallet_getAddressDetails"
                                                         chain-id
                                                         ens-address)]
        (assert-address-details response)))))

(defn assert-search-ens
  [expected-result actual-result]
  (is (= expected-result actual-result)))

#_(deftest wallet-search-ens-test
    (h/test-async :wallet/search-ens
      (fn []
        (promesa/let [_ (h/enable-testnet!)]
          (let [test-cases [{:description     "Test on Ethereum mainnet"
                             :ens-name        "test.eth"
                             :chain-id        constants/ethereum-mainnet-chain-id
                             :expected-result "0xeefb13c7d42efcc655e528da6d6f7bbcf9a2251d"}
                            {:description     "Test on Goerli testnet"
                             :ens-name        "qoqobolo.stateofus.eth"
                             :chain-id        constants/ethereum-goerli-chain-id
                             :expected-result "0xf8f4f6b5c73f4fb561eb2676326d81b7aba180b0"}
                            {:description     "Test on Sepolia testnet"
                             :ens-name        "code.eth"
                             :chain-id        constants/ethereum-sepolia-chain-id
                             :expected-result "0x035ebd096afa6b98372494c7f08f3402324117d3"}]]
            (promesa/all
             (map (fn [{:keys [ens-name chain-id expected-result]}]
                    (promesa/let [ens-address
                                  (contract-utils/call-rpc "ens_addressOf" chain-id ens-name)]
                      (assert-search-ens expected-result ens-address)))
                  test-cases)))))))
