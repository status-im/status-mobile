(ns status-im.ui.components.action-button.action-button
  (:require [status-im.ui.components.action-button.styles :as st]
            [status-im.ui.components.common.common :refer [list-separator]]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.ui.components.react :as rn]))

(defn action-button [{:keys [label accessibility-label icon icon-opts on-press label-style cyrcle-color]}]
  [rn/touchable-highlight (merge {:on-press on-press}
                                 (when accessibility-label
                                   {:accessibility-label accessibility-label}))
   [rn/view st/action-button
    [rn/view (st/action-button-icon-container cyrcle-color)
     [vi/icon icon icon-opts]]
    [rn/view st/action-button-label-container
     [rn/text {:style (merge st/action-button-label label-style)}
      label]]]])

(defn action-button-disabled [{:keys [label icon]}]
  [rn/view st/action-button
   [rn/view st/action-button-icon-container-disabled
    [rn/view {:opacity 0.4}
     [vi/icon icon]]]
   [rn/view st/action-button-label-container
    [rn/text {:style st/action-button-label-disabled}
     label]]])

(defn action-separator []
  [list-separator])
