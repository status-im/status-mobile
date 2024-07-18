(ns status-im.contexts.wallet.common.utils.address
  (:require
   [status-im.constants :as constants]))


(defn eip-155-suffix->eip-3770-prefix
  [eip-155-suffix]
  (case eip-155-suffix
    "0x1" "eth:"
    "0xa4b1" "arb1:"
    "0xa" "oeth:"
    nil))

(defn is-metamask-address?
  [address]
  (re-matches constants/regx-metamask-address address))

(defn metamask-address->status-address
  [metamask-address]
  metamask-address)
