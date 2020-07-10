(ns status-im.ui.screens.multiaccounts.recover.views
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.keycard.recovery :as keycard]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.ui.components.colors :as colors]
            [quo.core :as quo]
            [status-im.utils.platform :as platform]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defview custom-seed-phrase []
  [react/view
   [react/view {:margin-top 24 :margin-horizontal 24 :align-items :center}
    [react/view {:width       32      :height          32 :border-radius 16
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
      (i18n/label :t/custom-seed-phrase-text-1)]]
    [react/view {:margin-vertical 24
                 :align-items     :center}
     [quo/button {:on-press            #(re-frame/dispatch [:hide-popover])
                  :accessibility-label :cancel-custom-seed-phrase
                  :type                :secondary}
      (i18n/label :t/cancel)]]]])

(defn bottom-sheet-view []
  [react/view {:flex 1 :flex-direction :row}
   [react/view {:flex 1}
    [quo/list-item
     {:theme               :accent
      :title               (i18n/label :t/enter-seed-phrase)
      :accessibility-label :enter-seed-phrase-button
      :icon                :main-icons/text
      :on-press            #(hide-sheet-and-dispatch [::multiaccounts.recover/enter-phrase-pressed])}]
    (when (or platform/android?
              config/keycard-test-menu-enabled?)
      [quo/list-item
       {:theme               :accent
        :title               (i18n/label :t/recover-with-keycard)
        :accessibility-label :recover-with-keycard-button
        :icon                [react/view {:border-width     1
                                          :border-radius    20
                                          :border-color     colors/blue-light
                                          :background-color colors/blue-light
                                          :justify-content  :center
                                          :align-items      :center
                                          :width            40
                                          :height           40}
                              [react/image {:source (resources/get-image :keycard-logo-blue)
                                            :style  {:width 24 :height 24}}]]
        :on-press            #(hide-sheet-and-dispatch [::keycard/recover-with-keycard-pressed])}])]])

(def bottom-sheet
  {:content bottom-sheet-view})
