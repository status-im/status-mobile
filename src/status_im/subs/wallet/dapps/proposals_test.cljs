(ns status-im.subs.wallet.dapps.proposals-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.root
    status-im.subs.wallet.dapps.core
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def sample-session
  {:session-proposal
   {:id 1716798889093634
    :params
    {:id                 1716798889093634
     :pairingTopic       "9b18e1348817a548bbc97f9b4a09278f4fdf7c984e4a61ddf461bd1f57710d33"
     :expiryTimestamp    1716799189
     :requiredNamespaces {}
     :optionalNamespaces {:eip155
                          {:chains  ["eip155:1" "eip155:42161" "eip155:137" "eip155:43114" "eip155:56"
                                     "eip155:10" "eip155:100"
                                     "eip155:324" "eip155:7777777" "eip155:8453" "eip155:42220"
                                     "eip155:1313161554" "eip155:11155111" "eip155:11155420"]
                           :methods ["personal_sign" "eth_accounts" "eth_requestAccounts"
                                     "eth_sendRawTransaction" "eth_sendTransaction"
                                     "eth_sign" "eth_signTransaction" "eth_signTypedData"
                                     "eth_signTypedData_v3" "eth_signTypedData_v4"
                                     "wallet_addEthereumChain" "wallet_getCallsStatus"
                                     "wallet_getCapabilities" "wallet_getPermissions"
                                     "wallet_registerOnboarding" "wallet_requestPermissions"
                                     "wallet_scanQRCode" "wallet_sendCalls"
                                     "wallet_showCallsStatus" "wallet_switchEthereumChain"
                                     "wallet_watchAsset"]
                           :events  ["chainChanged" "accountsChanged"]}}
     :relays             [{:protocol "irn"}]
     :proposer           {:publicKey "cddea055b8974d93380e6c7e72110145506c06524047866f8034f3db0990137a"
                          :metadata  {:name "Web3Modal"
                                      :description "Web3Modal Laboratory"
                                      :url "https://lab.web3modal.com"
                                      :icons ["https://avatars.githubusercontent.com/u/37784886"]}}}
    :verifyContext {:verified {:verifyUrl  "https://verify.walletconnect.com"
                               :validation "VALID"
                               :origin     "https://lab.web3modal.com"
                               :isScam     false}}}})

(h/deftest-sub :wallet-connect/session-proposer
  [sub-name]
  (testing "Return the session proposer public key and metadata"
    (swap! rf-db/app-db
      assoc-in
      [:wallet-connect/current-proposal :request]
      sample-session)

    (let [proposer (rf/sub [sub-name])]
      (is (= (-> proposer :publicKey)
             (-> sample-session :params :proposer :publicKey)))

      (is (= (-> proposer :metadata :url)

             (-> sample-session :params :proposer :metadata :url))))))

(h/deftest-sub :wallet-connect/session-proposer-name
  [sub-name]
  (testing "Return only the name of the session proposer"
    (swap! rf-db/app-db
      assoc-in
      [:wallet-connect/current-proposal :request]
      sample-session)

    (is (= (-> sample-session :params :proposer :metadata :name)
           (rf/sub [sub-name])))))

(h/deftest-sub :wallet-connect/current-proposal-address
  [sub-name]
  (testing "Return the current proposal account address"
    (let [address "0x1234567890abcdef"]

      (swap! rf-db/app-db assoc-in [:wallet-connect/current-proposal :address] address)

      (is (= address (rf/sub [sub-name]))))

    (testing "Return nil when there is no current proposal account"
      (swap! rf-db/app-db assoc-in [:wallet-connect/current-proposal :address] nil)

      (is (nil? (rf/sub [sub-name]))))))
