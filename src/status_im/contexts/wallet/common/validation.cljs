(ns status-im.contexts.wallet.common.validation
  (:require [status-im.constants :as constants]))

(defn ens-name? [s] (boolean (re-find constants/regx-ens s)))
(defn private-key?
  [s]
  (or (re-find constants/regx-private-key-hex s)
      (re-find constants/regx-private-key s)))
