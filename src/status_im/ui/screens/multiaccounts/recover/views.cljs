(ns status-im.ui.screens.multiaccounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.hardwallet.core :as hardwallet]
            [status-im.hardwallet.nfc :as nfc]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.utils.platform :as platform]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.button :as button]))

(defview custom-seed-phrase []
  [react/view
   [react/view {:margin-top 24 :margin-horizontal 24 :align-items :center}
    [react/view {:width       32 :height 32 :border-radius 16
                 :align-items :center :justify-content :center}
     [icons/icon :main-icons/help {:color colors/blue}]]
    [react/text {:style {:typography    :title-bold
                         :margin-top    8
                         :margin-bottom 8}}
     (i18n/label :t/custom-seed-phrase)]
    [react/view {:flex-wrap       :wrap
                 :flex-direction  :row
                 :justify-content :center
                 :text-align      :center}
     [react/nested-text
      {:style {:color       colors/gray
               :text-align  :center
               :line-height 22}}
      (i18n/label :t/custom-seed-phrase-text-1)
      [{:style {:color colors/black}}
       (i18n/label :t/custom-seed-phrase-text-2)]
      (i18n/label :t/custom-seed-phrase-text-3)
      [{:style {:color colors/black}}
       (i18n/label :t/custom-seed-phrase-text-4)]]]
    [react/view {:margin-vertical 24
                 :align-items     :center}
     [button/button {:on-press            #(re-frame/dispatch [::multiaccounts.recover/continue-pressed])
                     :accessibility-label :continue-custom-seed-phrase
                     :label               (i18n/label :t/continue)}]
     [button/button {:on-press            #(re-frame/dispatch [:hide-popover])
                     :accessibility-label :cancel-custom-seed-phrase
                     :type                :secondary
                     :label               (i18n/label :t/cancel)}]]]])

(defn bottom-sheet-view []
  [react/view {:flex 1 :flex-direction :row}
   [react/view {:flex 1}
    [list-item/list-item
     {:theme               :action
      :title               :t/enter-seed-phrase
      :accessibility-label :enter-seed-phrase-button
      :icon                :main-icons/text
      :on-press            #(re-frame/dispatch [::multiaccounts.recover/enter-phrase-pressed])}]
    (when (and config/hardwallet-enabled?
               platform/android?
               (nfc/nfc-supported?))
      [list-item/list-item
       {:theme               :action
        :title               :t/recover-with-keycard
        :disabled?           (not config/hardwallet-enabled?)
        :accessibility-label :recover-with-keycard-button
        :icon [react/view {:border-width     1
                           :border-radius    20
                           :border-color     colors/blue-light
                           :background-color colors/blue-light
                           :justify-content  :center
                           :align-items      :center
                           :width            40
                           :height           40}
               [react/image {:source (resources/get-image :keycard-logo-blue)
                             :style  {:width 24 :height 24}}]]
        :on-press            #(re-frame/dispatch [::hardwallet/recover-with-keycard-pressed])}])]])

(defn bottom-sheet []
  {:content        bottom-sheet-view
   :content-height (if (and platform/android?
                            (nfc/nfc-supported?))
                     130
                     65)})
