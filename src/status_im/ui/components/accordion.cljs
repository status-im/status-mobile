(ns status-im.ui.components.accordion
  (:require [reagent.core :as reagent]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.icons :as icons]))

(defn drop-down-icon [opened?]
  [react/view {:flex-direction :row :align-items :center}
   [icons/icon (if opened? :main-icons/dropdown-up :main-icons/dropdown)
    {:container-style {:align-items     :center
                       :margin-left     8
                       :justify-content :center}
     :resize-mode     :center
     :color           colors/black}]])

(defn section
  "Render collapsible section"
  [_props]
  (let [opened? (reagent/atom false)]
    (fn [{:keys [title content icon]}]
      [react/view {:padding-vertical 8}
       (if (string? title)
         [quo/list-item
          {:title     title
           :icon      icon
           :on-press  #(swap! opened? not)
           :accessory [drop-down-icon @opened?]}]
         [react/touchable-opacity {:on-press #(swap! opened? not)}
          [react/view {:flex-direction  :row
                       :justify-content :space-between}
           title
           [drop-down-icon @opened?]]])
       (when @opened?
         content)])))

