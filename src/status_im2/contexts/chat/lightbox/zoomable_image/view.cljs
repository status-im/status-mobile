(ns status-im2.contexts.chat.lightbox.zoomable-image.view
  (:require
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [utils.re-frame :as rf]
    [oops.core :refer [oget]]
    [react-native.orientation :as orientation]
    [status-im2.contexts.chat.lightbox.zoomable-image.constants :as c]
    [status-im2.contexts.chat.lightbox.zoomable-image.style :as style]
    [status-im2.contexts.chat.lightbox.zoomable-image.utils :as utils]))

;;;; Some aliases for reanimated methods, as they are used 10s of times in this file
;; TODO: Abstract Reanimated methods in a better way, issue:
;; https://github.com/status-im/status-mobile/issues/15176
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
   (timing value c/default-duration))
  ([value duration]
   (if (= duration nil)
     value
     (reanimated/with-timing-duration value duration))))

(defn set-val-decay
  [animation velocity bounds]
  (reanimated/animate-shared-value-with-decay animation (* velocity c/velocity-factor) bounds))

;;;; Helpers
(defn center-x
  [{:keys [pinch-x pinch-x-start pan-x pan-x-start]} exit?]
  (let [duration (if exit? 100 c/default-duration)]
    (set-val pinch-x (timing c/init-offset duration))
    (set-val pinch-x-start c/init-offset)
    (set-val pan-x (timing c/init-offset duration))
    (set-val pan-x-start c/init-offset)))

(defn center-y
  [{:keys [pinch-y pinch-y-start pan-y pan-y-start]} exit?]
  (let [duration (if exit? 100 c/default-duration)]
    (set-val pinch-y (timing c/init-offset duration))
    (set-val pinch-y-start c/init-offset)
    (set-val pan-y (timing c/init-offset duration))
    (set-val pan-y-start c/init-offset)))

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
   {:keys [pan-x-enabled? pan-y-enabled?] :as props}]
  (set-val scale (timing value (if exit? 100 c/default-duration)))
  (set-val saved-scale value)
  (when (= value c/min-scale)
    (reset-values exit? animations props))
  (reset! pan-x-enabled? (> value x-threshold-scale))
  (reset! pan-y-enabled? (> value y-threshold-scale)))

(defn handle-orientation-change
  [curr-orientation
   focused?
   {:keys [landscape-scale-val x-threshold-scale y-threshold-scale]}
   {:keys [rotate rotate-scale scale] :as animations}
   {:keys [pan-x-enabled? pan-y-enabled?]}]
  (let [duration (when focused? c/default-duration)]
    (cond
      (= curr-orientation orientation/landscape-left)
      (do
        (set-val rotate (timing "90deg" duration))
        (set-val rotate-scale (timing landscape-scale-val duration)))
      (= curr-orientation orientation/landscape-right)
      (do
        (set-val rotate (timing "-90deg" duration))
        (set-val rotate-scale (timing landscape-scale-val duration)))
      (= curr-orientation orientation/portrait)
      (do
        (set-val rotate (timing c/init-rotation duration))
        (set-val rotate-scale (timing c/min-scale duration))))
    (center-x animations false)
    (center-y animations false)
    (reset! pan-x-enabled? (> (get-val scale) x-threshold-scale))
    (reset! pan-y-enabled? (> (get-val scale) y-threshold-scale))))

;;;; Gestures
(defn tap-gesture
  [on-tap]
  (->
    (gesture/gesture-tap)
    (gesture/on-start #(on-tap))))

(defn double-tap-gesture
  [{:keys [width height screen-width screen-height y-threshold-scale x-threshold-scale]}
   {:keys [scale pan-x pan-x-start pan-y pan-y-start]}
   rescale]
  (->
    (gesture/gesture-tap)
    (gesture/number-of-taps 2)
    (gesture/on-start
     (fn [e]
       (if (= (get-val scale) c/min-scale)
         (let [translate-x (utils/get-double-tap-offset width screen-width (oget e "x"))
               translate-y (utils/get-double-tap-offset height screen-height (oget e "y"))]
           (when (> c/double-tap-scale x-threshold-scale)
             (set-val pan-x (timing translate-x))
             (set-val pan-x-start translate-x))
           (when (> c/double-tap-scale y-threshold-scale)
             (set-val pan-y (timing translate-y))
             (set-val pan-y-start translate-y))
           (rescale c/double-tap-scale))
         (rescale c/min-scale))))))

(defn pinch-gesture
  [{:keys [width height screen-height screen-width x-threshold-scale y-threshold-scale]}
   {:keys [saved-scale scale pinch-x pinch-y pinch-x-start pinch-y-start pinch-x-max pinch-y-max pan-y
           pan-y-start pan-x pan-x-start]
    :as   animations}
   {:keys [pan-x-enabled? pan-y-enabled? focal-x focal-y]}
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
                               scale-diff  (utils/get-scale-diff new-scale (get-val saved-scale))
                               new-pinch-x (utils/get-pinch-position scale-diff width @focal-x)
                               new-pinch-y (utils/get-pinch-position scale-diff height @focal-y)]
                           (when (and (>= new-scale c/max-scale) (= (get-val pinch-x-max) js/Infinity))
                             (set-val pinch-x-max (get-val pinch-x))
                             (set-val pinch-y-max (get-val pinch-y)))
                           (set-val pinch-x (+ new-pinch-x (get-val pinch-x-start)))
                           (set-val pinch-y (+ new-pinch-y (get-val pinch-y-start)))
                           (set-val scale new-scale))))
    (gesture/on-end
     (fn []
       (cond
         (< (get-val scale) c/min-scale)
         (rescale c/min-scale)
         (> (get-val scale) c/max-scale)
         (do
           (set-val pinch-x (timing (get-val pinch-x-max)))
           (set-val pinch-x-start (get-val pinch-x-max))
           (set-val pinch-x-max js/Infinity)
           (set-val pinch-y (timing (get-val pinch-y-max)))
           (set-val pinch-y-start (get-val pinch-y-max))
           (set-val pinch-y-max js/Infinity)
           (set-val scale (timing c/max-scale))
           (set-val saved-scale c/max-scale))
         :else
         (do
           (set-val saved-scale (get-val scale))
           (set-val pinch-x-start (get-val pinch-x))
           (set-val pinch-y-start (get-val pinch-y))
           (when (< (get-val scale) x-threshold-scale)
             (center-x animations false))
           (when (< (get-val scale) y-threshold-scale)
             (center-y animations false))))))
    (gesture/on-finalize
     (fn []
       (let [curr-offset-y (+ (get-val pan-y) (get-val pinch-y))
             max-offset-y  (utils/get-max-offset height screen-height (get-val scale))
             max-offset-y  (if (neg? curr-offset-y) (- max-offset-y) max-offset-y)
             curr-offset-x (+ (get-val pan-x) (get-val pinch-x))
             max-offset-x  (utils/get-max-offset width screen-width (get-val scale))
             max-offset-x  (if (neg? curr-offset-x) (- max-offset-x) max-offset-x)]
         (when (and (> (get-val scale) y-threshold-scale)
                    (> (Math/abs curr-offset-y) (Math/abs max-offset-y)))
           (set-val pinch-y (timing c/init-offset))
           (set-val pinch-y-start c/init-offset)
           (set-val pan-y (timing max-offset-y))
           (set-val pan-y-start max-offset-y))
         (when (and (> (get-val scale) x-threshold-scale)
                    (> (Math/abs curr-offset-x) (Math/abs max-offset-x)))
           (set-val pinch-x (timing c/init-offset))
           (set-val pinch-x-start c/init-offset)
           (set-val pan-x (timing max-offset-x))
           (set-val pan-x-start max-offset-x))
         (reset! pan-x-enabled? (> (get-val scale) x-threshold-scale))
         (reset! pan-y-enabled? (> (get-val scale) y-threshold-scale)))))))

(defn pan-x-gesture
  [{:keys [width screen-width x-threshold-scale]}
   {:keys [scale pan-x-start pan-x pinch-x pinch-x-start]}
   {:keys [pan-x-enabled?]}
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
             max-offset  (utils/get-max-offset width screen-width (get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)]
         (cond
           (< (get-val scale) x-threshold-scale)
           (rescale c/min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (set-val pan-x (timing max-offset))
             (set-val pan-x-start max-offset)
             (set-val pinch-x (timing c/init-offset))
             (set-val pinch-x-start c/init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (get-val pinch-x-start))
                 upper-bound (- (Math/abs max-offset) (get-val pinch-x-start))]
             (set-val pan-x-start (get-val pan-x))
             (set-val-decay pan-x (oget e "velocityX") [lower-bound upper-bound])
             (set-val-decay pan-x-start (oget e "velocityX") [lower-bound upper-bound]))))))))


(defn pan-y-gesture
  [{:keys [height screen-height y-threshold-scale]}
   {:keys [scale pan-y-start pan-y pinch-y pinch-y-start]}
   {:keys [pan-y-enabled?]}
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
             max-offset  (utils/get-max-offset height screen-height (get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)]
         (cond
           (< (get-val scale) y-threshold-scale)
           (rescale c/min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (set-val pan-y (timing max-offset))
             (set-val pan-y-start max-offset)
             (set-val pinch-y (timing c/init-offset))
             (set-val pinch-y-start c/init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (get-val pinch-y-start))
                 upper-bound (- (Math/abs max-offset) (get-val pinch-y-start))]
             (set-val pan-y-start (get-val pan-y))
             (set-val-decay pan-y (oget e "velocityY") [lower-bound upper-bound])
             (set-val-decay pan-y-start (oget e "velocityY") [lower-bound upper-bound]))))))))


;;;; Finally, the component
(defn zoomable-image
  [{:keys [image-width image-height content message-id]} index border-radius on-tap]
  [:f>
   (fn []
     (let [shared-element-id    (rf/sub [:shared-element-id])
           exit-lightbox-signal (rf/sub [:lightbox/exit-signal])
           zoom-out-signal      (rf/sub [:lightbox/zoom-out-signal])
           curr-orientation     (or (rf/sub [:lightbox/orientation]) orientation/portrait)
           focused?             (= shared-element-id message-id)
           dimensions           (utils/get-dimensions image-width image-height curr-orientation)
           animations           {:scale         (use-val c/min-scale)
                                 :saved-scale   (use-val c/min-scale)
                                 :pan-x-start   (use-val c/init-offset)
                                 :pan-x         (use-val c/init-offset)
                                 :pan-y-start   (use-val c/init-offset)
                                 :pan-y         (use-val c/init-offset)
                                 :pinch-x-start (use-val c/init-offset)
                                 :pinch-x       (use-val c/init-offset)
                                 :pinch-y-start (use-val c/init-offset)
                                 :pinch-y       (use-val c/init-offset)
                                 :pinch-x-max   (use-val js/Infinity)
                                 :pinch-y-max   (use-val js/Infinity)
                                 :rotate        (use-val c/init-rotation)
                                 :rotate-scale  (use-val c/min-scale)}
           props                {:pan-x-enabled? (reagent/atom false)
                                 :pan-y-enabled? (reagent/atom false)
                                 :focal-x        (reagent/atom nil)
                                 :focal-y        (reagent/atom nil)}
           rescale              (fn [value exit?]
                                  (rescale-image value exit? dimensions animations props))]
       (when platform/ios?
         (handle-orientation-change curr-orientation focused? dimensions animations props)
         (utils/handle-exit-lightbox-signal exit-lightbox-signal
                                            index
                                            (get-val (:scale animations))
                                            rescale))
       (utils/handle-zoom-out-signal zoom-out-signal index (get-val (:scale animations)) rescale)
       [:f>
        (fn []
          (let [tap               (tap-gesture on-tap)
                double-tap        (double-tap-gesture dimensions animations rescale)
                pinch             (pinch-gesture dimensions animations props rescale)
                pan-x             (pan-x-gesture dimensions animations props rescale)
                pan-y             (pan-y-gesture dimensions animations props rescale)
                composed-gestures (gesture/exclusive
                                   (gesture/simultaneous pinch pan-x pan-y)
                                   (gesture/exclusive double-tap tap))]
            [gesture/gesture-detector {:gesture composed-gestures}
             [reanimated/view
              {:style (style/container dimensions animations)}
              [reanimated/fast-image
               {:source    {:uri (:image content)}
                :native-ID (when focused? :shared-element)
                :style     (style/image dimensions animations border-radius)}]]]))]))])
