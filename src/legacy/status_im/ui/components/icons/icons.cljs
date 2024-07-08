(ns legacy.status-im.ui.components.icons.icons
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react])
  (:refer-clojure :exclude [use])
  (:require-macros [legacy.status-im.ui.components.icons.icons :as icons]))

(def icons (icons/resolve-icons))

(defn icon-source
  [icon]
  (get icons (name icon)))

(defn- match-color
  [color]
  (cond
    (keyword? color)
    (case color
      :dark   colors/black
      :gray   colors/gray
      :blue   colors/blue
      :active colors/blue
      :white  colors/white
      :red    colors/red
      :none   nil
      colors/black)
    (string? color)
    color
    :else
    colors/black))

(defn memo-icon-fn
  ([name] (memo-icon-fn name nil))
  ([name
    {:keys [color resize-mode container-style
            accessibility-label width height no-color]
     :or   {accessibility-label :icon}}]
   ^{:key name}
   [react/image
    {:style               (merge (cond-> {:width  (or width 24)
                                          :height (or height 24)}

                                   resize-mode
                                   (assoc :resize-mode resize-mode)

                                   (not no-color)
                                   (assoc :tint-color (match-color color)))
                                 container-style)
     :accessibility-label accessibility-label
     :source              (icon-source name)}]))

(def icon (memoize memo-icon-fn))

(defn tiny-icon
  ([name] (tiny-icon name {}))
  ([name options]
   (icon name
         (merge {:width 16 :height 16}
                options))))
