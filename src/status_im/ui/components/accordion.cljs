(ns status-im.ui.components.accordion
  (:require [reagent.core :as reagent]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.icons :as icons]))

(defn section
  "Render collapsible section"
  [_props]
  (let [opened? (reagent/atom false)]
    (fn [{:keys [title cnt content icon]}]
      [react/view {:padding-vertical 8}
       [quo/list-item
        {:title    title
         :icon     icon
         :on-press #(swap! opened? not)
         :accessory
         [react/view {:flex-direction :row :align-items :center}
          (when (pos? cnt)
            [react/text {:style {:color colors/gray}} cnt])
          [icons/icon (if @opened? :main-icons/dropdown-up :main-icons/dropdown)
           {:container-style {:align-items     :center
                              :margin-left     8
                              :justify-content :center}
            :resize-mode     :center
            :color           colors/black}]]}]
       (when @opened?
         content)])))

(defn accordion
  "List of collapseable sections"
  [])
  ;; TODO(shivekkhurana): Extract status-im.ui.screens.wallet.recipient.views/accordion component here

