(ns status-im.ui.screens.add-new.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as components.styles]))

(defn input-container []
  {:flex-direction   :row
   :align-items      :center
   :margin-bottom    16
   :border-radius    components.styles/border-radius
   :background-color colors/gray-lighter})