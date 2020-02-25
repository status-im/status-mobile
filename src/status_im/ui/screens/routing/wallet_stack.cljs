(ns status-im.ui.screens.routing.wallet-stack)

(def wallet-stack
  {:name    :wallet-stack
   :screens (cond-> [:wallet
                     :wallet-account
                     :add-new-account
                     :add-new-account-pin
                     :account-settings
                     :collectibles-list
                     :wallet-onboarding-setup
                     :wallet-transaction-details
                     :wallet-settings-hook
                     :wallet-settings-assets
                     :wallet-add-custom-token
                     :wallet-custom-token-details
                     :currency-settings])
   :config  {:initialRouteName :wallet}})
