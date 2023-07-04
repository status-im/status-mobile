(ns status-im2.contexts.chat.lightbox.text-sheet.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.chat.lightbox.constants :as c]))

(defn sheet-container
  [{:keys [height top]}]
  (reanimated/apply-animations-to-style
   {:height height
    :top    top}
   {:position :absolute
    :left     0
    :right    0}))

(def text-style
  {:color             colors/white
   :align-self        :center
   :margin-horizontal 20
   :margin-bottom     c/text-margin
   :flex-grow         1})

(def bar-container
  {:height          c/bar-container-height
   :left            0
   :right           0
   :top             0
   :z-index         1
   :justify-content :center
   :align-items     :center})

(def bar
  {:width            32
   :height           4
   :border-radius    100
   :background-color colors/white-opa-40
   :border-width     0.5
   :border-color     colors/neutral-100})

(defn top-gradient
  [{:keys [gradient-opacity]} insets]
  (reanimated/apply-animations-to-style
   {:opacity gradient-opacity}
   {:position :absolute
    :left     0
    :right    0
    :top      (- (+ (:top insets)
                    c/top-view-height))
    :height   (+ (:top insets)
                 c/top-view-height
                 c/bar-container-height
                 c/text-margin
                 (* c/line-height 2))
    :z-index  1}))

(def bottom-gradient
  {:position :absolute
   :left     0
   :right    0
   :height   28
   :bottom   0
   :z-index  1})
