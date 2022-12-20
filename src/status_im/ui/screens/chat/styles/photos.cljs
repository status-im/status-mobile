(ns status-im.ui.screens.chat.styles.photos
  (:require [quo.design-system.colors :as colors]))

(def default-size 36)

(defn radius [size] (/ size 2))

(defn photo-container
  [size]
  {:position      :relative
   :border-radius (radius size)})

(defn photo-border
  ([size] (photo-border size :absolute))
  ([size position]
   {:position      position
    :width         size
    :height        size
    :border-color  colors/black-transparent
    :border-width  1
    :border-radius (radius size)}))

(defn photo
  [size]
  {:border-radius    (radius size)
   :width            size
   :height           size
   :background-color colors/white})
