(ns react-native.wallet-connect
  (:require
    ["@walletconnect/core" :refer [Core]]
    ["@walletconnect/utils" :refer
     [buildApprovedNamespaces getSdkError parseUri]]
    ["@walletconnect/web3wallet" :refer [Web3Wallet] :as web3-wallet]
    [taoensso.timbre :as log]))

(defn- wallet-connect-core
  [project-id]
  (Core. #js {:projectId project-id}))

(defn init
  [project-id metadata]
  (let [core (wallet-connect-core project-id)]
    (log/debug "Web3Wallet value" Web3Wallet)
    (log/debug "web3wallet default import value" web3-wallet)
    (log/debug "WalletConnect Core value" Core)
    (.init Web3Wallet
           (clj->js {:core     core
                     :metadata metadata}))))

(defn build-approved-namespaces
  [proposal supported-namespaces]
  (buildApprovedNamespaces
   (clj->js {:proposal            proposal
             :supportedNamespaces supported-namespaces})))

(defn get-sdk-error
  [error-key]
  (getSdkError error-key))

(defn parse-uri
  [uri]
  (-> uri
      parseUri
      (js->clj :keywordize-keys true)))
