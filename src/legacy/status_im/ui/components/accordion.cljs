(ns legacy.status-im.ui.components.accordion
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [reagent.core :as reagent]))

(defn drop-down-icon
  [{:keys [opened? dropdown-margin-left]}]
  [react/view {:flex-direction :row :align-items :center}
   [icons/icon (if opened? :main-icons/dropdown-up :main-icons/dropdown)
    {:container-style {:align-items     :center
                       :margin-left     dropdown-margin-left
                       :justify-content :center}
     :resize-mode     :center
     :color           colors/black}]])

(defn section
  "Render collapsible section"
  [_props]
  (let [opened? (reagent/atom (get _props :default))]
    (fn
      [{:keys [title content icon opened disabled
               padding-vertical dropdown-margin-left
               open-container-style
               on-open on-close]
        :or   {padding-vertical     8
               dropdown-margin-left 8
               open-container-style {}
               on-open              #()
               on-close             #()}}]
      (let [on-press #(do
                        (apply (if @opened? on-close on-open) [])
                        (swap! opened? not))]
        [react/view
         (merge {:padding-vertical padding-vertical}
                (when @opened? open-container-style))
         (if (string? title)
           [list.item/list-item
            {:title     title
             :icon      icon
             :on-press  on-press
             :accessory [drop-down-icon (or @opened? opened)]}]
           [react/touchable-opacity {:on-press on-press :disabled disabled}
            [react/view
             {:flex-direction  :row
              :margin-right    14
              :justify-content :space-between}
             title
             [drop-down-icon
              {:opened?              (or @opened? opened)
               :dropdown-margin-left dropdown-margin-left}]]])
         (when (or @opened? opened)
           content)]))))

