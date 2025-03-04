(ns status-im.contexts.wallet.trading.swap.components.flip-tokens
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.trading.swap.style :as style]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [pay-input]}]
  ;;TODO
  (let [approval-required? (rf/sub [:trading.swap/approval-required?])
        receive-amount     (rf/sub [:trading.swap/receive-amount])
        on-flip-press      (rn/use-callback (fn []
                                              (when receive-amount
                                                (apply (:set-input-value pay-input) [receive-amount]))
                                              (rf/dispatch [:trading.swap/flip-tokens]))
                                            [receive-amount pay-input])]
    [quo/swap-order-button
     {:container-style (style/swap-order-button approval-required?)
      :on-press        on-flip-press}]))
