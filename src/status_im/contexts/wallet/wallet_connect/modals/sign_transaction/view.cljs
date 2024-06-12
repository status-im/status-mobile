(ns status-im.contexts.wallet.wallet-connect.modals.sign-transaction.view
  (:require [status-im.contexts.wallet.wallet-connect.modals.base-modal.view :as base-modal]
            [utils.i18n :as i18n]))

(defn view
  []
  [base-modal/view
   {:header-label      (i18n/label :t/wallet-connect-sign-transaction-header)
    :warning-label     (i18n/label :t/wallet-connect-sign-transaction-warning)
    :show-network?     true
    :slide-button-text (i18n/label :t/slide-to-sign)}])
