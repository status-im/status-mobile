(ns status-im.contexts.wallet.wallet-connect.utils.signing
  (:require
    [native-module.core :as native-module]
    [promesa.core :as promesa]
    [status-im.contexts.wallet.rpc :as wallet-rpc]
    [status-im.contexts.wallet.wallet-connect.utils.data-store :as
     data-store]
    [utils.hex :as hex]
    [utils.transforms :as transforms]))

(defn eth-sign
  [password address data]
  (-> {:data     data
       :account  address
       :password password}
      transforms/clj->json
      native-module/sign-message
      (promesa/then data-store/extract-native-call-signature)))

(defn personal-sign
  [password address data]
  (-> (wallet-rpc/hash-message-eip-191 data)
      (promesa/then #(wallet-rpc/sign-message % address password))
      (promesa/then hex/prefix-hex)))
