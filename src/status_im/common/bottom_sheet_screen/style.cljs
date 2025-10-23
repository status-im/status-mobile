(ns status-im.common.bottom-sheet-screen.style
  (:require
    [quo.context]
    [quo.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]
    [status-im.constants :as constants]))

(defn container
  [padding-top]
  {:flex        1
   :padding-top padding-top})

(defn background
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:background-color colors/neutral-100-opa-70
    :position         :absolute
    :top              0
    :bottom           0
    :left             0
    :right            0}))

(defn main-view
  [translate-y theme]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-y translate-y}]}
   {:background-color        (colors/theme-colors colors/white colors/neutral-95 theme)
    :border-top-left-radius  20
    :border-top-right-radius 20
    :flex                    1
    :overflow                :hidden
    :padding-top             20}))

(def handle-container
  {:left            0
   :right           0
   :top             0
   :height          constants/sheet-screen-handle-height
   :z-index         2
   :position        :absolute
   :justify-content :center
   :align-items     :center})

(defn handle
  [theme]
  {:width            32
   :height           4
   :border-radius    100
   :background-color (colors/theme-colors colors/neutral-100 colors/white theme)
   :opacity          (if (= theme :light) 0.05 0.1)})
