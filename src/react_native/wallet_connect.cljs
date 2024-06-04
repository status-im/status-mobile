(ns react-native.wallet-connect
  (:require
    ["@walletconnect/core" :refer [Core]]
    ["@walletconnect/utils" :refer [buildApprovedNamespaces getSdkError]]
    ["@walletconnect/web3wallet" :refer [Web3Wallet]]))

(defn- wallet-connect-core
  [project-id]
  (Core. #js {:projectId project-id}))

(defn init
  [project-id metadata]
  (let [core (wallet-connect-core project-id)]
    (Web3Wallet.init
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
