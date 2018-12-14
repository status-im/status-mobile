(ns status-im.ui.screens.routing.wallet-stack)

(def wallet-stack
  {:name    :wallet-stack
   :screens [{:name    :main-wallet-stack
              :screens [:wallet
                        :collectibles-list
                        :wallet-onboarding-setup
                        :wallet-send-transaction-chat
                        :contact-code
                        {:name    :send-transaction-stack
                         :screens [:wallet-send-transaction
                                   :recent-recipients
                                   :wallet-choose-amount
                                   :wallet-txn-overview
                                   :wallet-choose-asset
                                   :wallet-transaction-sent
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
              :config  {:initialRouteName :wallet}}
             :selection-modal-screen
             {:name    :wallet-send-modal-stack
              :screens [:wallet-send-transaction-modal
                        :wallet-transaction-sent-modal
                        :wallet-transaction-fee]
              :config  {:initialRouteName :wallet-send-transaction-modal}}
             {:name    :wallet-send-modal-stack-with-onboarding
              :screens [:wallet-onboarding-setup-modal
                        :wallet-send-transaction-modal
                        :wallet-transaction-sent
                        :wallet-transaction-fee]
              :config  {:initialRouteName :wallet-send-modal-stack-with-onboarding}}
             :wallet-settings-assets
             :wallet-transaction-fee
             :wallet-transactions-filter]
   :config  {:mode             :modal
             :initialRouteName :main-wallet-stack}})
