(ns quo2.components.community.icon
  (:require [react-native.core :as rn]))

(defn community-icon
  [{:keys [images]} size]
  (let [thumbnail-image (get-in images [:thumbnail :uri])]
    [rn/image
     {:source {:uri thumbnail-image}
      :style  {:border-radius 50
               :border-width  0
               :border-color  :transparent
               :width         size
               :height        size}}]))
