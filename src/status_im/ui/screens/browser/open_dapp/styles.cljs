(ns status-im.ui.screens.browser.open-dapp.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

(def input-container
  {:flex-direction    :row
   :align-items       :center
   :border-radius     styles/border-radius
   :height            36
   :background-color  colors/gray-lighter
   :margin-horizontal 16
   :margin-bottom     9
   :margin-top        24})

(defstyle input
  {:flex               1
   :font-size          15
   :padding-horizontal 14
   :desktop            {:height 30
                        :width "100%"}
   :android            {:padding 0}})