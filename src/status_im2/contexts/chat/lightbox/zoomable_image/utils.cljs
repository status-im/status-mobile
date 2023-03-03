(ns status-im2.contexts.chat.lightbox.zoomable-image.utils
  (:require
    [clojure.string :as string]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [status-im2.contexts.chat.lightbox.zoomable-image.constants :as c]
    [utils.re-frame :as rf]))


(defn get-dimensions
  "Calculates all required dimensions. Dimensions calculations are different on iOS and Android because landscape
   mode is implemented differently.On Android, we just need to resize the content, and the OS takes care of the
   animations. On iOS, we need to animate the content ourselves in code"
  [pixels-width pixels-height curr-orientation]
  (let [window                (rf/sub [:dimensions/window])
        landscape?            (string/includes? curr-orientation orientation/landscape)
        portrait?             (= curr-orientation orientation/portrait)
        window-width          (:width window)
        window-height         (:height window)
        screen-width          (if (or platform/ios? portrait?) window-width window-height)
        screen-height         (if (or platform/ios? portrait?) window-height window-width)
        portrait-image-width  window-width
        portrait-image-height (* pixels-height (/ window-width pixels-width))
        landscape-image-width (* pixels-width (/ window-width pixels-height))
        width                 (if landscape? landscape-image-width portrait-image-width)
        height                (if landscape? screen-height portrait-image-height)
        container-width       (if platform/ios? window-width width)
        container-height      (if (and platform/ios? landscape?) landscape-image-width height)]
    ;; width and height used in style prop
    {:image-width         (if platform/ios? portrait-image-width width)
     :image-height        (if platform/ios? portrait-image-height height)
     ;; container width and height, also used in animations calculations
     :width               container-width
     :height              container-height
     ;; screen width and height used in calculations, and depends on platform
     :screen-width        screen-width
     :screen-height       screen-height
     :x-threshold-scale   (/ screen-width (min screen-width container-width))
     :y-threshold-scale   (/ screen-height (min screen-height container-height))
     :landscape-scale-val (/ portrait-image-width portrait-image-height)}))

(defn handle-exit-lightbox-signal
  "On ios, when attempting to navigate back while zoomed in, the shared-element transition animation
   doesn't execute properly, so we need to zoom out first"
  [exit-lightbox-signal index scale rescale]
  (when (= exit-lightbox-signal index)
    (if (> scale c/min-scale)
      (do
        (rescale c/min-scale true)
        (js/setTimeout #(rf/dispatch [:navigate-back]) 70))
      (rf/dispatch [:navigate-back]))
    (js/setTimeout #(rf/dispatch [:chat.ui/exit-lightbox-signal nil]) 500)))

(defn handle-zoom-out-signal
  "Zooms out when pressing on another photo from the small bottom list"
  [zoom-out-signal index scale rescale]
  (when (and (= zoom-out-signal index) (> scale c/min-scale))
    (rescale c/min-scale true)))


;;; MATH
(defn get-max-offset
  [size screen-size scale]
  (/ (- (* size (min scale c/max-scale))
        screen-size)
     2))

(defn get-scale-diff
  [new-scale saved-scale]
  (- (dec new-scale)
     (dec saved-scale)))

(defn get-double-tap-offset
  [size screen-size focal]
  (let [center        (/ size 2)
        target-point  (* (- center focal) c/double-tap-scale)
        max-offset    (get-max-offset size screen-size c/double-tap-scale)
        translate-val (min (Math/abs target-point) max-offset)]
    (if (neg? target-point) (- translate-val) translate-val)))

(defn get-pinch-position
  [scale-diff size focal]
  (* (- (/ size 2) focal) scale-diff))
