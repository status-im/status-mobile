(ns status-im.ui.components.lists.cell.view
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.lists.cell.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defn cell [{:keys [title details icon]
             {:keys [title-color
                     icon-color
                     icon-background]} :style}]
  [react/view
   {:style styles/cell-container}
   [react/view {:style styles/icon-container}
    [icons/icon icon (styles/icon icon-color icon-background)]]
   [react/view {:style styles/description}
    [react/view {:style styles/cell-text}
     [react/text {:style (styles/item-title title-color)}
      title]]
    [react/view {:style styles/cell-text}
     [react/text {:style styles/item-details}
      details]]]])
