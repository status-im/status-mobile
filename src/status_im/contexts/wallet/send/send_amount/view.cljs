(ns status-im.contexts.wallet.send.send-amount.view
  (:require
    [quo.theme]
    [status-im.contexts.wallet.send.input-amount.view :as input-amount]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  [input-amount/view
   {:current-screen-id :screen/wallet.send-input-amount
    :button-one-label  (i18n/label :t/review-send)
    :on-confirm        (fn [amount]
                         (rf/dispatch [:wallet/set-token-amount-to-send
                                       {:amount   amount
                                        :stack-id :screen/wallet.send-input-amount}]))
    :on-navigate-back  (fn []
                         (rf/dispatch-sync [:wallet/stop-and-clean-suggested-routes])
                         (rf/dispatch [:wallet/clean-disabled-from-networks])
                         (rf/dispatch [:wallet/clean-from-locked-amounts])
                         (rf/dispatch [:wallet/clean-send-amount]))}])
