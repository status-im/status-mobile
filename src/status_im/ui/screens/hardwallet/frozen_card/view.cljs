(ns status-im.ui.screens.hardwallet.frozen-card.view
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.common.common :as components.common]
            [status-im.hardwallet.login :as login]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]))

(views/defview frozen-card
  [{:keys [show-dismiss-button?]
    :or   {show-dismiss-button? true}}]
  [react/view {:style (when-not show-dismiss-button?
                        {:flex 1})}
   [react/view {:margin-top        24
                :margin-horizontal 24
                :align-items       :center}
    [react/view {:background-color colors/blue-light
                 :width            32 :height 32
                 :border-radius    16
                 :align-items      :center
                 :justify-content  :center}
     [icons/icon :main-icons/warning {:color colors/blue}]]
    [react/text {:style {:typography    :title-bold
                         :margin-top    16
                         :margin-bottom 8}}
     (i18n/label :t/keycard-is-frozen-title)]
    [react/text {:style {:color      colors/gray
                         :text-align :center}}
     (i18n/label :t/keycard-is-frozen-details)]]
   [react/view {:margin-bottom     24
                :margin-horizontal 24
                :align-items       :center}
    [components.common/button
     {:on-press     #(re-frame/dispatch [::login/reset-pin])
      :button-style {:margin-top 24}
      :label        (i18n/label :t/keycard-is-frozen-reset)}]
    (when show-dismiss-button?
      [components.common/button
       {:on-press     #(re-frame/dispatch [::login/frozen-keycard-popover-dismissed])
        :button-style {:margin-top 24}
        :background?  false
        :label        (i18n/label :t/dismiss)}])]])
