(ns quo.components.loaders.skeleton-list.style
  (:require
    [quo.components.loaders.skeleton-list.constants :as constants]))

(defn container
  [content]
  {:flex-direction :row
   :align-items    :center
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
  {:flex         1
   :padding-left 8})

(def right-content-container
  {:align-items :flex-end})

(def right-bottom-content-container
  {:flex-direction :row})

(defn author
  [color]
  {:height           10
   :width            10
   :border-radius    6
   :background-color color})

(defn content-view
  [{:keys [type index content color]}]
  (let [{:keys [width height margin-bottom margin-right]}
        (get-in constants/content-dimensions [content index type])]
    {:height           height
     :width            width
     :border-radius    6
     :background-color color
     :margin-bottom    margin-bottom
     :margin-right     margin-right}))
