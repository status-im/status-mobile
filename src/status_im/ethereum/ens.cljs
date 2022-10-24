(ns status-im.ethereum.ens
  (:require [clojure.string :as string]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.native-module.core :as status]))

(defn is-valid-eth-name?
  [ens-name]
  (and ens-name
       (string? ens-name)
       (string/ends-with? ens-name ".eth")))

(defn address
  [chain-id ens-name cb]
  (json-rpc/call {:method     "ens_addressOf"
                  :params     [chain-id ens-name]
                  :on-success cb
                  :on-error   #(cb "0x")}))

(defn pubkey
  [chain-id ens-name cb]
  (json-rpc/call {:method     "ens_publicKeyOf"
                  :params     [chain-id ens-name]
                  :on-success cb
                  ;;at some point infura started to return execution reverted error instead of "0x" result
                  ;;our code expects "0x" result
                  :on-error #(cb "0x")}))

(defn owner
  [chain-id ens-name cb]
  (json-rpc/call {:method     "ens_ownerOf"
                  :params     [chain-id ens-name]
                  :on-success cb
                  :on-error   #(cb "0x")}))

(defn resource-url
  [chain-id ens-name cb]
  (json-rpc/call {:method     "ens_resourceURL"
                  :params     [chain-id ens-name]
                  :on-success #(cb (str "https://" (:Host %)))
                  :on-error   #(cb "0x")}))

(defn expire-at
  [chain-id ens-name cb]
  (json-rpc/call {:method "ens_expireAt"
                  :params [chain-id ens-name]
                  :on-success
                  ;;NOTE: returns a timestamp in s and we want ms
                  #(cb (* (js/Number (status/hex-to-number %)) 1000))}))

(defn register-prepare-tx
  [chain-id from ens-name pubkey cb]
  (json-rpc/call {:method "ens_registerPrepareTx"
                  :params [chain-id {:from from} ens-name pubkey]
                  :on-success cb}))

(defn set-pub-key-prepare-tx
  [chain-id from ens-name pubkey cb]
  (json-rpc/call {:method "ens_setPubKeyPrepareTx"
                  :params [chain-id {:from from} ens-name pubkey]
                  :on-success cb}))
