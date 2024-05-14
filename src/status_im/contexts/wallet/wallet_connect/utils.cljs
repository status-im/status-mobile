(ns status-im.contexts.wallet.wallet-connect.utils
  ;; NOTE: Not sorting namespaces since @walletconnect/react-native-compat should be the first
  #_{:clj-kondo/ignore [:unsorted-required-namespaces]}
  (:require ["@walletconnect/core" :refer [Core]]
            ["@walletconnect/react-native-compat"]
            ["@walletconnect/utils" :refer [buildApprovedNamespaces]]
            ["@walletconnect/web3wallet" :refer [Web3Wallet]]
            [status-im.config :as config]
            [utils.i18n :as i18n]))

(defn- wallet-connect-metadata
  []
  #js
   {:name (i18n/label :t/status)
    :description (i18n/label :t/status-is-a-secure-messaging-app)
    :url "https://status.app"
    :icons
    ["https://res.cloudinary.com/dhgck7ebz/image/upload/f_auto,c_limit,w_1080,q_auto/Brand/Logo%20Section/Mark/Mark_01"]})

(defn- wallet-connect-core
  []
  (Core. #js {:projectId config/WALLET_CONNECT_PROJECT_ID}))

(defn init
  []
  (let [core (wallet-connect-core)]
    (Web3Wallet.init
     (clj->js {:core     core
               :metadata wallet-connect-metadata}))))

(defn build-approved-namespaces
  [proposal supported-namespaces]
  (buildApprovedNamespaces
   (clj->js {:proposal            proposal
             :supportedNamespaces supported-namespaces})))

(defn format-address
  [chain-id address]
  (str chain-id ":" address))
