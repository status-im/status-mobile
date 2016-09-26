(ns status-im.chat.styles.plain-message
  (:require [status-im.components.styles :refer [text1-color]]
            [status-im.chat.constants :refer [max-input-height
                                              min-input-height]]))

(defn message-input-button-touchable [width content-height]
  {:width            width
   :flex             1
   :margin-bottom    14
   :align-items      :center
   :justify-content  :flex-end})

(defn message-input-button [scale]
  {:transform       [{:scale scale}]
   :width           24
   :height          24
   :align-items     :center
   :justify-content :center})

(def list-icon
  {:width  20
   :height 16})

(def requests-icon
  {:background-color :#7099e6
   :width            8
   :height           8
   :border-radius    8
   :left             0
   :top              0
   :position         :absolute})

(def close-icon
  {:width  12
   :height 12})

(def message-input
  {:flex        1
   :padding     0
   :font-size   14
   :line-height 20
   :color       text1-color})

(def smile-icon
  {:width  20
   :height 20})
