(ns status-im2.contexts.chat.lightbox.zoomable-image.utils
  (:require
    [clojure.string :as string]
    [react-native.core :as rn]
    [react-native.navigation :as navigation]
    [react-native.orientation :as orientation]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im2.contexts.chat.lightbox.zoomable-image.constants :as constants]
    [status-im2.contexts.chat.lightbox.animations :as anim]
    [utils.re-frame :as rf]))

;;; Helpers
(defn center-x
  [{:keys [pinch-x pinch-x-start pan-x pan-x-start]} exit?]
  (let [duration (if exit? 100 constants/default-duration)]
    (anim/animate pinch-x constants/init-offset duration)
    (anim/set-val pinch-x-start constants/init-offset)
    (anim/animate pan-x constants/init-offset duration)
    (anim/set-val pan-x-start constants/init-offset)))

(defn center-y
  [{:keys [pinch-y pinch-y-start pan-y pan-y-start]} exit?]
  (let [duration (if exit? 100 constants/default-duration)]
    (anim/animate pinch-y constants/init-offset duration)
    (anim/set-val pinch-y-start constants/init-offset)
    (anim/animate pan-y constants/init-offset duration)
    (anim/set-val pan-y-start constants/init-offset)))

(defn reset-values
  [exit? animations {:keys [focal-x focal-y]}]
  (center-x animations exit?)
  (center-y animations exit?)
  (reset! focal-x nil)
  (reset! focal-y nil))

(defn rescale-image
  [value
   exit?
   {:keys [x-threshold-scale y-threshold-scale]}
   {:keys [scale saved-scale] :as animations}
   {:keys [pan-x-enabled? pan-y-enabled?] :as state}]
  (when (= value constants/min-scale)
    (reset-values exit? animations state))
  (anim/animate scale value (if exit? 100 constants/default-duration))
  (anim/set-val saved-scale value)
  (reset! pan-x-enabled? (> value x-threshold-scale))
  (reset! pan-y-enabled? (> value y-threshold-scale)))

;;; Handlers
(defn handle-orientation-change
  [curr-orientation
   focused?
   {:keys [landscape-scale-val x-threshold-scale y-threshold-scale]}
   {:keys [rotate rotate-scale scale] :as animations}
   {:keys [pan-x-enabled? pan-y-enabled?]}]
  (if focused?
    (cond
      (= curr-orientation orientation/landscape-left)
      (do
        (anim/animate rotate "90deg")
        (anim/animate rotate-scale landscape-scale-val))
      (= curr-orientation orientation/landscape-right)
      (do
        (anim/animate rotate "-90deg")
        (anim/animate rotate-scale landscape-scale-val))
      (= curr-orientation orientation/portrait)
      (do
        (anim/animate rotate constants/init-rotation)
        (anim/animate rotate-scale constants/min-scale)))
    (cond
      (= curr-orientation orientation/landscape-left)
      (do
        (anim/set-val rotate "90deg")
        (anim/set-val rotate-scale landscape-scale-val))
      (= curr-orientation orientation/landscape-right)
      (do
        (anim/set-val rotate "-90deg")
        (anim/set-val rotate-scale landscape-scale-val))
      (= curr-orientation orientation/portrait)
      (do
        (anim/set-val rotate constants/init-rotation)
        (anim/set-val rotate-scale constants/min-scale))))
  (center-x animations false)
  (center-y animations false)
  (reset! pan-x-enabled? (> (anim/get-val scale) x-threshold-scale))
  (reset! pan-y-enabled? (> (anim/get-val scale) y-threshold-scale)))

;; On ios, when attempting to navigate back while zoomed in, the shared-element transition animation
;; doesn't execute properly, so we need to zoom out first
(defn handle-exit-lightbox-signal
  [exit-lightbox-signal index scale rescale set-full-height?]
  (when (= exit-lightbox-signal index)
    (reset! set-full-height? false)
    (if (> scale constants/min-scale)
      (do
        (rescale constants/min-scale true)
        (js/setTimeout #(rf/dispatch [:navigate-back]) 70))
      (rf/dispatch [:navigate-back]))
    (js/setTimeout #(rf/dispatch [:chat.ui/exit-lightbox-signal nil]) 500)))

(defn handle-zoom-out-signal
  "Zooms out when pressing on another photo from the small bottom list"
  [zoom-out-signal index scale rescale]
  (when (and (= zoom-out-signal index) (> scale constants/min-scale))
    (rescale constants/min-scale true)))

(defn toggle-opacity
  [index {:keys [opacity-value border-value full-screen-scale transparent? props]} portrait?]
  (let [{:keys [small-list-ref timers text-sheet-lock?]} props
        opacity                                          (reanimated/get-shared-value opacity-value)
        scale-value                                      (+ 1
                                                            (/ constants/margin
                                                               (:width (rn/get-window))))]
    (if (= opacity 1)
      (do
        (js/clearTimeout (:show-0 @timers))
        (js/clearTimeout (:show-1 @timers))
        (js/clearTimeout (:show-2 @timers))
        (swap! timers assoc
          :hide-0
          (js/setTimeout #(navigation/merge-options "lightbox" {:statusBar {:visible false}})
                         (if platform/ios? 75 0)))
        (anim/animate opacity-value 0)
        (swap! timers assoc :hide-1 (js/setTimeout #(reset! transparent? (not @transparent?)) 400))
        (reset! text-sheet-lock? true)
        (js/setTimeout #(reset! text-sheet-lock? false) 300))
      (do
        (js/clearTimeout (:hide-0 @timers))
        (js/clearTimeout (:hide-1 @timers))
        (reset! transparent? (not @transparent?))
        (swap! timers assoc :show-0 (js/setTimeout #(anim/animate opacity-value 1) 50))
        (swap! timers assoc
          :show-1
          (js/setTimeout #(when @small-list-ref
                            (.scrollToIndex ^js @small-list-ref #js {:animated false :index index}))
                         100))
        (when portrait?
          (swap! timers assoc
            :show-2
            (js/setTimeout #(navigation/merge-options "lightbox" {:statusBar {:visible true}})
                           (if platform/ios? 150 50))))))
    (anim/animate border-value (if (= opacity 1) 0 16))
    (anim/animate full-screen-scale (if (= opacity 1) scale-value 1))))

;;; Dimensions
(defn get-dimensions
  "Calculates all required dimensions. Dimensions calculations are different on iOS and Android because landscape
   mode is implemented differently.On Android, we just need to resize the content, and the OS takes care of the
   animations. On iOS, we need to animate the content ourselves in code"
  [pixels-width pixels-height curr-orientation
   {:keys [window-width screen-width screen-height]}]
  (let [landscape?            (string/includes? curr-orientation orientation/landscape)
        portrait-image-width  window-width
        portrait-image-height (* pixels-height (/ window-width pixels-width))
        landscape-image-width (* pixels-width (/ window-width pixels-height))
        width                 (if landscape? landscape-image-width portrait-image-width)
        height                (if landscape? screen-height portrait-image-height)
        container-width       (- (if platform/ios? window-width width) constants/margin)
        container-height      (- (if (and platform/ios? landscape?) landscape-image-width height)
                                 constants/margin)]
    ;; width and height used in style prop
    {:image-width         (- (if platform/ios? portrait-image-width width) constants/margin)
     :image-height        (- (if platform/ios? portrait-image-height height) constants/margin)
     ;; container width and height, also used in animations calculations
     :width               container-width
     :height              container-height
     ;; screen width and height used in calculations, and depends on platform
     :screen-width        screen-width
     :screen-height       screen-height
     :x-threshold-scale   (/ screen-width (min screen-width container-width))
     :y-threshold-scale   (/ screen-height (min screen-height container-height))
     :landscape-scale-val (/ portrait-image-width portrait-image-height)}))


;;; MATH
(defn get-max-offset
  [size screen-size scale]
  (/ (- (* size (min scale constants/max-scale))
        screen-size)
     2))

(defn get-scale-diff
  [new-scale saved-scale]
  (- (dec new-scale)
     (dec saved-scale)))

(defn get-double-tap-offset
  [size screen-size focal]
  (let [center        (/ size 2)
        target-point  (* (- center focal) constants/double-tap-scale)
        max-offset    (get-max-offset size screen-size constants/double-tap-scale)
        translate-val (min (Math/abs target-point) max-offset)]
    (if (neg? target-point) (- translate-val) translate-val)))

(defn get-pinch-position
  [scale-diff size focal]
  (* (- (/ size 2) focal) scale-diff))

(defn get-focal
  [focal size screen-size]
  (let [min-size (/ (- screen-size size) 2)
        max-size (+ min-size size)]
    (if (or (> focal max-size) (< focal min-size))
      (/ screen-size 2)
      focal)))

;;; INITIALIZATIONS
(defn init-animations
  []
  {:scale         (anim/use-val constants/min-scale)
   :saved-scale   (anim/use-val constants/min-scale)
   :pan-x-start   (anim/use-val constants/init-offset)
   :pan-x         (anim/use-val constants/init-offset)
   :pan-y-start   (anim/use-val constants/init-offset)
   :pan-y         (anim/use-val constants/init-offset)
   :pinch-x-start (anim/use-val constants/init-offset)
   :pinch-x       (anim/use-val constants/init-offset)
   :pinch-y-start (anim/use-val constants/init-offset)
   :pinch-y       (anim/use-val constants/init-offset)
   :pinch-x-max   (anim/use-val js/Infinity)
   :pinch-y-max   (anim/use-val js/Infinity)
   :rotate        (anim/use-val constants/init-rotation)
   :rotate-scale  (anim/use-val constants/min-scale)})

(defn init-state
  []
  {:pan-x-enabled? (reagent/atom false)
   :pan-y-enabled? (reagent/atom false)
   :focal-x        (reagent/atom nil)
   :focal-y        (reagent/atom nil)})
