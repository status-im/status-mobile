(ns status-im.ui.screens.routing.wallet-stack
  (:require [status-im.utils.config :as config]))

(def wallet-stack
  {:name    :wallet-stack
   :screens (cond-> [:wallet
                     :wallet-account
                     :add-new-account
                     :add-watch-account
                     :add-new-account-password
                     :add-new-account-pin
                     :account-added
                     :account-settings
                     :collectibles-list
                     :wallet-onboarding-setup
                     :wallet-transaction-details
                     :wallet-settings-hook
                     :wallet-settings-assets
                     :wallet-add-custom-token
                     :wallet-custom-token-details
                     :currency-settings]
              config/hardwallet-enabled?
              (concat [:keycard-connection-lost
                       :keycard-processing]))
   :config  {:initialRouteName :wallet}})
