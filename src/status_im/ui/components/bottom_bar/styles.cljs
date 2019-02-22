(ns status-im.ui.components.bottom-bar.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.animation :as animation])
  (:require-macros [status-im.utils.styles :refer [defnstyle]]))

(def tabs-height
  (cond
    platform/android? 52
    platform/ios? 52
    platform/desktop? 68))

(def tab-height (dec tabs-height))

(def tabs-container
  {:flex-direction   :row
   :height           tabs-height
   :background-color colors/white
   :border-top-width 1
   :border-top-color colors/black-transparent})

(def tab-container
  {:height          tabs-height
   :justify-content :center
   :align-items     :center})

(defnstyle tab-title [active?]
  {:ios        {:font-size 11}
   :android    {:font-size 12}
   :desktop    {:font-size   12
                :font-weight (if active? "600" :normal)}
   :text-align :center
   :color      (if active?
                 colors/blue
                 colors/gray)})

(def counter
  {:right    0
   :top      0
   :position :absolute})

(def touchable-container
  {:flex   1
   :height tabs-height})

(def new-tab-container
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

(defn new-tab-title [active?]
  {:color     (if active? colors/blue colors/gray)
   :font-size 11})

(def new-tabs-container
  {:height           tabs-height
   :align-self       :stretch
   :background-color :white
   :shadow-radius    4
   :shadow-offset    {:width 0 :height -5}
   :shadow-opacity   0.3
   :shadow-color     "rgba(0, 9, 26, 0.12)"})

(def tabs
  {:height         tabs-height
   :align-self     :stretch
   :padding-left   8
   :padding-right  8
   :flex-direction :row})

(defn animated-container [visible? keyboard-shown?]
  {:bottom           0
   :left             0
   :right            0
   :background-color :white
   :elevation        8
   :position         (when keyboard-shown? :absolute)
   :transform        [{:translateY
                       (animation/interpolate
                        visible?
                        {:inputRange  [0 1]
                         :outputRange [tabs-height 0]})}]})

