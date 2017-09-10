(ns status-im.ui.screens.wallet.db
  (:require [cljs.spec.alpha :as spec]))

;; (angusiguess) If we add more error types we can treat them as 'one-of' the following
(spec/def :wallet/error #{:error})

(spec/def :wallet/wallet (spec/keys :opt [:wallet/error]))

;; Placeholder namespace for wallet specs, which are a WIP depending on data
;; model we decide on for balances, prices, etc.

;; TODO(oskarth): spec for balance as BigNumber
;; TODO(oskarth): Spec for prices as as: {:from ETH, :to USD, :price 290.11, :last-day 304.17}
