(ns status-im.ui.screens.routing.modals)

(def modal-screens
  [;; this route is broken after
   ;; https://github.com/status-im/status-react/commit/7de2941f26bbfaa4f5948f32dd2ffa4409e1bdcc#diff-290267c339459950d383066cd474a69aL5
   #_{:name    :wallet-send-modal-stack
      :screens [:hardwallet-connect-modal
                :enter-pin-modal]
      :config  {:initialRouteName :wallet-send-transaction-modal}}
   {:name    :wallet-send-modal-stack-with-onboarding
    :screens [:wallet-onboarding-setup-modal
              :hardwallet-connect-modal
              :enter-pin-modal]
    :config  {:initialRouteName :wallet-onboarding-setup-modal}}
   :chat-modal
   :show-extension-modal
   :stickers-pack-modal
   :tribute-learn-more
   :enter-pin-modal
   :hardwallet-connect-modal
   :selection-modal-screen
   :wallet-transactions-filter
   :profile-qr-viewer
   :welcome])
