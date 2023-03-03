(ns status-im2.contexts.chat.lightbox.zoomable-image.style
  (:require [react-native.reanimated :as reanimated]))

(defn container
  [{:keys [width height]}
   {:keys [pan-x pan-y pinch-x pinch-y scale]}]
  (reanimated/apply-animations-to-style
   {:transform [{:translateX pan-x}
                {:translateY pan-y}
                {:translateX pinch-x}
                {:translateY pinch-y}
                {:scale scale}]}
   {:justify-content :center
    :align-items     :center
    :width           width
    :height          height}))

(defn image
  [{:keys [image-width image-height]}
   {:keys [rotate rotate-scale]}
   border-radius]
  (reanimated/apply-animations-to-style
   {:transform     [{:rotate rotate}
                    {:scale rotate-scale}]
    :border-radius border-radius}
   {:width  image-width
    :height image-height}))
