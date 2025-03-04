(ns status-im.contexts.wallet.trading.swap.components.transaction.provider-footer
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [status-im.contexts.wallet.trading.swap.components.transaction.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [theme    (quo.theme/use-theme)
        provider (rf/sub [:trading.swap/provider])
        on-press (rn/use-callback #(when provider
                                     (rf/dispatch [:open-url (:terms-and-conditions-url provider)]))
                                  [provider])]
    [rn/view {:style style/providers-container}
     [quo/text
      {:size  :paragraph-2
       :style (style/swaps-powered-by theme)}
      (i18n/label :t/swaps-powered-by
                  {:provider (if provider (:full-name provider) (i18n/label :t/unknown))})]
     [quo/text
      {:size     :paragraph-2
       :style    (style/terms-and-conditions theme)
       :on-press on-press}
      (i18n/label :t/terms-and-conditions)]]))
