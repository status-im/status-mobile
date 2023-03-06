(ns status-im2.contexts.chat.lightbox.zoomable-image.style
  (:require
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]))

(defn container
  [{:keys [width height]}
   {:keys [pan-x pan-y pinch-x pinch-y scale]}
   set-full-height?]
  (reanimated/apply-animations-to-style
   {:transform [{:translateX pan-x}
                {:translateY pan-y}
                {:translateX pinch-x}
                {:translateY pinch-y}
                {:scale scale}]}
   {:justify-content :center
    :align-items     :center
    :width           (if platform/ios? width "100%")
    :height          (if set-full-height? "100%" height)}))

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
