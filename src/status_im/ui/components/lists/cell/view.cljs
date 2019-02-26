(ns status-im.ui.components.lists.cell.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.lists.cell.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defn cell [{:keys [title details icon on-press]
             {:keys [title-color
                     icon-color
                     icon-background]} :style}]
  [react/view
   {:style styles/cell-container}
   [react/touchable-highlight
    {:style    styles/icon-container
     :on-press on-press}
    [react/view {}
     [icons/icon icon (styles/icon icon-color icon-background)]]]
   [react/view {:style styles/description}
    [react/touchable-highlight
     {:style    styles/cell-text
      :on-press on-press}
     [react/view {}
      [react/text {:style (styles/item-title title-color)}
       title]]]
    [react/view {:style styles/cell-text}
     [react/text {:style styles/item-details}
      details]]]])
