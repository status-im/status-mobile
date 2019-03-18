(ns status-im.ui.screens.routing.modals)

(def modal-screens
  [{:name    :wallet-send-modal-stack
    :screens [:wallet-send-transaction-modal
              :wallet-transaction-sent-modal
              :wallet-transaction-fee]
    :config  {:initialRouteName :wallet-send-transaction-modal}}
   {:name    :wallet-send-modal-stack-with-onboarding
    :screens [:wallet-onboarding-setup-modal
              :wallet-send-transaction-modal
              :wallet-transaction-sent-modal
              :wallet-transaction-fee]
    :config  {:initialRouteName :wallet-onboarding-setup-modal}}
   :chat-modal
   :show-extension-modal
   :stickers-pack-modal
   :wallet-sign-message-modal
   :selection-modal-screen
   :wallet-settings-assets
   :wallet-transaction-fee
   :wallet-transactions-filter
   :profile-qr-viewer
   :welcome])
