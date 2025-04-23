(ns status-im.contexts.settings.wallet.network-settings.max-active-networks-sheet
  (:require [quo.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn view
  []
  (let [customization-color (rf/sub [:profile/customization-color])
        max-active-networks (rf/sub [:wallet/max-available-active-networks])]
    [:<>
     [quo/drawer-top
      {:title           (i18n/label :t/max-active-networks-title
                                    {:count max-active-networks})
       :blur?           true
       :container-style {:padding-bottom 16
                         :padding-top    12}}]
     [quo/text
      {:style {:padding-horizontal 20
               :padding-bottom     8}}
      (i18n/label :t/max-active-networks-message)]
     [quo/bottom-actions
      {:actions          :one-action
       :blur?            true
       :button-one-label (i18n/label :t/close)
       :button-one-props {:accessibility-label :confirm-testnet-mode-change
                          :on-press            hide-bottom-sheet
                          :type                :grey
                          :customization-color customization-color}}]]))
