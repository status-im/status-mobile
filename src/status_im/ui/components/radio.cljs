(ns status-im.ui.components.radio
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]))

(defn radio [selected?]
  [react/view {:style {:width 20 :height 20 :border-radius 10 :align-items :center :justify-content :center
                       :background-color (if selected? colors/blue colors/gray-lighter)}}
   (when selected?
     [react/view {:style {:width 12 :height 12 :border-radius 6
                          :background-color colors/white-persist}}])])