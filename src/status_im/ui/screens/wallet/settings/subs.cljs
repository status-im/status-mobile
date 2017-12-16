(ns status-im.ui.screens.wallet.settings.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.ethereum.core :as ethereum]))

;; A set of visible-tokens for current network
(re-frame/reg-sub :wallet.settings/visible-tokens
  :<- [:network]
  :<- [:get-current-account]
  (fn [[network current-account]]
    (let [chain (ethereum/network->chain-keyword network)]
      (get-in current-account [:settings :wallet :visible-tokens chain]))))