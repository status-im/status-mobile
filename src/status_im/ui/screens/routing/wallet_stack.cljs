(ns status-im.ui.screens.routing.wallet-stack)

(def wallet-stack
  {:name    :wallet-stack
   :screens [:wallet
             :wallet-account
             :add-new-account
             :add-new-account-password
             :account-added
             :collectibles-list
             :wallet-onboarding-setup
             :contact-code
             {:name    :send-transaction-stack
              :screens [:wallet-send-transaction
                        :recent-recipients
                        :select-account
                        :enter-pin-sign
                        :hardwallet-connect-sign
                        :recipient-qr-code
                        :wallet-send-assets]}
             {:name    :request-transaction-stack
              :screens [:wallet-send-transaction-request
                        :wallet-request-assets
                        :recent-recipients]}
             :wallet-transaction-details
             :wallet-settings-hook
             :extension-screen-holder
             :wallet-settings-assets
             :wallet-add-custom-token
             :wallet-custom-token-details
             :currency-settings]
   :config  {:initialRouteName :wallet}})
