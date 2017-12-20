(ns status-im.ui.screens.wallet.wallet-list.subs
  (:require [re-frame.core :as re-frame]))

;; TODO(jeluard) update when adding multiple wallet support. This will probably require changes to app-db

(re-frame/reg-sub
  :wallet.list/all
  :<- [:portfolio-value]
  (fn [portfolio-value]
    [{:name     "Main wallet"
      :assets   []
      :amount   portfolio-value
      :active?  true}]))
