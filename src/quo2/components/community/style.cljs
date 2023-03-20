(ns quo2.components.community.style
  (:require [quo2.foundations.colors :as colors]))

(def category-item
  {:flex           1
   :flex-direction :row
   :align-items    :center
   :height         52
   :padding-left   18})

(defn community-card
  [radius]
  {:shadow-offset    {:width  0
                      :height 2}
   :shadow-radius    radius
   :shadow-opacity   1
   :shadow-color     colors/shadow
   :elevation        1
   :border-radius    radius
   :justify-content  :space-between
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90)})

(defn stats-count-container
  []
  {:flex-direction :row
   :align-items    :center
   :margin-right   16})

(defn card-stats-container
  []
  {:flex-direction :row})

(defn list-stats-container
  []
  {:flex-direction :row
   :align-items    :center})

(defn community-tags-container
  []
  {:flex-direction :row})

(defn card-stats-position
  []
  {:position :absolute
   :top      116
   :right    12
   :left     12})

(defn community-tags-position
  []
  {:position :absolute
   :top      154
   :right    12
   :left     12})

(defn card-view-content-container
  [padding-horizontal]
  {:position                :absolute
   :top                     40
   :bottom                  0
   :left                    0
   :right                   0
   :height                  20
   :padding-horizontal      padding-horizontal
   :border-top-right-radius 16
   :border-top-left-radius  16
   :background-color        (colors/theme-colors
                             colors/white
                             colors/neutral-90)})

(defn card-view-chat-icon
  [icon-size]
  {:border-radius    48
   :position         :absolute
   :top              (- (/ icon-size 2))
   :left             (/ icon-size 4)
   :padding          2
   :background-color (colors/theme-colors
                      colors/white
                      colors/neutral-90)})

(defn list-info-container
  []
  {:flex-direction     :row
   :border-radius      16
   :padding-horizontal 12
   :align-items        :center
   :padding-vertical   8})

(defn membership-info-container
  []
  {:flex-direction :row
   :border-radius  16
   :align-items    :center
   :height         48})

(defn community-title-description-container
  [margin-top]
  {:margin-top margin-top})

(defn community-cover-container
  [height]
  {:flex-direction          :row
   :height                  height
   :border-top-right-radius 20
   :border-top-left-radius  20
   :background-color        colors/primary-50-opa-20})

(defn permission-tag-styles
  []
  {:position :absolute
   :top      8
   :right    8})
