(ns react-native.wallet-connect
  (:require
   ["@walletconnect/core" :as wc-core]
   ["@walletconnect/utils" :as wc-utils]
   ["@walletconnect/web3wallet$default" :as Web3Wallet]))

(defn- wallet-connect-core
  [project-id]
  (new ^js wc-core/Core (clj->js {:projectId project-id})))

(defn init
  [project-id metadata]
  (let [core (wallet-connect-core project-id)]
    (.init ^js Web3Wallet
           (clj->js {:core     core
                     :metadata metadata}))))

(defn build-approved-namespaces
  [proposal supported-namespaces]
  (^js wc-utils/buildApprovedNamespaces
   (clj->js {:proposal            proposal
             :supportedNamespaces supported-namespaces})))

;; Get an error from this list:
;; https://github.com/WalletConnect/walletconnect-monorepo/blob/c6e9529418a0c81d4efcc6ac4e61f242a50b56c5/packages/utils/src/errors.ts
(defn get-sdk-error
  [error-key]
  (^js wc-utils/getSdkError error-key))

(defn parse-uri
  [uri]
  (-> uri
      (^js wc-utils/parseUri)
      (js->clj :keywordize-keys true)))
