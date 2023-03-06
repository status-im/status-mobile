(ns status-im2.contexts.chat.lightbox.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.lightbox.common :as common]))

;;;; MAIN-VIEW
(def container-view
  {:background-color :black})

;;;; TOP-VIEW
(defn top-view-container
  [top-inset {:keys [opacity rotate top-view-y top-view-x top-view-width top-view-bg top-layout]}
   window-width
   bg-color]
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
    :top                (if platform/ios? top-inset 0)
    :height             common/top-view-height
    :z-index            4
    :flex-direction     :row
    :justify-content    :space-between
    :width              (when platform/android? window-width)
    :background-color   (when platform/android? bg-color)
    :align-items        :center}))

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
  [insets {:keys [opacity bottom-layout]}]
  (reanimated/apply-animations-to-style
   {:transform [{:translateY bottom-layout}]
    :opacity   opacity}
   {:position       :absolute
    :bottom         0
    :padding-bottom (:bottom insets)
    :z-index        3}))

(defn content-container
  [padding-horizontal]
  {:padding-vertical   12
   :padding-horizontal padding-horizontal
   :align-items        :center
   :justify-content    :center})

(def text-style
  {:color             colors/white
   :align-self        :center
   :margin-horizontal 20
   :margin-vertical   12})
