(ns status-im.contexts.wallet.utils
  (:require [clojure.string :as string]
            [status-im.contexts.wallet.common.validation :as validation]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn validate-address
  [known-addresses user-input]
  (cond
    (string/blank? user-input)               nil
    (some #(= % user-input) known-addresses) (i18n/label :t/address-already-in-use)
    (not
     (or (validation/eth-address? user-input)
         (validation/ens-name? user-input))) (i18n/label :t/invalid-address)))

(defn validate-fn
  [entered-address addresses]
  (validate-address addresses (string/lower-case entered-address)))

(defn clear-activity-and-scanned-address
  []
  (rf/dispatch [:wallet/clear-address-activity])
  (rf/dispatch [:wallet/clean-scanned-address]))

(defn on-press-close
  []
  (clear-activity-and-scanned-address)
  (rf/dispatch [:navigate-back]))
