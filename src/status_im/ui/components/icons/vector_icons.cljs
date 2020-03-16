(ns status-im.ui.components.icons.vector-icons
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]
            [clojure.string :as string])
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
      :none nil
      colors/black)
    (string? color)
    color
    :else
    colors/black))

(defn icon-source [name]
  (if platform/desktop?
    {:uri (keyword (string/replace (clojure.core/name name) "-" "_"))}
    {:uri (keyword (clojure.core/name name))}))

(defn icon
  ([name] (icon name nil))
  ([name {:keys [color resize-mode container-style
                 accessibility-label width height]
          :or   {accessibility-label :icon}}]
   ^{:key name}
   [react/view
    {:style               (or
                           container-style
                           {:width  (or width 24)
                            :height (or height 24)})
     :accessibility-label accessibility-label}
    [react/image {:style  (cond-> {:width  (or width 24)
                                   :height (or height 24)}

                            resize-mode
                            (assoc :resize-mode resize-mode)

                            :always
                            (assoc :tint-color (match-color color)))
                  :source (icon-source name)}]]))

(defn tiny-icon
  ([name] (tiny-icon name {}))
  ([name options]
   (icon name (merge {:width 16 :height 16}
                     options))))
