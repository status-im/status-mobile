(ns status-im.ui.screens.wallet.components.views
  (:require [status-im.components.react :as react]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [status-im.i18n :as i18n]))

(defn amount-input [& [{:keys [input-options style]}]]
  [react/view {:flex 1}
   [react/text {:style styles/label} (i18n/label :t/amount)]
   [react/view (merge styles/amount-container style)
    [react/text-input
     (merge
       {:keyboard-type          :numeric
        :max-length             15
        :placeholder            "0.000"
        :placeholder-text-color "#ffffff66"
        :selection-color        :white
        :style                  styles/text-input}
       input-options)]]])

;;TODO (andrey) this should be choose component with the list of currencies
(defn choose-currency [& [style]]
  [react/view
   [react/text {:style styles/label} (i18n/label :t/currency)]
   [react/view (merge styles/currency-container
                      style)
    [react/text {:style styles/value} "ETH"]]])

;;TODO (andrey) this should be choose component with the list of wallets
(defn choose-wallet [& [style]]
  [react/view
   [react/text {:style styles/label} (i18n/label :t/wallet)]
   [react/view (merge styles/wallet-container
                      style)
    [react/text {:style styles/value} "Main wallet"]]])

(defn network-label
  ([n] (network-label [{} n]))
  ([style n] [react/view (merge styles/network-container
                                style)
               [react/text {:style styles/network} n]]))