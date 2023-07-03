(ns quo2.components.loaders.skeleton.style
  (:require [quo2.components.loaders.skeleton.constants :as constants]))

(defn container
  [content]
  {:flex-direction :row
   :padding        12
   :padding-top    (get-in constants/layout-dimensions [content :padding-top])
   :height         (get-in constants/layout-dimensions [content :height])})

(defn avatar
  [color]
  {:height           32
   :width            32
   :border-radius    16
   :background-color color})

(def content-container
  {:padding-left 8})

(defn content-view
  [{:keys [type index content color]}]
  (let [{:keys [width height margin-bottom margin-top]}
        (get-in constants/content-dimensions [content index type])]
    {:height           height
     :width            width
     :border-radius    6
     :background-color color
     :margin-bottom    margin-bottom
     :margin-top       margin-top}))
