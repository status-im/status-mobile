(ns status-im.ui.components.unviewed-indicator
  (:require [status-im.ui.components.badge :as badge]
            [status-im.ui.components.react :as react]))

(defn unviewed-indicator [c]
  (when (pos? c)
    [react/view {:padding-left    16
                 :justify-content :flex-end
                 :align-items     :flex-end}
     [badge/message-counter c]]))
