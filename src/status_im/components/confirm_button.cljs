(ns status-im.components.confirm-button
  (:require [status-im.components.styles :as st]
            [status-im.components.react :refer [view
                                                text
                                                touchable-highlight]]))

(defn confirm-button [label on-press]
  [touchable-highlight {:on-press on-press}
   [view st/confirm-button
    [text {:style st/confirm-button-label} label]]])