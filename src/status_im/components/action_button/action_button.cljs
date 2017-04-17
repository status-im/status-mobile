(ns status-im.components.action-button.action-button
  (:require [status-im.components.action-button.styles :as st]
            [status-im.components.common.common :refer [list-separator]]
            [status-im.components.react :refer [view
                                                text
                                                icon
                                                touchable-highlight]]))

(defn action-button [label icon-key on-press]
  [touchable-highlight {:on-press on-press}
   [view st/action-button
    [view st/action-button-icon-container
     [icon icon-key]]
    [view st/action-button-label-container
     [text {:style st/action-button-label}
      label]]]])

(defn action-button-disabled [label icon-key]
  [view st/action-button
   [view st/action-button-icon-container-disabled
    [view {:opacity 0.4}
     [icon icon-key]]]
   [view st/action-button-label-container
    [text {:style st/action-button-label-disabled}
     label]]])

(defn action-separator []
  [list-separator])
