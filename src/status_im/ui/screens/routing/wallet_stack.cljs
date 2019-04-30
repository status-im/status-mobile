(ns status-im.ui.screens.routing.wallet-stack)

(def wallet-stack
  {:name    :wallet-stack
   :screens [:wallet
             :collectibles-list
             :wallet-onboarding-setup
             :wallet-send-transaction-chat
             :contact-code
             {:name    :send-transaction-stack
              :screens [:wallet-send-transaction
                        :recent-recipients
                        :wallet-transaction-sent
                        :enter-pin-sign
                        :hardwallet-connect-sign
                        :recipient-qr-code
                        :wallet-send-assets]}
             {:name    :request-transaction-stack
              :screens [:wallet-request-transaction
                        :wallet-send-transaction-request
                        :wallet-request-assets
                        :recent-recipients]}
             :unsigned-transactions
             :transactions-history
             :wallet-transaction-details
             :wallet-settings-hook]
   :config  {:initialRouteName :wallet}})
