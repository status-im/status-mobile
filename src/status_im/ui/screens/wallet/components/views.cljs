(ns status-im.ui.screens.wallet.components.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.components.react :as react]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [status-im.i18n :as i18n]
            [reagent.core :as reagent]))

(defn amount-input [& [{:keys [input-options style]}]]
  (let [active? (reagent/atom false)
        {:keys [on-focus on-blur]} input-options]
    (fn []
      [react/view {:flex 1}
       [react/text {:style styles/label} (i18n/label :t/amount)]
       [react/view (merge (styles/amount-container @active?) style)
        [react/text-input
         (merge
           {:keyboard-type          :numeric
            :placeholder            "0.000"
            :placeholder-text-color "#ffffff66"
            :selection-color        :white
            :style                  styles/text-input
            :on-focus               #(do (reset! active? true)
                                         (when on-focus (on-focus)))
            :on-blur                #(do (reset! active? false)
                                         (when on-blur (on-blur)))}
           (dissoc input-options :on-focus :on-blur))]]])))

;;TODO (andrey) this should be choose component with the list of currencies
(defn choose-currency [& [style]]
  [react/view
   [react/text {:style styles/label} (i18n/label :t/currency)]
   [react/view (merge styles/currency-container
                      style)
    [react/text {:style styles/wallet-name} "ETH"]]])

;;TODO (andrey) this should be choose component with the list of wallets
(views/defview choose-wallet [& [style]]
  (views/letsubs [eth-balance [:eth-balance]]
    [react/view
     [react/text {:style styles/label} (i18n/label :t/wallet)]
     [react/view (merge styles/wallet-container
                        style)
      [react/text {:style styles/wallet-name} "Main wallet"]
      [react/text {:style styles/wallet-value} (str eth-balance " ETH")]]]))

(defn network-label
  ([n] (network-label [{} n]))
  ([style n] [react/view (merge styles/network-container
                                style)
               [react/text {:style styles/network} n]]))