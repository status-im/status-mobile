(ns status-im.ui.screens.add-new.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

(def input-container
  {:flex-direction    :row
   :align-items       :center
   :border-radius     styles/border-radius
   :height            52
   :background-color  colors/gray-lighter
   :margin-horizontal 14
   :margin-top        24})

(defstyle input
  {:flex               1
   :font-size          15
   :letter-spacing     -0.2
   :padding-horizontal 14
   :android            {:padding 0}})
