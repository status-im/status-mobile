(ns status-im.contexts.wallet.trading.swap.components.receive-input
  (:require [quo.core :as quo]
            [status-im.contexts.wallet.trading.swap.components.token-list :as token-list]
            [status-im.contexts.wallet.trading.swap.style :as style]
            [status-im.contexts.wallet.trading.swap.utils :as utils]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [pay-input]}]
  (let [customization-color  (rf/sub [:trading.swap/customization-color])
        receive-token-symbol (rf/sub [:trading.swap/receive-token-symbol])
        processing-route?    (rf/sub [:trading.swap/processing-route?])
        route                (rf/sub [:trading.swap/route])
        receive-amount       (rf/sub [:trading.swap/receive-amount])
        pay-amount           (:value pay-input)
        receive-fiat-amount  (rf/sub [:trading.swap/receive-fiat-amount])
        error?               (rf/sub [:trading.swap/error?])
        loading-input?       (or processing-route?
                                 error?
                                 (and (not route)
                                      (not (utils/input-empty? pay-amount))))
        approval-required?   (rf/sub [:trading.swap/approval-required?])]
    [quo/swap-input
     {:type                 :receive
      :error?               false
      :token                receive-token-symbol
      :customization-color  customization-color
      :show-approval-label? false
      :enable-swap?         true
      :input-disabled?      true
      :show-keyboard?       false
      :status               (cond
                              loading-input? :loading
                              :else          :disabled)
      :on-token-press       (token-list/show receive-token-symbol)
      :value                (if (and (utils/input-empty? receive-amount)
                                     (utils/input-empty? pay-amount))
                              "0"
                              receive-amount)
      :fiat-value           receive-fiat-amount
      :container-style      (style/receive-token-swap-input-container approval-required?)}]))
