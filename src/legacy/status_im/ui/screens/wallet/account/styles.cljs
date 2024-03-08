(ns legacy.status-im.ui.screens.wallet.account.styles
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.animation :as animation]
    [legacy.status-im.ui.components.colors :as colors]))

(defn card
  [window-width color]
  {:width            (- window-width 30)
   :height           161
   :background-color (if (string/blank? color)
                       colors/blue
                       color)
   :shadow-offset    {:width 0 :height 2}
   :shadow-radius    8
   :shadow-opacity   1
   :shadow-color     (if (colors/dark?)
                       "rgba(0, 0, 0, 0.75)"
                       "rgba(0, 9, 26, 0.12)")
   :elevation        2
   :border-radius    8
   :justify-content  :space-between})

(defn divider
  []
  {:height           52
   :width            1
   :background-color colors/black-transparent-20
   :shadow-offset    {:width 0 :height 2}
   :shadow-radius    8
   :shadow-opacity   1
   :shadow-color     (if (colors/dark?)
                       "rgba(0, 0, 0, 0.75)"
                       "rgba(0, 9, 26, 0.12)")})

(defn bottom-send-recv-buttons-raise
  [anim-y]
  (animation/timing
   anim-y
   {:toValue         0
    :duration        200
    :easing          (.-ease ^js animation/easing)
    :useNativeDriver true}))

(defn bottom-send-recv-buttons-lower
  [anim-y y]
  (animation/timing
   anim-y
   {:toValue         y
    :duration        200
    :easing          (.-ease ^js animation/easing)
    :useNativeDriver true}))

(def round-action-button
  {:background-color colors/blue
   :height           44
   :flex             1
   :justify-content  :center
   :align-items      :center
   :width            44
   :border-radius    44})

(def top-actions
  {:flex            1
   :flex-direction  :row
   :justify-content :space-between
   :width           "60%"
   :align-self      :center})
