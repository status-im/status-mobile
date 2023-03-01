(ns status-im2.contexts.chat.lightbox.zoomable-image.view
  (:require
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [utils.re-frame :as rf]
    [oops.core :refer [oget]]))

;;;; Definitions
(def min-scale 1)

(def double-tap-scale 2)

(def max-scale 5)

(def init-offset 0)

(def velocity-factor 0.5)

(def default-duration 300)

;;;; Some aliases for reanimated methods, as they are used 10s of times in this file
(defn get-val
  [animation]
  (reanimated/get-shared-value animation))

(defn set-val
  [animation value]
  (reanimated/set-shared-value animation value))

(defn use-val
  [value]
  (reanimated/use-shared-value value))

(defn timing
  ([value]
   (timing value default-duration))
  ([value duration]
   (reanimated/with-timing-duration value duration)))

(defn set-val-decay
  [animation velocity bounds]
  (reanimated/animate-shared-value-with-decay animation (* velocity velocity-factor) bounds))

;;;; MATH
(defn get-max-offset
  [size screen-size scale]
  (/ (- (* size (min scale max-scale))
        screen-size)
     2))

(defn get-scale-diff
  [new-scale saved-scale]
  (- (dec new-scale)
     (dec saved-scale)))

(defn get-double-tap-offset
  [size screen-size focal]
  (let [center        (/ size 2)
        target-point  (* (- center focal) double-tap-scale)
        max-offset    (get-max-offset size screen-size double-tap-scale)
        translate-val (min (Math/abs target-point) max-offset)]
    (if (neg? target-point) (- translate-val) translate-val)))

(defn get-pinch-position
  [scale-diff size focal]
  (* (- (/ size 2) focal) scale-diff))


;;;; 5 Gestures: tap, double-tap, pinch, pan-x, pan-y
(defn tap-gesture
  [on-tap]
  (->
    (gesture/gesture-tap)
    (gesture/on-start #(on-tap))))

(defn double-tap-gesture
  [{:keys [width height screen-height]}
   {:keys [scale pan-x pan-x-start pan-y pan-y-start]}
   {:keys [y-threshold-scale x-threshold-scale]}
   rescale]
  (->
    (gesture/gesture-tap)
    (gesture/number-of-taps 2)
    (gesture/on-start (fn [e]
                        (if (= (get-val scale) min-scale)
                          (let [translate-x (get-double-tap-offset width width (oget e "x"))
                                translate-y (get-double-tap-offset height screen-height (oget e "y"))]
                            (when (> double-tap-scale x-threshold-scale)
                              (set-val pan-x (timing translate-x))
                              (set-val pan-x-start translate-x))
                            (when (> double-tap-scale y-threshold-scale)
                              (set-val pan-y (timing translate-y))
                              (set-val pan-y-start translate-y))
                            (rescale double-tap-scale))
                          (rescale min-scale))))))

(defn pinch-gesture
  [{:keys [width height]}
   {:keys [saved-scale scale pinch-x pinch-y pinch-x-start pinch-y-start pinch-x-max pinch-y-max]}
   {:keys [pan-x-enabled? pan-y-enabled? x-threshold-scale y-threshold-scale focal-x focal-y]}
   rescale]
  (->
    (gesture/gesture-pinch)
    (gesture/on-begin (fn [e]
                        (when platform/ios?
                          (reset! focal-x (oget e "focalX"))
                          (reset! focal-y (oget e "focalY")))))
    (gesture/on-start (fn [e]
                        (when platform/android?
                          (reset! focal-x (oget e "focalX"))
                          (reset! focal-y (oget e "focalY")))))
    (gesture/on-update (fn [e]
                         (let [new-scale   (* (oget e "scale") (get-val saved-scale))
                               scale-diff  (get-scale-diff new-scale (get-val saved-scale))
                               new-pinch-x (get-pinch-position scale-diff width @focal-x)
                               new-pinch-y (get-pinch-position scale-diff height @focal-y)]
                           (when (and (>= new-scale max-scale) (= (get-val pinch-x-max) js/Infinity))
                             (set-val pinch-x-max (get-val pinch-x))
                             (set-val pinch-y-max (get-val pinch-y)))
                           (set-val pinch-x (+ new-pinch-x (get-val pinch-x-start)))
                           (set-val pinch-y (+ new-pinch-y (get-val pinch-y-start)))
                           (set-val scale new-scale))))
    (gesture/on-end
     (fn []
       (cond
         (< (get-val scale) min-scale)
         (rescale min-scale)
         (> (get-val scale) max-scale)
         (do
           (set-val pinch-x (timing (get-val pinch-x-max)))
           (set-val pinch-x-start (get-val pinch-x-max))
           (set-val pinch-x-max js/Infinity)
           (set-val pinch-y (timing (get-val pinch-y-max)))
           (set-val pinch-y-start (get-val pinch-y-max))
           (set-val pinch-y-max js/Infinity)
           (set-val scale (timing max-scale))
           (set-val saved-scale max-scale)
           (reset! pan-x-enabled? (> (get-val scale) x-threshold-scale))
           (reset! pan-y-enabled? (> (get-val scale) y-threshold-scale)))
         :else
         (do
           (set-val saved-scale (get-val scale))
           (set-val pinch-x-start (get-val pinch-x))
           (set-val pinch-y-start (get-val pinch-y))
           (reset! pan-x-enabled? (> (get-val scale) x-threshold-scale))
           (reset! pan-y-enabled? (> (get-val scale) y-threshold-scale))))))))

(defn pan-x-gesture
  [{:keys [width]}
   {:keys [scale pan-x-start pan-x pinch-x pinch-x-start]}
   {:keys [pan-x-enabled? x-threshold-scale]}
   rescale]
  (->
    (gesture/gesture-pan)
    (gesture/enabled @pan-x-enabled?)
    (gesture/average-touches false)
    (gesture/on-update (fn [e]
                         (set-val pan-x (+ (get-val pan-x-start) (oget e "translationX")))))
    (gesture/on-end
     (fn [e]
       (let [curr-offset (+ (get-val pan-x) (get-val pinch-x-start))
             max-offset  (get-max-offset width width (get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)]
         (cond
           (< (get-val scale) x-threshold-scale)
           (rescale min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (set-val pan-x (timing max-offset))
             (set-val pan-x-start max-offset)
             (set-val pinch-x (timing init-offset))
             (set-val pinch-x-start init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (get-val pinch-x-start))
                 upper-bound (- (Math/abs max-offset) (get-val pinch-x-start))]
             (set-val pan-x-start (get-val pan-x))
             (set-val-decay pan-x (oget e "velocityX") [lower-bound upper-bound])
             (set-val-decay pan-x-start (oget e "velocityX") [lower-bound upper-bound]))))))))


(defn pan-y-gesture
  [{:keys [height screen-height]}
   {:keys [scale pan-y-start pan-y pinch-y pinch-y-start]}
   {:keys [pan-y-enabled? y-threshold-scale]}
   rescale]
  (->
    (gesture/gesture-pan)
    (gesture/enabled @pan-y-enabled?)
    (gesture/average-touches false)
    (gesture/on-update (fn [e]
                         (set-val pan-y (+ (get-val pan-y-start) (oget e "translationY")))))
    (gesture/on-end
     (fn [e]
       (let [curr-offset (+ (get-val pan-y) (get-val pinch-y-start))
             max-offset  (get-max-offset height screen-height (get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)]
         (cond
           (< (get-val scale) y-threshold-scale)
           (rescale min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (set-val pan-y (timing max-offset))
             (set-val pan-y-start max-offset)
             (set-val pinch-y (timing init-offset))
             (set-val pinch-y-start init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (get-val pinch-y-start))
                 upper-bound (- (Math/abs max-offset) (get-val pinch-y-start))]
             (set-val pan-y-start (get-val pan-y))
             (set-val-decay pan-y (oget e "velocityY") [lower-bound upper-bound])
             (set-val-decay pan-y-start (oget e "velocityY") [lower-bound upper-bound]))))))))

(defn reset-values
  [exit?
   {:keys [pan-x pan-x-start pan-y pan-y-start pinch-x pinch-y pinch-x-start pinch-y-start]}
   {:keys [focal-x focal-y]}]
  (let [duration (if exit? 100 default-duration)]
    (set-val pan-x (timing init-offset duration))
    (set-val pinch-x (timing init-offset duration))
    (set-val pan-x-start init-offset)
    (set-val pinch-x-start init-offset)
    (set-val pan-y (timing init-offset duration))
    (set-val pinch-y (timing init-offset duration))
    (set-val pinch-y-start init-offset)
    (set-val pan-y-start init-offset)
    (reset! focal-x nil)
    (reset! focal-y nil)))

(defn rescale-image
  [value
   exit?
   {:keys [scale saved-scale] :as animations}
   {:keys [pan-x-enabled? pan-y-enabled? x-threshold-scale y-threshold-scale] :as props}]
  (set-val scale (timing value (if exit? 100 300)))
  (set-val saved-scale value)
  (when (= value min-scale)
    (reset-values exit? animations props))
  (reset! pan-x-enabled? (> value x-threshold-scale))
  (reset! pan-y-enabled? (> value y-threshold-scale)))

;;; On ios, when attempting to navigate back while zoomed in, the shared-element transition
;;; animation doesn't execute properly, so we need to zoom out first
(defn handle-exit-lightbox-signal
  [exit-lightbox-signal index scale rescale]
  (when (= exit-lightbox-signal index)
    (if (> scale min-scale)
      (do
        (rescale min-scale true)
        (js/setTimeout #(rf/dispatch [:navigate-back]) 70))
      (rf/dispatch [:navigate-back]))
    (js/setTimeout #(rf/dispatch [:chat.ui/exit-lightbox-signal nil]) 500)))

(defn handle-zoom-out-signal
  [zoom-out-signal index scale rescale]
  (when (and (= zoom-out-signal index) (> scale min-scale))
    (rescale min-scale true)))

;;;; Finally, the component
(defn zoomable-image
  [{:keys [image-width image-height content message-id]} index border-value on-tap]
  [:f>
   (fn []
     (let [shared-element-id    (rf/sub [:shared-element-id])
           exit-lightbox-signal (rf/sub [:lightbox/exit-signal])
           zoom-out-signal      (rf/sub [:lightbox/zoom-out-signal])
           width                (:width (rn/get-window))
           height               (* image-height (/ (:width (rn/get-window)) image-width))
           screen-height        (:height (rn/get-window))
           dimensions           {:width         width
                                 :height        height
                                 :screen-height screen-height}
           animations           {:scale         (use-val min-scale)
                                 :saved-scale   (use-val min-scale)
                                 :pan-x-start   (use-val init-offset)
                                 :pan-x         (use-val init-offset)
                                 :pan-y-start   (use-val init-offset)
                                 :pan-y         (use-val init-offset)
                                 :pinch-x-start (use-val init-offset)
                                 :pinch-x       (use-val init-offset)
                                 :pinch-y-start (use-val init-offset)
                                 :pinch-y       (use-val init-offset)
                                 :pinch-x-max   (use-val js/Infinity)
                                 :pinch-y-max   (use-val js/Infinity)}
           props                {:x-threshold-scale 1
                                 :y-threshold-scale (/ screen-height (min screen-height height))
                                 :pan-x-enabled?    (reagent/atom false)
                                 :pan-y-enabled?    (reagent/atom false)
                                 :focal-x           (reagent/atom nil)
                                 :focal-y           (reagent/atom nil)}
           rescale              (fn [value exit?]
                                  (rescale-image value exit? animations props))]
       (handle-exit-lightbox-signal exit-lightbox-signal index (get-val (:scale animations)) rescale)
       (handle-zoom-out-signal zoom-out-signal index (get-val (:scale animations)) rescale)
       [:f>
        (fn []
          (let [tap               (tap-gesture on-tap)
                double-tap        (double-tap-gesture dimensions animations props rescale)
                pinch             (pinch-gesture dimensions animations props rescale)
                pan-x             (pan-x-gesture dimensions animations props rescale)
                pan-y             (pan-y-gesture dimensions animations props rescale)
                composed-gestures (gesture/exclusive
                                   (gesture/simultaneous pinch pan-x pan-y)
                                   (gesture/exclusive double-tap tap))]
            [gesture/gesture-detector {:gesture composed-gestures}
             [reanimated/fast-image
              {:source    {:uri (:image content)}
               :native-ID (when (= shared-element-id message-id) :shared-element)
               :style     (reanimated/apply-animations-to-style
                           {:transform     [{:translateX (:pan-x animations)}
                                            {:translateY (:pan-y animations)}
                                            {:translateX (:pinch-x animations)}
                                            {:translateY (:pinch-y animations)}
                                            {:scale (:scale animations)}]
                            :border-radius border-value}
                           {:width  width
                            :height height})}]]))]))])

