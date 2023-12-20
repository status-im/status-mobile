(ns legacy.status-im.ui.screens.keycard.frozen-card.view
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.keycard.login :as login]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(views/defview frozen-card
  [{:keys [show-dismiss-button?]
    :or   {show-dismiss-button? true}}]
  [react/view
   {:style (when-not show-dismiss-button?
             {:flex 1})}
   [react/view
    {:margin-top        24
     :margin-horizontal 24
     :align-items       :center}
    [react/view
     {:background-color colors/blue-light
      :width            32
      :height           32
      :border-radius    16
      :align-items      :center
      :justify-content  :center}
     [icons/icon :main-icons/warning {:color colors/blue}]]
    [react/text
     {:style {:typography    :title-bold
              :margin-top    16
              :margin-bottom 8}}
     (i18n/label :t/keycard-is-frozen-title)]
    [react/text
     {:style {:color      colors/gray
              :text-align :center}}
     (i18n/label :t/keycard-is-frozen-details)]]
   [react/view
    {:margin-bottom     24
     :margin-horizontal 24
     :align-items       :center}
    [react/view {:style {:margin-top 24}}
     [quo/button
      {:on-press #(re-frame/dispatch [::login/reset-pin])}
      (i18n/label :t/keycard-is-frozen-reset)]]
    [react/view {:style {:margin-top 24}}
     [quo/button
      {:on-press #(re-frame/dispatch [:keycard-settings.ui/recovery-card-pressed false])}
      (i18n/label :t/keycard-is-frozen-factory-reset)]]
    (when show-dismiss-button?
      [react/view {:margin-top 24}
       [quo/button
        {:on-press #(re-frame/dispatch [::login/frozen-keycard-popover-dismissed])
         :type     :secondary}
        (i18n/label :t/dismiss)]])]])
