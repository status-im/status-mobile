(ns status-im.contexts.wallet.common.validation
  (:require [clojure.string :as string]
            [status-im.constants :as constants]))

(defn ens-name?
  [s]
  (and (not (string/blank? s))
       (boolean (re-find constants/regx-ens s))))
(defn private-key?
  [s]
  (or (re-find constants/regx-private-key-hex s)
      (re-find constants/regx-private-key s)))
