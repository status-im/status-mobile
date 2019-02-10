(ns status-im.ui.screens.add-new.styles
  (:require-macros [status-im.utils.styles :refer [defstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.styles :as styles]))

(def new-chat-container
  {:flex-direction :row
   :align-items    :center})

(def input-container
  {:flex-direction    :row
   :align-items       :center
   :border-radius     styles/border-radius
   :height            52
   :background-color  colors/gray-lighter
   :margin-horizontal 14
   :margin-top        24})

(def new-chat-input-container
  (merge
   input-container
   {:flex              1
    :margin-horizontal 0
    :margin-left       14}))

(def button-container
  {:justify-content    :center
   :border-radius      styles/border-radius
   :height             52
   :background-color   colors/gray-lighter
   :padding-horizontal 15
   :margin-right       14
   :margin-left        3
   :margin-top         24})

(defstyle input
  {:flex               1
   :font-size          15
   :letter-spacing     -0.2
   :padding-horizontal 14
   :desktop            {:height 30
                        :width "100%"}
   :android            {:padding 0}})
