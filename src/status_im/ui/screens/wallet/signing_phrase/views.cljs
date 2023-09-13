(ns status-im.ui.screens.wallet.signing-phrase.views
  (:require-macros [status-im.utils.views :as views])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]))

(views/defview signing-phrase
  []
  (views/letsubs [phrase                          [:signing/phrase]
                  {:keys [wallet-set-up-passed?]} [:profile/profile]]
    [react/view
     [react/view {:margin-top 24 :margin-horizontal 24 :align-items :center}
      [react/view
       {:background-color colors/blue-light
        :width            32
        :height           32
        :border-radius    16
        :align-items      :center
        :justify-content  :center}
       [icons/icon :main-icons/security {:color colors/blue}]]
      [react/text {:style {:typography :title-bold :margin-top 16 :margin-bottom 8}}
       (i18n/label :t/this-is-you-signing)]
      [react/text {:style {:color colors/gray :text-align :center}}
       (i18n/label :t/three-words-description)]]
     [react/view
      {:margin-vertical  16
       :height           52
       :background-color colors/gray-lighter
       :align-items      :center
       :justify-content  :center}
      [react/text phrase]]
     [react/view
      {:padding-bottom     8
       :padding-horizontal 24
       :align-items        :center}
      [react/text {:style {:color colors/gray :text-align :center}}
       (i18n/label :t/three-words-description-2)]
      (when-not wallet-set-up-passed?
        [react/view {:style {:margin-top 16}}
         [quo/button {:on-press #(re-frame/dispatch [:hide-popover])}
          (i18n/label :t/remind-me-later)]])
      [react/view {:style {:padding-vertical 8}}
       [quo/button
        {:on-press #(do
                      (when-not wallet-set-up-passed?
                        (re-frame/dispatch [:multiaccounts.ui/wallet-set-up-confirmed]))
                      (re-frame/dispatch [:hide-popover]))
         :type     :secondary}
        (i18n/label :t/ok-got-it)]]]]))
