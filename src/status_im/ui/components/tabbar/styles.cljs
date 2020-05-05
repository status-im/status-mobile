(ns status-im.ui.components.tabbar.styles
  (:require [status-im.ui.components.animation :as animation]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]))

(def tabs-height
  (cond
    platform/android? 52
    platform/ios?     52
    platform/desktop? 36))

(def minimized-tabs-height 36)

(def tabs-diff (- tabs-height minimized-tabs-height))

(def counter
  {:right    0
   :top      0
   :position :absolute})

(defn counter-public-container []
  {:right            2
   :top              0
   :position         :absolute
   :border-radius    8
   :width            16
   :height           16
   :justify-content  :center
   :align-items      :center
   :background-color colors/white})

(def counter-public
  {:background-color colors/blue
   :width            12
   :border-radius    6
   :height           12})

;; NOTE: Extra padding to allow badge width to be up to 42 (in case of 99+)
;; 42 Max allowed width, 24 icon width as per spec, 16 left pos as per spec.
(def ^:private message-counter-left (+ (/ (- 42 24) 2) 16))

(def message-counter
  {:left     message-counter-left
   :bottom   6
   :position :absolute})

(def touchable-container
  {:flex   1
   :height tabs-height})

(def tab-container
  {:flex            1
   :height          tabs-height
   :align-items     :center
   :justify-content :space-between
   :padding-top     6
   :padding         4})

(def icon-container
  {:height          24
   :width           42
   :align-items     :center
   :justify-content :center})

(defn icon [active?]
  {:color  (if active? colors/blue colors/gray)
   :height 24
   :width  24})

(def tab-title-container
  {:align-self      :stretch
   :height          14
   :align-items     :center
   :justify-content :center})

(defn tab-title [active?]
  {:color     (if active? colors/blue colors/gray)
   :font-size 11})

(defn animated-container [visible? inset]
  {:flex-direction   :row
   :shadow-radius    4
   :shadow-offset    {:width 0 :height -5}
   :shadow-opacity   0.3
   :shadow-color     (if (colors/dark?)
                       "rgba(0, 0, 0, 0.75)"
                       "rgba(0, 9, 26, 0.12)")
   :elevation 8
   :background-color colors/white
   :position         :absolute
   :left             0
   :right            0
   :height           tabs-height
   :bottom           inset
   :transform        [{:translateY
                       (animation/interpolate visible?
                                              {:inputRange  [0 1]
                                               :outputRange [0 tabs-diff]})}]})

(defn ios-titles-cover [inset]
  {:background-color colors/white
   :position         :absolute
   :height           tabs-diff
   :align-self       :stretch
   :bottom           (- inset tabs-diff)
   :right            0
   :left             0})

(defn tabs-wrapper [keyboard visible]
  (merge {:padding-horizontal 8
          :elevation          8
          :left               0
          :right              0
          :bottom             0
          :transform          [{:translateY
                                (animation/interpolate visible
                                                       {:inputRange  [0 1]
                                                        :outputRange [0 tabs-height]})}]}
         (when keyboard
           {:position :absolute})))

(defn space-handler [inset]
  {:height (+ inset minimized-tabs-height)})
