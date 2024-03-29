(ns legacy.status-im.ethereum.ens
  (:require
    [native-module.core :as native-module]
    [status-im.common.json-rpc.events :as json-rpc]
    [utils.ens.core :as utils.ens]))

(defn address
  [chain-id ens-name cb]
  {:pre [(utils.ens/is-valid-eth-name? ens-name)]}
  (json-rpc/call {:method     "ens_addressOf"
                  :params     [chain-id ens-name]
                  :on-success cb
                  :on-error   #(cb "0x")}))

(defn pubkey
  ([chain-id ens-name on-success on-error]
   {:pre [(utils.ens/is-valid-eth-name? ens-name)]}
   (json-rpc/call {:method     "ens_publicKeyOf"
                   :params     [chain-id ens-name]
                   :on-success on-success
                   :on-error   on-error}))
  ;; At some point, infura started to return "execution reverted" error
  ;; instead of "0x" result. Our code expects "0x" result.
  ([chain-id ens-name cb]
   (pubkey chain-id ens-name cb #(cb "0x"))))

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
  (json-rpc/call {:method     "ens_expireAt"
                  :params     [chain-id ens-name]
                  :on-success
                  ;;NOTE: returns a timestamp in s and we want ms
                  #(cb (* (js/Number (native-module/hex-to-number %)) 1000))}))
