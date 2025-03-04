(ns status-im.contexts.wallet.trading.swap.components.alert-banner
  (:require [quo.core :as quo]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.sheets.buy-token.view :as buy-token]
            [status-im.contexts.wallet.trading.swap.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- error-message-from-code
  [error-code error-details]
  (cond
    (= error-code
       constants/router-error-code-not-enough-liquidity)
    (i18n/label :t/not-enough-liquidity)
    (= error-code
       constants/router-error-code-price-timeout)
    (i18n/label :t/fetching-the-price-took-longer-than-expected)
    (= error-code
       constants/router-error-code-price-impact-too-high)
    (i18n/label :t/price-impact-too-high)
    (= error-code
       constants/router-error-code-paraswap-custom-error)
    (i18n/label :t/paraswap-error
                {:paraswap-error error-details})
    (= error-code
       constants/router-error-code-generic)
    (i18n/label :t/generic-error
                {:generic-error error-details})
    (= error-code
       constants/router-error-code-not-enough-native-balance)
    (i18n/label :t/not-enough-assets-to-pay-gas-fees)
    :else
    (i18n/label :t/something-went-wrong-please-try-again-later)))

(defn- on-buy-crypto-press
  []
  (rf/dispatch [:centralized-metrics/track :metric/swap-buy-eth])
  (rf/dispatch
   [:show-bottom-sheet
    {:content (fn [] [buy-token/view])}]))

(defn view
  []
  (let [route-error            (rf/sub [:trading.swap/route-error])
        pay-input-error?       (rf/sub [:trading.swap/pay-amount-error?])
        processing-route?      (rf/sub [:trading.swap/processing-route?])
        error-response-code    (:code route-error)
        error-response-details (:details route-error)]
    (when (and (or pay-input-error? route-error)
               (not processing-route?))
      [quo/alert-banner
       (cond-> {:container-style      style/alert-banner
                :text-number-of-lines 0
                :text                 (if pay-input-error?
                                        (i18n/label :t/insufficient-funds-for-swaps)
                                        (error-message-from-code error-response-code
                                                                 error-response-details))}

         pay-input-error?
         (merge {:action?         true
                 :on-button-press (fn []
                                    (rf/dispatch [:centralized-metrics/track
                                                  :metric/swap-buy-assets])
                                    (rf/dispatch [:show-bottom-sheet
                                                  {:content buy-token/view}]))
                 :button-text     (i18n/label :t/add-assets)})

         (= error-response-code
            constants/router-error-code-not-enough-native-balance)
         (merge {:action?         true
                 :on-button-press on-buy-crypto-press
                 :button-text     (i18n/label :t/add-eth)}))])))
