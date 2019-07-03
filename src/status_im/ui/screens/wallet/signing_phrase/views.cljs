(ns status-im.ui.screens.wallet.signing-phrase.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.common.common :as components.common]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]))

(views/defview signing-phrase []
  (views/letsubs [phrase [:signing/phrase]
                  {:keys [wallet-set-up-passed?]} [:multiaccount]]
    [react/view
     [react/view {:margin-top 24 :margin-horizontal 24 :align-items :center}
      [react/view {:background-color colors/blue-light :width 32 :height 32 :border-radius 16
                   :align-items :center :justify-content :center}
       [icons/icon :main-icons/security {:color colors/blue}]]
      [react/text {:style {:typography :title-bold :margin-top 16 :margin-bottom 8}}
       (i18n/label :t/this-is-you-signing)]
      [react/text {:style {:color colors/gray :text-align :center}} (i18n/label :t/three-words-description)]]
     [react/view {:margin-vertical 16 :height 52 :background-color colors/gray-lighter :align-items :center :justify-content :center}
      [react/text phrase]]
     [react/view {:margin-bottom 24 :margin-horizontal 24 :align-items :center}
      [react/text {:style {:color colors/gray :text-align :center}} (i18n/label :t/three-words-description-2)]
      (when-not wallet-set-up-passed?
        [components.common/button {:on-press     #(re-frame/dispatch [:hide-popover])
                                   :button-style {:margin-top 24}
                                   :label        (i18n/label :t/remind-me-later)}])
      [components.common/button {:on-press     #(do
                                                  (when-not wallet-set-up-passed?
                                                    (re-frame/dispatch [:multiaccounts.ui/wallet-set-up-confirmed]))
                                                  (re-frame/dispatch [:hide-popover]))
                                 :button-style {:margin-top 24}
                                 :background?  false
                                 :label        (i18n/label :t/ok-got-it)}]]]))
