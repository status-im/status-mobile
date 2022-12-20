(ns status-im.ui.components.badge
  (:require [quo.design-system.colors :as colors]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]))

(defn badge
  [label & [small?]]
  [react/view
   (merge
    (if small?
      {:height 18 :border-radius 9 :min-width 18 :padding-horizontal 6}
      {:height 22 :border-radius 11 :min-width 22 :padding-horizontal 8})
    {:background-color colors/blue
     :justify-content  :center
     :align-items      :center})
   [react/text
    {:style {:typography  :caption
             :font-weight "500"
             :color       colors/white-persist}}
    label]])

(defn message-counter
  [value & [small?]]
  [badge
   (if (> value 99)
     (i18n/label :t/counter-99-plus)
     value)
   small?])
