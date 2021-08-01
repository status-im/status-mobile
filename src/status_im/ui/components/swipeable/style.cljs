(ns status-im.ui.components.swipeable.style
  (:require [status-im.ui.components.colors :as colors]))

(defn delete-action-container []
  {:flex              0.3
   :background-color  colors/red
   :align-items       :flex-end
   :justify-content   :center
   :padding           16})
