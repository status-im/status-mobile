(ns status-im.contexts.wallet.send.send-amount.view
  (:require
    [quo.theme]
    [status-im.contexts.wallet.send.input-amount.view :as input-amount]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- view-internal
  []
  [input-amount/view
   {:current-screen-id :screen/wallet.send-input-amount
    :button-one-label  (i18n/label :t/confirm)
    :on-navigate-back  (fn []
                         (rf/dispatch [:wallet/clean-selected-token])
                         (rf/dispatch [:wallet/clean-selected-collectible])
                         (rf/dispatch [:navigate-back]))}])

(def view (quo.theme/with-theme view-internal))
