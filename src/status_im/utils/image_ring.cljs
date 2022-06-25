(ns status-im.utils.image-ring)

(defn to-draw-ring-params [images color-hash theme]
  (map (fn [image] {:uri (:uri image)
                    :height (:height image)
                    :width (:width image)
                    :colorHash color-hash
                    :theme theme}) images))
