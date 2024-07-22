(ns status-im.contexts.wallet.common.utils.address
  (:require
    [status-im.constants :as constants]))

(defn eip-155-suffix->eip-3770-prefix
  [eip-155-suffix]
  (case eip-155-suffix
    "0x1"    "eth:"
    "0xa4b1" "arb1:"
    "0xa"    "oeth:"
    nil))

(defn is-metamask-address?
  [address]
  (re-find constants/regx-metamask-address address))

(defn eip-3770-address?
  [s]
  (re-find constants/regx-eip-3770-address s))

(defn supported-address?
  [s]
  (boolean (or (eip-3770-address? s)
               (is-metamask-address? s))))

(defn metamask-address->status-address
  [metamask-address]
  (when-let [[_ address metamask-network-suffix] (is-metamask-address? metamask-address)]
    (when-let [status-network-prefix (eip-155-suffix->eip-3770-prefix metamask-network-suffix)]
      (str status-network-prefix address))))

(defn supported-address->status-address
  [address]
  (cond
    (eip-3770-address? address)
    address

    (is-metamask-address? address)
    (metamask-address->status-address address)

    :else
    nil))

(defn extract-address-without-chains-info
  [address]
  (re-find constants/regx-address-contains address))
