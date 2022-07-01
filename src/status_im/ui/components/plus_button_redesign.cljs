(ns status-im.ui.components.plus-button-redesign
  (:require [quo2.foundations.colors :as colors]
            [quo2.components.button :as button]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.icons :as icons]))

(def action-button-container
  {:width           32
   :height          32})

(defn action-button []
  {:width            32
   :height           32
   :background-color (colors/theme-colors
                      colors/primary-50
                      colors/primary-60)
   :border-radius    20
   :align-items      :center
   :justify-content  :center})

(defn plus-button [{:keys [on-press loading accessibility-label]}]
  [react/view action-button-container
   [button/button {:size                32
                   :type                :grey
                   :accessibility-label (or accessibility-label :plus-button)
                   :on-press            on-press}
    [react/view (action-button)
     (if loading
       [react/activity-indicator {:color     colors/white
                                  :animating true}]
       [icons/icon :main-icons/add {:color (colors/theme-colors
                                            colors/white
                                            colors/black)}])]]])