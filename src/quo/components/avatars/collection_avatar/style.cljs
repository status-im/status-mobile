(ns quo.components.avatars.collection-avatar.style
  (:require
    [quo.foundations.colors :as colors]))

(defn- get-dimensions
  [size]
  (case size
    :size-24 24
    :size-20 20
    nil))

(defn collection-avatar-container
  [theme size]
  {:width           (get-dimensions size)
   :height          (get-dimensions size)
   :border-width    1
   :border-color    (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
   :border-radius   6
   :justify-content :center
   :align-items     :center})

(defn collection-avatar
  [size]
  {:width  (get-dimensions size)
   :height (get-dimensions size)})
