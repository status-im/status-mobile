(ns status-im.ui.screens.wallet.assets.subs
  (:require [re-frame.core :as re-frame]))

;; TODO(goranjovic) - the USD value is currently hardcoded, will be replaced with actual data
;; in a different PR
(re-frame/reg-sub :token-balance
  (fn [{:keys [wallet-selected-asset]}]
    (assoc wallet-selected-asset :usd-value 0.93)))