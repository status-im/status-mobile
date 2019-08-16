(ns fiddle.frame
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]))

(def width 375)
(def height 568) ;667

(re-frame/reg-sub :dimensions/window-width (fn [_] width))
(re-frame/reg-sub :dimensions/window (fn [_] {:width width :height height}))

(defn frame [content]
  [react/view {:style {:width width :height height :border-color :black :border-width 1}}
   content])