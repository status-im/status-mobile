(ns status-im.ui.screens.communities.styles
  (:require
   [quo2.foundations.colors :as colors]))

(def category-item
  {:flex           1
   :flex-direction :row
   :align-items    :center
   :height         52
   :padding-left   18})

(defn community-card [window-width radius]
  {:width            window-width
   :shadow-offset    {:width 0
                      :height 2}
   :shadow-radius    radius
   :shadow-opacity   1
   :shadow-color     colors/shadow
   :border-radius    radius
   :justify-content  :space-between
   :elevation        2
   :background-color colors/white})

(defn stats-count-container []
  {:flex-direction :row
   :align-items    :center
   :margin-right   16})

(defn card-stats-container []
  {:flex-direction :row
   :position       :absolute
   :top            116
   :left           12
   :right          12})

(defn list-stats-container []
  {:flex-direction :row
   :align-items    :center})

(defn community-tags-container  []
  {:flex-direction :row
   :position  :absolute
   :top       154
   :left      12
   :right     12})

(defn card-view-content-container []
  {:flex               1
   :position           :absolute
   :top                40
   :left               0
   :right              0
   :bottom             0
   :border-radius      16
   :padding-horizontal 12
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90)})

(defn card-view-chat-icon []
  {:border-radius    48
   :position         :absolute
   :top              -24
   :left             12
   :padding          2
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90)})

(defn list-view-content-container []
  {:flex-direction    :row
   :border-radius     16
   :align-items       :center
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90)})

(defn list-view-chat-icon []
  {:border-radius    32
   :padding          12})

(defn community-title-description-container []
  {:position  :absolute
   :top       32
   :left      12
   :right     12})

(defn community-cover-container []
  {:height                  64
   :border-top-right-radius 20
   :border-top-left-radius  20
   :background-color        colors/primary-50-opa-20})

(defn permission-tag-styles []
  {:position         :absolute
   :top              8
   :right            8})