(ns status-im2.contexts.chat.lightbox.zoomable-image.style
  (:require
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]))

(defn container
  [{:keys [width height]}
   {:keys [pan-x pan-y pinch-x pinch-y scale]}
   full-screen-scale
   set-full-height?
   portrait?]
  (reanimated/apply-animations-to-style
   {:transform [{:translateX pan-x}
                {:translateY pan-y}
                {:translateX pinch-x}
                {:translateY pinch-y}
                {:scale scale}
                {:scale full-screen-scale}]}
   {:justify-content :center
    :align-items     :center
    :width           (if (or platform/ios? portrait?) width "100%")
    :height          (if set-full-height? "100%" height)}))

(defn image
  [{:keys [image-width image-height]}
   {:keys [rotate rotate-scale]}
   {:keys [border-value images-opacity]}
   index]
  (reanimated/apply-animations-to-style
   {:transform     [{:rotate rotate}
                    {:scale rotate-scale}]
    :opacity       (nth images-opacity index)
    :border-radius border-value}
   {:width  image-width
    :height image-height}))
