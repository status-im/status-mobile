(ns status-im.contexts.wallet.trading.swap.components.exchange-rate.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [status-im.contexts.wallet.trading.swap.components.exchange-rate.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [theme                 (quo.theme/use-theme)
        input-valid?          (rf/sub [:trading.swap/pay-amount-valid?])
        pay-token-symbol      (rf/sub [:trading.swap/pay-token-symbol])
        receive-token-symbol  (rf/sub [:trading.swap/receive-token-symbol])
        {:keys [crypto fiat]} (rf/sub [:trading.swap/exchange-rate])]
    (when input-valid?
      (if crypto
        [rn/view {:style style/exchange-rate-container}
         [quo/text
          {:weight :medium
           :size   :paragraph-2
           :style  style/exchange-rate-crypto-label}
          (i18n/label :t/swap-exchange-rate-in-crypto
                      {:receive-token-symbol receive-token-symbol
                       :pay-token-symbol     pay-token-symbol
                       :exchange-rate        crypto})]
         [quo/text
          {:weight :medium
           :size   :paragraph-2
           :style  (style/exchange-rate-fiat-label theme)}
          (str " (" fiat ")")]]
        [rn/view {:style (style/exchange-rate-loader theme)}]))))
