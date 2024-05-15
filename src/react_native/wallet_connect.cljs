(ns react-native.wallet-connect
  ;; NOTE: Not sorting namespaces since @walletconnect/react-native-compat should be the first
  #_{:clj-kondo/ignore [:unsorted-required-namespaces]}
  (:require ["@walletconnect/react-native-compat"]
            ["@walletconnect/core" :refer [Core]]
            ["@walletconnect/web3wallet" :refer [Web3Wallet]]
            ["@walletconnect/utils" :refer [buildApprovedNamespaces]]))

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
