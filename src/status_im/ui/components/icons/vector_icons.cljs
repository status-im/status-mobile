(ns status-im.ui.components.icons.vector-icons
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors])
  (:refer-clojure :exclude [use]))

(defn- match-color [color]
  (cond
    (keyword? color)
    (case color
      :dark colors/black
      :gray colors/gray
      :blue colors/blue
      :active colors/blue
      :white colors/white
      :red colors/red
      colors/black)
    (string? color)
    color
    :else
    colors/black))

(defn icon
  ([name] (icon name nil))
  ([name {:keys [color container-style accessibility-label width height]
          :or   {accessibility-label :icon}}]
   ^{:key name}
   [react/view
    {:style               (or
                           container-style
                           {:width  (or width 24)
                            :height (or height 24)})
     :accessibility-label accessibility-label}
    [react/image {:source {:uri (keyword (clojure.core/name name))}
                  :style  (cond-> {:width  (or width 24)
                                   :height (or height 24)}
                            color
                            (assoc :tint-color (match-color color)))}]]))

(defn tiny-icon
  ([name] (tiny-icon name {}))
  ([name options]
   (icon name (merge {:width 16 :height 16}
                     options))))