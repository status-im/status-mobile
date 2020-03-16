(ns status-im.ui.components.badge
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.i18n :as i18n]))

(defn badge [label & [small?]]
  [react/view (merge
               (if small?
                 {:height 18 :border-radius 9 :min-width 18 :padding-horizontal 6}
                 {:height 22 :border-radius 11 :min-width 22 :padding-horizontal 8})
               {:background-color colors/blue
                :justify-content  :center
                :align-items      :center})
   [react/text {:style {:typography  :caption
                        :font-weight "500"
                        :color       colors/white-persist}}
    label]])

(defn message-counter [value & [small?]]
  [badge
   (if (> value 99)
     (i18n/label :t/counter-99-plus)
     value)
   small?])
