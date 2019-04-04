(ns status-im.ui.screens.routing.wallet-stack)

(def wallet-stack
  {:name    :wallet-stack
   :screens [:wallet
             :collectibles-list
             :wallet-onboarding-setup
             :wallet-send-transaction-chat
             :contact-code
             {:name    :send-transaction-new-stack
              :config  {:initialRouteName :wallet-send-transaction-new}
              :screens [:wallet-send-transaction-new
                        :wallet-choose-amount
                        :wallet-choose-asset
                        :wallet-txn-overview
                        :recent-recipients
                        :wallet-transaction-sent-modal
                        :recipient-qr-code
                        :wallet-send-assets]}
             {:name    :send-transaction-stack
              :config  {:initialRouteName :wallet-send-transaction}
              :screens [:wallet-send-transaction
                        :wallet-choose-amount
                        :wallet-choose-asset
                        :wallet-txn-overview
                        :recent-recipients
                        :wallet-transaction-sent
                        :enter-pin
                        :hardwallet-connect
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
