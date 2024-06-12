(ns status-im.contexts.wallet.wallet-connect.modals.sign-message.view
  (:require [status-im.contexts.wallet.wallet-connect.modals.base-modal.view :as base-modal]
            [utils.i18n :as i18n]))

(defn view
  []
  [base-modal/view
   {:header-label      (i18n/label :t/wallet-connect-sign-message-header)
    :warning-label     (i18n/label :t/wallet-connect-sign-message-warning)
    :show-network?     false
    :slide-button-text (i18n/label :t/slide-to-sign)}])
