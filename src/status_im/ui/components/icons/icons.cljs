(ns status-im.ui.components.icons.icons
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors])
  (:refer-clojure :exclude [use])
  (:require-macros [status-im.ui.components.icons.icons :as icons]))

(def icons (icons/resolve-icons))

(defn icon-source [icon]
  (get icons (name icon)))

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
