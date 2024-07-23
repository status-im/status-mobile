(ns status-im.contexts.wallet.wallet-connect.signing
  (:require [native-module.core :as native-module]
            [promesa.core :as promesa]
            [status-im.contexts.wallet.wallet-connect.core :as core]
            [status-im.contexts.wallet.wallet-connect.rpc :as rpc]
            [utils.hex :as hex]
            [utils.transforms :as transforms]))

(defn eth-sign
  [password address data]
  (-> {:data     data
       :account  address
       :password password}
      transforms/clj->json
      native-module/sign-message
      (promesa/then core/extract-native-call-signature)))

(defn personal-sign
  [password address data]
  (-> (rpc/wallet-hash-message-eip-191 data)
      (promesa/then #(rpc/wallet-sign-message % address password))
      (promesa/then hex/prefix-hex)))

(defn eth-sign-typed-data
  [password address data chain-id-eip155 version]
  (let [legacy?  (= version :v1)
        chain-id (core/eip155->chain-id chain-id-eip155)]
    (rpc/wallet-safe-sign-typed-data data
                                     address
                                     password
                                     chain-id
                                     legacy?)))
