(ns status-im2.common.top-bar.style
  (:require [quo.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defonce ^:const cover-height 168)
(defonce ^:const overscroll-cover-height 2000)
(defonce ^:const header-avatar-top-offset -36)

(defn header-container
  [show? theme]
  {:display          (if show? :flex :none)
   :background-color (colors/theme-colors colors/white colors/neutral-100 theme)
   :top              (- overscroll-cover-height)
   :margin-bottom    (- overscroll-cover-height)})

(defn header-cover
  [cover-bg-color theme]
  {:flex             1
   :height           (+ overscroll-cover-height cover-height)
   :background-color (colors/theme-colors
                       (colors/custom-color cover-bg-color 50 20)
                       (colors/custom-color cover-bg-color 50 40)
                       theme)})

(defn header-bottom-part
  [animation theme]
  (reanimated/apply-animations-to-style
    {:border-top-right-radius animation
     :border-top-left-radius  animation}
    {:top              -16
     :margin-bottom    -16
     :padding-bottom   24
     :background-color (colors/theme-colors colors/white colors/neutral-95 theme)}))

(def header-avatar
  {:top               header-avatar-top-offset
   :margin-horizontal 20
   :margin-bottom     header-avatar-top-offset})

(def header-description-text
  {:margin-top 8})