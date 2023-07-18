(ns quo2.components.inputs.locked-input.view
  (:require [react-native.core :as rn]
            [status-im2.contexts.quo-preview.tabs.segmented-tab]
            [quo2.foundations.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
    ;[quo2.components.inputs.locked-input.style :as style]
            ))

(defn info-box
  [icon label-text]
  [rn/view
   {:style {:flex-direction     :row
            :border-radius      12
            :align-items        :center
            :background-color   (colors/theme-colors
                                  colors/neutral-10
                                  colors/neutral-80-opa-80)
            :width              :100%
            :height             40
            :padding-horizontal 12
            :padding-vertical   9
            :gap                8
            :margin-top         5}}
   [icons/icon icon {:width  20
                     :height 20
                     :color (colors/theme-colors colors/white-opa-0
                                                 colors/neutral-50)}]
   [rn/text
    {:style {:font-size   15
             :color       (colors/theme-colors colors/black colors/white)
             :margin-left 5}} label-text]])

(defn locked-input
  [{:keys [label-text label-style value-text icon style]}]
  [rn/view {:style style}
   [rn/text {:style label-style} label-text]
   [info-box icon value-text]])
