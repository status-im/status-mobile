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
  (js/alert "rn.wc/metadata" metadata)
  (js/alert "rn.wc/wallet" Web3Wallet)
  (js/alert "rn.wc/-init" Web3Wallet.-init)
  (js/alert "rn.wc/init fn" Web3Wallet.init)
  (let [core (wallet-connect-core project-id)]
    (js/alert "rn.wc/core" core)
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
