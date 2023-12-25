(ns status-im.contexts.wallet.common.validation
  (:require [status-im.constants :as constants]))

(defn ens-name? [s] (re-find constants/regx-ens s))
(defn eth-address? [s] (re-find constants/regx-address s))
