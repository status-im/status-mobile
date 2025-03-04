(ns status-im.contexts.wallet.trading.swap.components.footer
  (:require [quo.core :as quo]
            [status-im.contexts.wallet.trading.swap.components.alert-banner :as alert-banner]
            [status-im.contexts.wallet.trading.swap.components.info-bar.view :as info-bar]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- on-press-review
  []
  (rf/dispatch [:open-modal :screen/trading.swap-confirmation])
  (rf/dispatch [:trading.swap/swap-confirmation]))

(defn- submit-button
  [{:keys [disabled?]}]
  (let [customization-color (rf/sub [:trading.swap/customization-color])]
    [quo/bottom-actions
     {:container-style  {:background-color :transparent}
      :actions          :one-action
      :button-one-label (i18n/label :t/review-swap)
      :button-one-props {:disabled?           disabled?
                         :customization-color customization-color
                         :on-press            on-press-review}}]))

(defn transaction-info-bar
  []
  (let [max-slippage      (rf/sub [:trading.swap/max-slippage])
        max-fee           (rf/sub [:trading.swap/total-fee-fiat])
        error-response    (rf/sub [:wallet/swap-error-response])
        processing-route? (rf/sub [:trading.swap/processing-route?])]
    [info-bar/container
     {:style {:padding-top        12
              :padding-horizontal 20}}
     [info-bar/fiat-max-fee
      {:fee-amount      max-fee
       :subtitle-error? (boolean error-response)}]
     [info-bar/max-slippage
      {:slippage  max-slippage
       :disabled? processing-route?}]]))

(defn view
  []
  (let [processing-route?           (rf/sub [:trading.swap/processing-route?])
        route                       (rf/sub [:trading.swap/route])
        route-error                 (rf/sub [:trading.swap/route-error])
        input-valid?                (rf/sub [:trading.swap/pay-amount-valid?])
        approval-required?          (rf/sub [:trading.swap/approval-required?])
        approval-transaction-status (rf/sub [:trading.swap/approval-status])]
    [:<>
     (when (and input-valid? (not processing-route?))
       [alert-banner/view])
     (when input-valid?
       [transaction-info-bar])
     [submit-button
      {:disabled? (or (not (seq route))
                      (not input-valid?)
                      route-error
                      (and approval-required?
                           (not= approval-transaction-status :confirmed))
                      processing-route?)}]]))
