(ns status-im.contexts.wallet.trading.swap.components.pay-input
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.trading.swap.components.token-list :as token-list]
            [status-im.contexts.wallet.trading.swap.hooks :as hooks]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- on-press-approve
  []
  (rf/dispatch [:open-modal :screen/trading.swap-approval-confirmation])
  (rf/dispatch [:trading.swap/approve-confirmation]))

(defn view
  [{:keys [pay-input on-pay-input-focus input-ref]}]
  (let [{:keys [value set-input-value]} pay-input
        set-input-ref                   (rn/use-callback (fn [ref] (reset! input-ref ref)))
        pay-input-focused?              (rf/sub [:trading.swap/focused?])
        customization-color             (rf/sub [:trading.swap/customization-color])
        processing-route?               (rf/sub [:trading.swap/processing-route?])
        error?                          (rf/sub [:trading.swap/error?])
        pay-token-symbol                (rf/sub [:trading.swap/pay-token-symbol])
        pay-token-balance-formatted     (rf/sub [:trading.swap/formatted-pay-token-balance])
        pay-fiat-amount                 (rf/sub [:trading.swap/pay-fiat-amount])
        network                         (rf/sub [:trading.swap/network])
        approval-required?              (rf/sub [:trading.swap/approval-required?])
        approval-transaction-id         (rf/sub [:trading.swap/approval-transaction-id])
        approval-status                 (rf/sub [:trading.swap/approval-status])
        approval-amount                 (rf/sub [:trading.swap/approval-amount])
        {:keys [input-error?]}          (hooks/use-input-validation value)
        show-approval-label?            (or approval-required?
                                            (boolean approval-transaction-id))
        on-max-press                    (rn/use-callback
                                         (fn []
                                           (set-input-value pay-token-balance-formatted))
                                         [pay-token-balance-formatted set-input-value])]
    [quo/swap-input
     {:type                 :pay
      :get-ref              set-input-ref
      :error?               input-error?
      :token                pay-token-symbol
      :customization-color  customization-color
      :auto-focus?          true
      :show-keyboard?       false
      :status               (cond pay-input-focused? :typing
                                  :else              :default)
      :on-token-press       (token-list/show pay-token-symbol)
      :on-max-press         on-max-press
      :on-input-focus       on-pay-input-focus
      :value                value
      :fiat-value           pay-fiat-amount
      :show-approval-label? show-approval-label?
      :network-tag-props    {:title    (i18n/label :t/max-token
                                                   {:number       pay-token-balance-formatted
                                                    :token-symbol pay-token-symbol})
                             :networks [{:source (:source network)}]}
      :approval-label-props (when show-approval-label?
                              {:status              (case approval-status
                                                      :pending   :approving
                                                      :confirmed :approved
                                                      :finalised :approved
                                                      :approve)
                               :token-value         approval-amount
                               :button-props        {:on-press  on-press-approve
                                                     :disabled? (or processing-route? error?)}
                               :customization-color customization-color
                               :token-symbol        pay-token-symbol})}]))
