(ns status-im.ui.components.badge
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]))

(defn badge [label & [small?]]
  [react/view (merge
               (if small?
                 {:height 18 :border-radius 9 :min-width 18 :padding-horizontal 6}
                 {:height 22 :border-radius 11 :min-width 22 :padding-horizontal 8})
               {:background-color colors/blue
                :justify-content :center
                :align-items :center})
   [react/text {:style {:typography :caption :color colors/white}} label]])