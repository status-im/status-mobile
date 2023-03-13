(ns status-im2.contexts.chat.lightbox.zoomable-image.view
  (:require
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [utils.re-frame :as rf]
    [oops.core :refer [oget]]
    [react-native.orientation :as orientation]
    [status-im2.contexts.chat.lightbox.animations :as anim]
    [status-im2.contexts.chat.lightbox.zoomable-image.constants :as c]
    [status-im2.contexts.chat.lightbox.zoomable-image.style :as style]
    [status-im2.contexts.chat.lightbox.zoomable-image.utils :as utils]))

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
       (if (= (anim/get-val scale) c/min-scale)
         (let [translate-x (utils/get-double-tap-offset width screen-width (oget e "x"))
               translate-y (utils/get-double-tap-offset height screen-height (oget e "y"))]
           (when (> c/double-tap-scale x-threshold-scale)
             (anim/animate pan-x translate-x)
             (anim/set-val pan-x-start translate-x))
           (when (> c/double-tap-scale y-threshold-scale)
             (anim/animate pan-y translate-y)
             (anim/set-val pan-y-start translate-y))
           (rescale c/double-tap-scale))
         (rescale c/min-scale))))))

;; not using on-finalize because on-finalize gets called always regardless the gesture executed or not
(defn finalize-pinch
  [{:keys [width height screen-height screen-width x-threshold-scale y-threshold-scale]}
   {:keys [scale pinch-x pinch-y pinch-x-start pinch-y-start pan-y pan-y-start pan-x
           pan-x-start]}
   {:keys [pan-x-enabled? pan-y-enabled?]}]
  (let [curr-offset-y (+ (anim/get-val pan-y) (anim/get-val pinch-y))
        max-offset-y  (utils/get-max-offset height screen-height (anim/get-val scale))
        max-offset-y  (if (neg? curr-offset-y) (- max-offset-y) max-offset-y)
        curr-offset-x (+ (anim/get-val pan-x) (anim/get-val pinch-x))
        max-offset-x  (utils/get-max-offset width screen-width (anim/get-val scale))
        max-offset-x  (if (neg? curr-offset-x) (- max-offset-x) max-offset-x)]
    (when (and (> (anim/get-val scale) y-threshold-scale)
               (< (anim/get-val scale) c/max-scale)
               (> (Math/abs curr-offset-y) (Math/abs max-offset-y)))
      (anim/animate pinch-y c/init-offset)
      (anim/set-val pinch-y-start c/init-offset)
      (anim/animate pan-y max-offset-y)
      (anim/set-val pan-y-start max-offset-y))
    (when (and (> (anim/get-val scale) x-threshold-scale)
               (< (anim/get-val scale) c/max-scale)
               (> (Math/abs curr-offset-x) (Math/abs max-offset-x)))
      (anim/animate pinch-x c/init-offset)
      (anim/set-val pinch-x-start c/init-offset)
      (anim/animate pan-x max-offset-x)
      (anim/set-val pan-x-start max-offset-x))
    (reset! pan-x-enabled? (> (anim/get-val scale) x-threshold-scale))
    (reset! pan-y-enabled? (> (anim/get-val scale) y-threshold-scale))))

(defn pinch-gesture
  [{:keys [width height screen-height screen-width x-threshold-scale y-threshold-scale] :as dimensions}
   {:keys [saved-scale scale pinch-x pinch-y pinch-x-start pinch-y-start pinch-x-max pinch-y-max]
    :as   animations}
   {:keys [focal-x focal-y] :as props}
   rescale]
  (->
    (gesture/gesture-pinch)
    (gesture/on-begin (fn [e]
                        (when platform/ios?
                          (reset! focal-x (oget e "focalX"))
                          (reset! focal-y (utils/get-focal (oget e "focalY") height screen-height)))))
    (gesture/on-start (fn [e]
                        (when platform/android?
                          (reset! focal-x (utils/get-focal (oget e "focalX") width screen-width))
                          (reset! focal-y (utils/get-focal (oget e "focalY") height screen-height)))))
    (gesture/on-update (fn [e]
                         (let [new-scale   (* (oget e "scale") (anim/get-val saved-scale))
                               scale-diff  (utils/get-scale-diff new-scale (anim/get-val saved-scale))
                               new-pinch-x (utils/get-pinch-position scale-diff screen-width @focal-x)
                               new-pinch-y (utils/get-pinch-position scale-diff screen-height @focal-y)]
                           (when (and (>= new-scale c/max-scale)
                                      (= (anim/get-val pinch-x-max) js/Infinity))
                             (anim/set-val pinch-x-max (anim/get-val pinch-x))
                             (anim/set-val pinch-y-max (anim/get-val pinch-y)))
                           (anim/set-val pinch-x (+ new-pinch-x (anim/get-val pinch-x-start)))
                           (anim/set-val pinch-y (+ new-pinch-y (anim/get-val pinch-y-start)))
                           (anim/set-val scale new-scale))))
    (gesture/on-end
     (fn []
       (cond
         (< (anim/get-val scale) c/min-scale)
         (rescale c/min-scale)
         (> (anim/get-val scale) c/max-scale)
         (do
           (anim/animate pinch-x (anim/get-val pinch-x-max))
           (anim/set-val pinch-x-start (anim/get-val pinch-x-max))
           (anim/set-val pinch-x-max js/Infinity)
           (anim/animate pinch-y (anim/get-val pinch-y-max))
           (anim/set-val pinch-y-start (anim/get-val pinch-y-max))
           (anim/set-val pinch-y-max js/Infinity)
           (anim/animate scale c/max-scale)
           (anim/set-val saved-scale c/max-scale))
         :else
         (do
           (anim/set-val saved-scale (anim/get-val scale))
           (anim/set-val pinch-x-start (anim/get-val pinch-x))
           (anim/set-val pinch-y-start (anim/get-val pinch-y))
           (when (< (anim/get-val scale) x-threshold-scale)
             (utils/center-x animations false))
           (when (< (anim/get-val scale) y-threshold-scale)
             (utils/center-y animations false))))
       (finalize-pinch dimensions animations props)))))

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
                         (anim/set-val pan-x (+ (anim/get-val pan-x-start) (oget e "translationX")))))
    (gesture/on-end
     (fn [e]
       (let [curr-offset (+ (anim/get-val pan-x) (anim/get-val pinch-x-start))
             max-offset  (utils/get-max-offset width screen-width (anim/get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)
             velocity    (* (oget e "velocityX") c/velocity-factor)]
         (cond
           (< (anim/get-val scale) x-threshold-scale)
           (rescale c/min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (anim/animate pan-x max-offset)
             (anim/set-val pan-x-start max-offset)
             (anim/animate pinch-x c/init-offset)
             (anim/set-val pinch-x-start c/init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (anim/get-val pinch-x-start))
                 upper-bound (- (Math/abs max-offset) (anim/get-val pinch-x-start))]
             (anim/set-val pan-x-start (anim/get-val pan-x))
             (anim/animate-decay pan-x velocity [lower-bound upper-bound])
             (anim/animate-decay pan-x-start velocity [lower-bound upper-bound]))))))))


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
                         (anim/set-val pan-y (+ (anim/get-val pan-y-start) (oget e "translationY")))))
    (gesture/on-end
     (fn [e]
       (let [curr-offset (+ (anim/get-val pan-y) (anim/get-val pinch-y-start))
             max-offset  (utils/get-max-offset height screen-height (anim/get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)
             velocity    (* (oget e "velocityY") c/velocity-factor)]
         (cond
           (< (anim/get-val scale) y-threshold-scale)
           (rescale c/min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (anim/animate pan-y max-offset)
             (anim/set-val pan-y-start max-offset)
             (anim/animate pinch-y c/init-offset)
             (anim/set-val pinch-y-start c/init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (anim/get-val pinch-y-start))
                 upper-bound (- (Math/abs max-offset) (anim/get-val pinch-y-start))]
             (anim/set-val pan-y-start (anim/get-val pan-y))
             (anim/animate-decay pan-y velocity [lower-bound upper-bound])
             (anim/animate-decay pan-y-start velocity [lower-bound upper-bound]))))))))

(defn zoomable-image
  [{:keys [image-width image-height content message-id]} index border-radius on-tap]
  (let [set-full-height? (reagent/atom false)]
    [:f>
     (fn []
       (let [shared-element-id    (rf/sub [:shared-element-id])
             exit-lightbox-signal (rf/sub [:lightbox/exit-signal])
             zoom-out-signal      (rf/sub [:lightbox/zoom-out-signal])
             focused?             (= shared-element-id message-id)
             curr-orientation     (or (rf/sub [:lightbox/orientation]) orientation/portrait)
             dimensions           (utils/get-dimensions image-width image-height curr-orientation)
             animations           {:scale         (anim/use-val c/min-scale)
                                   :saved-scale   (anim/use-val c/min-scale)
                                   :pan-x-start   (anim/use-val c/init-offset)
                                   :pan-x         (anim/use-val c/init-offset)
                                   :pan-y-start   (anim/use-val c/init-offset)
                                   :pan-y         (anim/use-val c/init-offset)
                                   :pinch-x-start (anim/use-val c/init-offset)
                                   :pinch-x       (anim/use-val c/init-offset)
                                   :pinch-y-start (anim/use-val c/init-offset)
                                   :pinch-y       (anim/use-val c/init-offset)
                                   :pinch-x-max   (anim/use-val js/Infinity)
                                   :pinch-y-max   (anim/use-val js/Infinity)
                                   :rotate        (anim/use-val c/init-rotation)
                                   :rotate-scale  (anim/use-val c/min-scale)}
             props                {:pan-x-enabled? (reagent/atom false)
                                   :pan-y-enabled? (reagent/atom false)
                                   :focal-x        (reagent/atom nil)
                                   :focal-y        (reagent/atom nil)}
             rescale              (fn [value exit?]
                                    (utils/rescale-image value exit? dimensions animations props))]
         (rn/use-effect-once (fn []
                               (js/setTimeout #(reset! set-full-height? true) 500)
                               js/undefined))
         (when platform/ios?
           (utils/handle-orientation-change curr-orientation focused? dimensions animations props)
           (utils/handle-exit-lightbox-signal exit-lightbox-signal
                                              index
                                              (anim/get-val (:scale animations))
                                              rescale
                                              set-full-height?))
         (utils/handle-zoom-out-signal zoom-out-signal index (anim/get-val (:scale animations)) rescale)
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
                {:style (style/container dimensions
                                         animations
                                         @set-full-height?
                                         (= curr-orientation orientation/portrait))}
                [reanimated/fast-image
                 {:source    {:uri (:image content)}
                  :native-ID (when focused? :shared-element)
                  :style     (style/image dimensions animations border-radius)}]]]))]))]))
