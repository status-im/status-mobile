(ns status-im.contexts.communities.discover.style
  (:require
    [status-im.contexts.shell.constants :as shell.constants]))

(def header-height 56)

(defn screen-title-container
  [safe-area-top]
  {:height            header-height
   :padding-vertical  12
   :justify-content   :center
   :margin-horizontal 20
   :margin-top        (+ header-height safe-area-top)})

(def featured-communities-header
  {:flex-direction  :row
   :height          30
   :padding-top     6
   :margin-bottom   7
   :padding-right   20
   :margin-left     20
   :align-items     :center
   :justify-content :space-between})

(def featured-communities-title-container
  {:flex-direction :row
   :align-items    :center})

(def featured-list-container
  {:flex-direction :row
   :overflow       :hidden})

(def flat-list-container
  {:padding-bottom     24
   :padding-horizontal 20})

(def other-communities-container
  {:flex              1
   :padding-bottom    (+ shell.constants/floating-shell-button-height 34)
   :margin-horizontal 20})

(defn discover-communities-segments
  [fixed?]
  (merge
   {:padding-vertical 12
    :height           header-height
    :background-color :transparent}
   (when-not fixed?
     {:margin-top        12
      :margin-horizontal 20
      :margin-bottom     4})))

(defn discover-screen-container
  [background-color]
  {:background-color background-color
   :position         :absolute
   :top              0
   :bottom           0
   :left             0
   :right            0})

(def communities-header-container
  {:align-items     :center
   :justify-content :center})

(defn blur-tabs-header
  [safe-area-top]
  {:padding-horizontal 20
   :position           :absolute
   :top                (+ header-height safe-area-top)
   :height             header-height
   :right              0
   :left               0
   :justify-content    :center
   :flex               1
   :background-color   :transparent})
