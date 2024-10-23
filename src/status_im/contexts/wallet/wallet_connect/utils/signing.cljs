(ns status-im.contexts.wallet.wallet-connect.utils.signing
  (:require
    [native-module.core :as native-module]
    [promesa.core :as promesa]
    [status-im.contexts.wallet.wallet-connect.utils.data-store :as
     data-store]
    [status-im.contexts.wallet.wallet-connect.utils.rpc :as rpc]
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
  (-> (rpc/wallet-hash-message-eip-191 data)
      (promesa/then #(rpc/wallet-sign-message % address password))
      (promesa/then hex/prefix-hex)))
