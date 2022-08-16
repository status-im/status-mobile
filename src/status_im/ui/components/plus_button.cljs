(ns status-im.ui.components.plus-button
  (:require [quo.design-system.colors :as colors]
            [quo.core :as quo]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.icons :as icons]
            [quo2.components.button :as quo2.button]))

(def action-button-container
  {:align-items     :center
   :justify-content :center
   :height          32
   :border-radius   16
   :overflow        :hidden})

(defn action-button []
  {:width            32
   :height           32
   :border-radius    18
   :align-items      :center
   :justify-content  :center})

(def action-button-container-old
  {:position        :absolute
   :z-index         2
   :align-items     :center
   :justify-content :center
   :left            0
   :right           0
   :bottom          16
   :height          40})

(defn action-button-old []
  {:width            40
   :height           40
   :background-color colors/blue
   :border-radius    20
   :align-items      :center
   :justify-content  :center
   :shadow-offset    {:width 0 :height 1}
   :shadow-radius    6
   :shadow-opacity   1
   :shadow-color     (if (colors/dark?)
                       "rgba(0, 0, 0, 0.75)"
                       "rgba(0, 12, 63, 0.2)")
   :elevation        2})

(defn plus-button [{:keys [on-press loading accessibility-label]}]
  [react/view action-button-container
   [quo2.button/button {:type                :primary
                        :size                32
                        :width               32
                        :accessibility-label (or accessibility-label :plus-button)
                        :on-press            on-press}
    [react/view (action-button)
     (if loading
       [react/activity-indicator {:color     colors/white-persist
                                  :animating true}]
       [icons/icon :main-icons/add {:color colors/white-persist}])]]])

(defn plus-button-old [{:keys [on-press loading accessibility-label]}]
  [react/view action-button-container-old
   [quo/button {:type                :scale
                :accessibility-label (or accessibility-label :plus-button)
                :on-press            on-press}
    [react/view (action-button-old)
     (if loading
       [react/activity-indicator {:color     colors/white-persist
                                  :animating true}]
       [icons/icon :main-icons/add {:color colors/white-persist}])]]])