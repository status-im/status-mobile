(ns status-im2.contexts.chat.lightbox.zoomable-image.style
  (:require
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]))

(defn container
  [{:keys [width height]}
   {:keys [pan-x pan-y pinch-x pinch-y scale]}
   set-full-height?
   portrait?
   margin-horizontal]
  (prn margin-horizontal "!!!!!!!!!!!!!!!!!" width
       (or platform/ios? portrait?))
  (reanimated/apply-animations-to-style
   {:transform [{:translateX pan-x}
                {:translateY pan-y}
                {:translateX pinch-x}
                {:translateY pinch-y}
                {:scale scale}]
    :margin-horizontal margin-horizontal
    }
   {:justify-content   :center
    :align-items       :center
    :flex 1
    #_#_:width             (when-not (or portrait? platform/ios?)
                         "100%")
    #_#_:width             (if (or platform/ios? portrait?) width "100%")
    :height            (if set-full-height? "100%" height)
    :overflow          :hidden}))

(defn image
  [{:keys [image-width image-height]}
   {:keys [rotate rotate-scale]}
   border-radius]
  (reanimated/apply-animations-to-style
   {:transform     [{:rotate rotate}
                    {:scale rotate-scale}]
    :border-radius border-radius}
   {:height image-height
    :width  image-width
    }))
