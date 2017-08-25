(ns status-im.components.action-button.action-button
  (:require [status-im.components.action-button.styles :as st]
            [status-im.components.common.common :refer [list-separator]]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.react :as rn]))

(defn action-button [{:keys [label icon on-press label-style cyrcle-color]}]
  [rn/touchable-highlight {:on-press on-press}
   [rn/view st/action-button
    [rn/view (st/action-button-icon-container cyrcle-color)
     ((comp vec flatten vector) vi/icon icon)]
    [rn/view st/action-button-label-container
     [rn/text {:style (merge st/action-button-label label-style)}
      label]]]])

(defn action-button-disabled [{:keys [label icon]}]
  [rn/view st/action-button
   [rn/view st/action-button-icon-container-disabled
    [rn/view {:opacity 0.4}
     ((comp vec flatten vector) vi/icon icon)]]
   [rn/view st/action-button-label-container
    [rn/text {:style st/action-button-label-disabled}
     label]]])

(defn action-separator []
  [list-separator])
