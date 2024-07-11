(ns react-native.wallet-connect
  (:require
    ["@walletconnect/core" :refer [Core]]
    ["@walletconnect/utils" :refer
     [buildApprovedNamespaces getSdkError parseUri]]
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

;; Get an error from this list:
;; https://github.com/WalletConnect/walletconnect-monorepo/blob/c6e9529418a0c81d4efcc6ac4e61f242a50b56c5/packages/utils/src/errors.ts
(defn get-sdk-error
  [error-key]
  (getSdkError error-key))

(defn parse-uri
  [uri]
  (-> uri
      parseUri
      (js->clj :keywordize-keys true)))
