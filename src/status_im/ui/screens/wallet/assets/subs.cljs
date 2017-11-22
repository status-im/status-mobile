(ns status-im.ui.screens.wallet.assets.subs
  (:require [re-frame.core :as re-frame]))

;; TODO(goranjovic) - this is currently hardcoded, will be replaced with actual data
;; in a different PR
(re-frame/reg-sub :token-balance
  (fn [_db]
    {:token-symbol "SNT"
     :token-name   "Status"
     :token-value  30
     :usd-value    0.93}))