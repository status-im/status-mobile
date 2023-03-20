(ns status-im2.contexts.chat.lightbox.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.lightbox.constants :as c]))

;;;; VIEW
(defn image
  [width height]
  {:flex-direction  :row
   :width           width
   :height          height
   :align-items     :center
   :justify-content :center})

;;;; TOP-VIEW
(defn top-view-container
  [top-inset window-width bg-color landscape?
   {:keys [opacity rotate top-view-y top-view-x top-view-width top-view-bg]}
   {:keys [top-layout]}]
  (reanimated/apply-animations-to-style
   (if platform/ios?
     {:transform        [{:translateY top-layout}
                         {:rotate rotate}
                         {:translateY top-view-y}
                         {:translateX top-view-x}]
      :opacity          opacity
      :width            top-view-width
      :background-color top-view-bg}
     {:transform [{:translateY top-layout}]
      :opacity   opacity})
   {:position           :absolute
    :padding-horizontal 20
    :top                (if (or platform/ios? (not landscape?)) top-inset 0)
    :height             c/top-view-height
    :z-index            4
    :flex-direction     :row
    :justify-content    :space-between
    :width              (when platform/android? window-width)
    :background-color   (when platform/android? bg-color)
    :align-items        :center}))

(defn top-gradient
  [insets]
  {:position :absolute
   :height   (+ c/top-view-height (:top insets) 0)
   :top      (- (:top insets))
   :left     0
   :right    0})

(def close-container
  {:width            32
   :height           32
   :border-radius    12
   :justify-content  :center
   :align-items      :center
   :background-color colors/neutral-80-opa-40})

(def top-right-buttons
  {:flex-direction :row})

;;;; BOTTOM-VIEW
(defn gradient-container
  [insets {:keys [opacity]} {:keys [bottom-layout]}]
  (reanimated/apply-animations-to-style
   {:transform [{:translateY bottom-layout}]
    :opacity   opacity}
   {:position       :absolute
    :bottom         0
    :padding-bottom (if platform/ios?
                      (:bottom insets)
                      (+ (:bottom insets) c/small-list-padding-vertical c/focused-extra-size))
    :z-index        3}))

(defn content-container
  [padding-horizontal]
  {:padding-vertical   c/small-list-padding-vertical
   :padding-horizontal padding-horizontal
   :align-items        :center
   :justify-content    :center})

(def text-style
  {:color             colors/white
   :align-self        :center
   :margin-horizontal 20
   :margin-vertical   12})
