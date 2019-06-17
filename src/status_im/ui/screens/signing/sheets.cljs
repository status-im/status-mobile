(ns status-im.ui.screens.signing.sheets
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.i18n :as i18n]
            [status-im.ui.components.button.view :as button]
            [status-im.ui.components.colors :as colors]))

(views/defview fee-bottom-sheet [fee-display-symbol]
  (views/letsubs [{gas-edit :gas gas-price-edit :gasPrice max-fee :max-fee} [:signing/edit-fee]]
    [react/view
     [react/view {:style {:margin-horizontal 16 :margin-top 8}}
      [react/text {:style {:typography :title-bold}} (i18n/label :t/network-fee)]
      [react/view {:style {:flex-direction :row :margin-top 8}}
       [react/view {:flex 1}
        [text-input/text-input-with-label
         {:on-change-text  #(re-frame/dispatch [:signing.edit-fee.ui/edit-value :gas %])
          :label           (i18n/label :t/gas-limit)
          :error           (:error gas-edit)
          :default-value   (:value gas-edit)
          :keyboard-type   :numeric
          :auto-capitalize :none
          :placeholder     "0.000"
          :auto-focus      false}]]
       [react/view {:flex 1 :margin-left 33}
        [text-input/text-input-with-label
         {:label           (i18n/label :t/gas-price)
          :on-change-text  #(re-frame/dispatch [:signing.edit-fee.ui/edit-value :gasPrice %])
          :error           (:error gas-price-edit)
          :default-value   (:value gas-price-edit)
          :keyboard-type   :numeric
          :auto-capitalize :none
          :placeholder     "0.000"
          :auto-focus      false}]]
       [react/view {:margin-top 58 :margin-left 10}
        [react/text (i18n/label :t/gwei)]]]
      [react/view {:margin-vertical 16 :align-items :center}
       [react/text {:style {:color colors/gray}} (i18n/label :t/wallet-transaction-total-fee)]
       [react/view {:height 8}]
       [react/nested-text {:style {:font-size 17}}
        max-fee " "
        [{:style {:color colors/gray}} fee-display-symbol]]]]
     [react/view {:height 1 :background-color colors/gray-lighter}]
     [react/view {:margin-horizontal 16 :align-items :center :justify-content :space-between :flex-direction :row :margin-top 6}
      [button/secondary-button {:on-press  #(re-frame/dispatch [:bottom-sheet/hide-sheet])
                                :style {:background-color nil}}
       (i18n/label :t/cancel)]
      [button/secondary-button {:on-press  #(re-frame/dispatch [:signing.edit-fee.ui/submit])
                                :disabled? (or (:error gas-edit) (:error gas-price-edit))}
       (i18n/label :t/update)]]]))