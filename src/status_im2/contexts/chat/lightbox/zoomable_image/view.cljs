(ns status-im2.contexts.chat.lightbox.zoomable-image.view
  (:require
   [react-native.core :as rn]
   [react-native.gesture :as gesture]
   [react-native.reanimated :as reanimated]
   [reagent.core :as reagent]
   [utils.re-frame :as rf]))

;; Definitions
(def min-scale 1)

(def double-tap-scale 2)

(def max-scale 5)

(def init-offset 0)

(def velocity-factor 0.25)

(def default-duration 300)

;; Some aliases for reanimated methods, as they are used 10s of times in this file
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
  [animation value bounds]
  (reanimated/animate-shared-value-with-decay animation value bounds))

;; MATH
(defn get-max-offset
  [size screen-size scale]
  (/ (/ (- (* size scale) screen-size) 2) scale))

(defn get-scale-ratio
  [new-scale saved-scale]
  (/ (- (dec new-scale) (dec saved-scale)) new-scale))

(defn get-current-center
  [size scaled-size offset]
  (- (+ (/ (- size scaled-size) 2) (/ scaled-size 2)) offset))

;; 5 Gestures: tap, double-tap, pinch, pan-x, pan-y
(defn tap-gesture
  [on-tap]
  (->
    (.Tap gesture/gesture)
    (.onStart #(on-tap))))

(defn double-tap-gesture
  [{:keys [width height screen-height]}
   {:keys [scale pan-x pan-x-start pan-y pan-y-start]}
   {:keys [y-threshold-scale]}
   rescale]
  (->
    (.Tap gesture/gesture)
    (.numberOfTaps 2)
    (.onStart (fn [evt]
                ;; Scale to x2
                (if (= (get-val scale) min-scale)
                  ;; Find translate-x value
                  (let [focal-x     (- (/ width 2) (.-x evt))
                        max-pan-x   (get-max-offset width width double-tap-scale)
                        translate-x (min (Math/abs focal-x) max-pan-x)
                        translate-x (if (neg? focal-x) (- translate-x) translate-x)
                        ;; Find translate-y value
                        focal-y     (- (/ height 2) (.-y evt))
                        max-pan-y   (get-max-offset height screen-height double-tap-scale)
                        translate-y (min (Math/abs focal-y) max-pan-y)
                        translate-y (if (neg? focal-y) (- translate-y) translate-y)]
                    ;; apply animations
                    (set-val pan-x (timing translate-x))
                    (set-val pan-x-start (timing translate-x))
                    ;; animate y position only if the double-tap-scale exceeds the threshold
                    (when (> double-tap-scale y-threshold-scale)
                      (set-val pan-y (timing translate-y))
                      (set-val pan-y-start (timing translate-y)))
                    (rescale double-tap-scale))
                  ;; Otherwise scale back to x1
                  (rescale min-scale))))))

(defn pinch-gesture
  [{:keys [width height]}
   {:keys [saved-scale scale pinch-x pinch-y pinch-x-start pinch-y-start pan-x-start pan-y-start]}
   {:keys [pan-x-enabled? pan-y-enabled? x-threshold-scale y-threshold-scale focal-x focal-y]}
   rescale]
  (->
    (.Pinch gesture/gesture)
    (.onStart (fn [evt]
                ;; Read the x and y touch positions on the screen
                (reset! focal-x (.-focalX evt))
                (reset! focal-y (.-focalY evt))))
    (.onUpdate (fn [evt]
                 ;; First, find the scale ration
                 (let [new-scale     (* (.-scale evt) (get-val saved-scale))
                       scale-ratio   (get-scale-ratio new-scale (get-val saved-scale))
                       ;; Then, find the translate-x value (pinch-x)
                       scaled-width  (/ width (get-val saved-scale))
                       x-start       (+ (get-val pinch-x-start) (get-val pan-x-start))
                       center-x      (get-current-center width scaled-width x-start)
                       new-pinch-x   (* (- center-x @focal-x) scale-ratio)
                       ;; Lastly, find the translate-y value (pinch-y)
                       scaled-height (/ height (get-val scale))
                       y-start       (+ (get-val pinch-y-start) (get-val pan-y-start))
                       center-y      (get-current-center height scaled-height y-start)
                       new-pinch-y   (* (- center-y @focal-y) scale-ratio)]
                   ;; Update the values
                   (set-val pinch-x (+ new-pinch-x (get-val pinch-x-start)))
                   (set-val pinch-y (+ new-pinch-y (get-val pinch-y-start)))
                   (set-val scale new-scale))))
    (.onEnd
     (fn []
       (cond
         ;; if less than min-scale, then scale back to x1
         (< (get-val scale) min-scale)
         (rescale min-scale)
         ;; if greater than max-scale, then scale back to max-scale
         (> (get-val scale) max-scale)
         (do
           (set-val pinch-x-start (get-val pinch-x))
           (set-val pinch-y-start (get-val pinch-y))
           (rescale max-scale))
         ;; Otherwise, apply animations
         :else
         (do
           (set-val saved-scale (get-val scale))
           (set-val pinch-x-start (get-val pinch-x))
           (set-val pinch-y-start (get-val pinch-y))
           ;; Enable panning if the scale is bigger than the corresponding threshold
           (reset! pan-x-enabled? (> (get-val scale) x-threshold-scale))
           (reset! pan-y-enabled? (> (get-val scale) y-threshold-scale))))))))

(defn pan-x-gesture
  [{:keys [width]}
   {:keys [scale pan-x-start pan-x pinch-x pinch-x-start]}
   {:keys [pan-x-enabled?]}]
  (->
    (.Pan gesture/gesture)
    (.enabled @pan-x-enabled?)
    (.averageTouches true)
    (.onUpdate (fn [evt]
                 (let [scaled-translation (/ (.-translationX evt) (get-val scale))
                       updated-value      (+ scaled-translation (get-val pan-x-start))]
                   (set-val pan-x updated-value))))
    (.onEnd
     (fn [evt]
       (let [curr-offset (+ (get-val pan-x) (get-val pinch-x))
             max-offset  (get-max-offset width width (get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)]
         (if (> (Math/abs curr-offset) (Math/abs max-offset))
           ;; Snap the image back to its edge if the translation is beyond its edge
           (do
             (set-val pan-x (timing max-offset))
             (set-val pan-x-start (timing max-offset))
             (set-val pinch-x (timing init-offset))
             (set-val pinch-x-start (timing init-offset)))
           ;; Otherwise, apply animations
           (let [lower-bound (- (- (Math/abs max-offset)) (get-val pinch-x))
                 upper-bound (- (Math/abs max-offset) (get-val pinch-x))]
             (set-val pan-x-start (get-val pan-x))
             ;; Apply decaying animation
             (set-val-decay pan-x (* (.-velocityX evt) velocity-factor) [lower-bound upper-bound])
             (set-val-decay pan-x-start
                            (* (.-velocityX evt) velocity-factor)
                            [lower-bound upper-bound]))))))))


(defn pan-y-gesture
  [{:keys [height screen-height]}
   {:keys [scale pan-y-start pan-y pinch-y pinch-y-start]}
   {:keys [pan-y-enabled? y-threshold-scale]}
   rescale]
  (->
    (.Pan gesture/gesture)
    (.enabled @pan-y-enabled?)
    (.averageTouches true)
    (.onUpdate (fn [evt]
                 (let [scaled-translation (/ (.-translationY evt) (get-val scale))
                       updated-value      (+ scaled-translation (get-val pan-y-start))]
                   (set-val pan-y updated-value))))
    (.onEnd
     (fn [evt]
       (let [curr-offset (+ (get-val pan-y) (get-val pinch-y))
             max-offset  (get-max-offset height screen-height (get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)]
         (cond
           ;; On-end, if scale is less than min-scale, then scale back to x1
           (< (get-val scale) y-threshold-scale)
           (rescale min-scale)
           ;; Snap the image back to its edge if the translation is beyond its edge
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (set-val pan-y (timing max-offset))
             (set-val pan-y-start (timing max-offset))
             (set-val pinch-y (timing init-offset))
             (set-val pinch-y-start (timing init-offset)))
           ;; Otherwise, apply animations
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (get-val pinch-y))
                 upper-bound (- (Math/abs max-offset) (get-val pinch-y))]
             (set-val pan-y-start (get-val pan-y))
             ;; Apply decaying animation
             (set-val-decay pan-y (* (.-velocityY evt) velocity-factor) [lower-bound upper-bound])
             (set-val-decay pan-y-start
                            (* (.-velocityY evt) velocity-factor)
                            [lower-bound upper-bound]))))))))

;; A helper method to rescale and reset values
(defn rescale-image
  [value
   exit?
   {:keys [scale saved-scale pan-x pan-x-start pan-y pan-y-start pinch-x pinch-y pinch-x-start
           pinch-y-start]}
   {:keys [pan-x-enabled? pan-y-enabled? x-threshold-scale y-threshold-scale focal-x focal-y]}]
  (let [duration (if exit? 100 default-duration)]
    (set-val scale (timing value duration))
    (set-val saved-scale (timing value duration))
    (when (= value min-scale)
      (set-val pan-x (timing init-offset duration))
      (set-val pinch-x (timing init-offset duration))
      (set-val pan-x-start (timing init-offset duration))
      (set-val pinch-x-start (timing init-offset duration))
      (set-val pan-y (timing init-offset duration))
      (set-val pinch-y (timing init-offset duration))
      (set-val pinch-y-start (timing init-offset duration))
      (set-val pan-y-start (timing init-offset duration))
      (reset! focal-x nil)
      (reset! focal-y nil))
    (reset! pan-x-enabled? (> value x-threshold-scale))
    (reset! pan-y-enabled? (> value y-threshold-scale))))

;; On ios, when attempting to navigate back while zoomed in, the shared-element transition
;; animation doesn't execute properly, so we need to zoom out first
(defn handle-close-lightbox
  [close-lightbox index scale rescale]
  (when (= close-lightbox index)
    (if (> scale min-scale)
      (do
        (rescale min-scale true)
        (js/setTimeout #(rf/dispatch [:navigate-back]) 70))
      (rf/dispatch [:navigate-back]))
    (js/setTimeout #(rf/dispatch [:chat.ui/close-lightbox nil]) 500)))

;; Finally, the component
(defn zoomable-image
  [{:keys [image-width image-height content message-id]} index border-value on-tap]
  [:f>
   (fn []
     (let [shared-element-id (rf/sub [:shared-element-id])
           close-lightbox    (rf/sub [:close-lightbox])
           width             (:width (rn/get-window))
           height            (* image-height (/ (:width (rn/get-window)) image-width))
           screen-height     (:height (rn/get-window))
           dimensions        {:width         width
                              :height        height
                              :screen-height screen-height}
           animations        {:scale         (use-val min-scale)
                              :saved-scale   (use-val min-scale)
                              :pan-x-start   (use-val init-offset)
                              :pan-x         (use-val init-offset)
                              :pan-y-start   (use-val init-offset)
                              :pan-y         (use-val init-offset)
                              :pinch-x-start (use-val init-offset)
                              :pinch-x       (use-val init-offset)
                              :pinch-y-start (use-val init-offset)
                              :pinch-y       (use-val init-offset)}
           props             {:x-threshold-scale 1
                              :y-threshold-scale (/ screen-height (min screen-height height))
                              :pan-x-enabled?    (reagent/atom false)
                              :pan-y-enabled?    (reagent/atom false)
                              :focal-x           (reagent/atom nil)
                              :focal-y           (reagent/atom nil)}
           rescale           (fn [value duration]
                               (rescale-image value duration animations props))]
       (handle-close-lightbox close-lightbox index (get-val (:scale animations)) rescale)
       [:f>
        (fn []
          (let [tap               (tap-gesture on-tap)
                double-tap        (double-tap-gesture dimensions animations props rescale)
                pinch             (pinch-gesture dimensions animations props rescale)
                pan-x             (pan-x-gesture dimensions animations props)
                pan-y             (pan-y-gesture dimensions animations props rescale)
                composed-gestures (.Exclusive gesture/gesture
                                              (.Simultaneous gesture/gesture pinch pan-x pan-y)
                                              (.Exclusive gesture/gesture double-tap tap))]
            [gesture/gesture-detector {:gesture composed-gestures}
             [reanimated/fast-image
              {:source    {:uri (:image content)}
               :native-ID (when (= shared-element-id message-id) :shared-element)
               :style     (reanimated/apply-animations-to-style
                           {:transform     [{:scale (:scale animations)}
                                            {:translateX (:pan-x animations)}
                                            {:translateY (:pan-y animations)}
                                            {:translateX (:pinch-x animations)}
                                            {:translateY (:pinch-y animations)}]
                            :border-radius border-value}
                           {:width  width
                            :height height})}]]))]))])
