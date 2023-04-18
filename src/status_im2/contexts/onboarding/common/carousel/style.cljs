(ns status-im2.contexts.onboarding.common.carousel.style
  (:require
    [quo2.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]))

(defn header-container
  [status-bar-height content-width index]
  {:position         :absolute
   :top              0
   :left             (* content-width index)
   :padding-top      (+ 30 status-bar-height)
   :width            content-width
   :height           (+ 96 status-bar-height)
   :flex-direction   :row
   :background-color colors/onboarding-header-black})

(defn header-text-view
  [window-width]
  {:flex-direction :column
   :width          window-width
   :padding-left   20})

(def carousel-text
  {:color colors/white})

(def carousel-sub-text
  {:color      colors/white
   :margin-top 2})

(defn background-image
  [content-width]
  {:resize-mode :stretch
   :width       content-width})

(defn progress-bar-item
  [static? end?]
  {:height           2
   :flex             1
   :background-color (if static? colors/white-opa-10 colors/white)
   :margin-right     (if end? 0 8)
   :border-radius    4})

(defn progress-bar
  [width]
  {:position       :absolute
   :top            0
   :width          width
   :flex-direction :row})

(defn dynamic-progress-bar
  [width]
  (reanimated/apply-animations-to-style
   {:width width}
   {:height        2
    :border-radius 4
    :overflow      :hidden}))

(defn progress-bar-container
  [progress-bar-width status-bar-height]
  {:position    :absolute
   :width       progress-bar-width
   :margin-left 20
   :top         (+ 12 status-bar-height)})

(defn carousel-container
  [left]
  (reanimated/apply-animations-to-style
   {:left left}
   {:position       :absolute
    :right          0
    :top            0
    :bottom         0
    :flex-direction :row}))
