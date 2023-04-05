(ns quo2.components.share.qr-code.style)

(def container
  {:flex-direction  :row
   :justify-content :center})

(defn image
  [width height]
  {:width         width
   :height        height
   :border-radius 12
   :aspect-ratio  1})
