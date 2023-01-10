(ns status-im2.contexts.chat.lightbox.zoomable-image.view
  (:require
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.reanimated :as ra]
    [reagent.core :as reagent]
    [utils.re-frame :as rf]
    [oops.core :refer [oget]]
    [react-native.orientation :as orientation]
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
       (if (= (ra/get-val scale) c/min-scale)
         (let [translate-x (utils/get-double-tap-offset width screen-width (oget e "x"))
               translate-y (utils/get-double-tap-offset height screen-height (oget e "y"))]
           (when (> c/double-tap-scale x-threshold-scale)
             (ra/animate pan-x translate-x)
             (ra/set-val pan-x-start translate-x))
           (when (> c/double-tap-scale y-threshold-scale)
             (ra/animate pan-y translate-y)
             (ra/set-val pan-y-start translate-y))
           (rescale c/double-tap-scale))
         (rescale c/min-scale))))))

;; not using on-finalize because on-finalize gets called always regardless the gesture executed or not
(defn finalize-pinch
  [{:keys [width height screen-height screen-width x-threshold-scale y-threshold-scale]}
   {:keys [saved-scale scale pinch-x pinch-y pinch-x-start pinch-y-start pan-y pan-y-start pan-x
           pan-x-start]}
   {:keys [pan-x-enabled? pan-y-enabled?]}]
  (let [curr-offset-y (+ (ra/get-val pan-y) (ra/get-val pinch-y))
        max-offset-y  (utils/get-max-offset height screen-height (ra/get-val scale))
        max-offset-y  (if (neg? curr-offset-y) (- max-offset-y) max-offset-y)
        curr-offset-x (+ (ra/get-val pan-x) (ra/get-val pinch-x))
        max-offset-x  (utils/get-max-offset width screen-width (ra/get-val scale))
        max-offset-x  (if (neg? curr-offset-x) (- max-offset-x) max-offset-x)]
    (when (and (> (ra/get-val scale) y-threshold-scale)
               (< (ra/get-val scale) c/max-scale)
               (> (Math/abs curr-offset-y) (Math/abs max-offset-y)))
      (ra/animate pinch-y c/init-offset)
      (ra/set-val pinch-y-start c/init-offset)
      (ra/animate pan-y max-offset-y)
      (ra/set-val pan-y-start max-offset-y))
    (when (and (> (ra/get-val scale) x-threshold-scale)
               (< (ra/get-val scale) c/max-scale)
               (> (Math/abs curr-offset-x) (Math/abs max-offset-x)))
      (ra/animate pinch-x c/init-offset)
      (ra/set-val pinch-x-start c/init-offset)
      (ra/animate pan-x max-offset-x)
      (ra/set-val pan-x-start max-offset-x))
    (reset! pan-x-enabled? (> (ra/get-val scale) x-threshold-scale))
    (reset! pan-y-enabled? (> (ra/get-val scale) y-threshold-scale))
    (when platform/android?
      (rf/dispatch [:chat.ui/lightbox-scale (ra/get-val saved-scale)]))))

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
                         (let [new-scale   (* (oget e "scale") (ra/get-val saved-scale))
                               scale-diff  (utils/get-scale-diff new-scale (ra/get-val saved-scale))
                               new-pinch-x (utils/get-pinch-position scale-diff screen-width @focal-x)
                               new-pinch-y (utils/get-pinch-position scale-diff screen-height @focal-y)]
                           (when (and (>= new-scale c/max-scale)
                                      (= (ra/get-val pinch-x-max) js/Infinity))
                             (ra/set-val pinch-x-max (ra/get-val pinch-x))
                             (ra/set-val pinch-y-max (ra/get-val pinch-y)))
                           (ra/set-val pinch-x (+ new-pinch-x (ra/get-val pinch-x-start)))
                           (ra/set-val pinch-y (+ new-pinch-y (ra/get-val pinch-y-start)))
                           (ra/set-val scale new-scale))))
    (gesture/on-end
     (fn []
       (cond
         (< (ra/get-val scale) c/min-scale)
         (rescale c/min-scale)
         (> (ra/get-val scale) c/max-scale)
         (do
           (ra/animate pinch-x (ra/get-val pinch-x-max))
           (ra/set-val pinch-x-start (ra/get-val pinch-x-max))
           (ra/set-val pinch-x-max js/Infinity)
           (ra/animate pinch-y (ra/get-val pinch-y-max))
           (ra/set-val pinch-y-start (ra/get-val pinch-y-max))
           (ra/set-val pinch-y-max js/Infinity)
           (ra/animate scale c/max-scale)
           (ra/set-val saved-scale c/max-scale))
         :else
         (do
           (ra/set-val saved-scale (ra/get-val scale))
           (ra/set-val pinch-x-start (ra/get-val pinch-x))
           (ra/set-val pinch-y-start (ra/get-val pinch-y))
           (when (< (ra/get-val scale) x-threshold-scale)
             (utils/center-x animations false))
           (when (< (ra/get-val scale) y-threshold-scale)
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
                         (ra/set-val pan-x (+ (ra/get-val pan-x-start) (oget e "translationX")))))
    (gesture/on-end
     (fn [e]
       (let [curr-offset (+ (ra/get-val pan-x) (ra/get-val pinch-x-start))
             max-offset  (utils/get-max-offset width screen-width (ra/get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)
             velocity    (* (oget e "velocityX") c/velocity-factor)]
         (cond
           (< (ra/get-val scale) x-threshold-scale)
           (rescale c/min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (ra/animate pan-x max-offset)
             (ra/set-val pan-x-start max-offset)
             (ra/animate pinch-x c/init-offset)
             (ra/set-val pinch-x-start c/init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (ra/get-val pinch-x-start))
                 upper-bound (- (Math/abs max-offset) (ra/get-val pinch-x-start))]
             (ra/set-val pan-x-start (ra/get-val pan-x))
             (ra/animate-decay pan-x velocity [lower-bound upper-bound])
             (ra/animate-decay pan-x-start velocity [lower-bound upper-bound]))))))))


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
                         (ra/set-val pan-y (+ (ra/get-val pan-y-start) (oget e "translationY")))))
    (gesture/on-end
     (fn [e]
       (let [curr-offset (+ (ra/get-val pan-y) (ra/get-val pinch-y-start))
             max-offset  (utils/get-max-offset height screen-height (ra/get-val scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)
             velocity    (* (oget e "velocityY") c/velocity-factor)]
         (cond
           (< (ra/get-val scale) y-threshold-scale)
           (rescale c/min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (ra/animate pan-y max-offset)
             (ra/set-val pan-y-start max-offset)
             (ra/animate pinch-y c/init-offset)
             (ra/set-val pinch-y-start c/init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (ra/get-val pinch-y-start))
                 upper-bound (- (Math/abs max-offset) (ra/get-val pinch-y-start))]
             (ra/set-val pan-y-start (ra/get-val pan-y))
             (ra/animate-decay pan-y velocity [lower-bound upper-bound])
             (ra/animate-decay pan-y-start velocity [lower-bound upper-bound]))))))))

(defn zoomable-image
  [{:keys [image-width image-height content message-id]} index border-radius on-tap]
  (let [set-full-height? (reagent/atom false)]
    [:f>
     (fn []
       (let [shared-element-id    (rf/sub [:shared-element-id])
             exit-lightbox-signal (rf/sub [:lightbox/exit-signal])
             zoom-out-signal      (rf/sub [:lightbox/zoom-out-signal])
             initial-scale        (if platform/ios? c/min-scale (rf/sub [:lightbox/scale]))
             curr-orientation     (or (rf/sub [:lightbox/orientation]) orientation/portrait)
             focused?             (= shared-element-id message-id)
             dimensions           (utils/get-dimensions image-width image-height curr-orientation)
             animations           {:scale         (ra/use-val initial-scale)
                                   :saved-scale   (ra/use-val initial-scale)
                                   :pan-x-start   (ra/use-val c/init-offset)
                                   :pan-x         (ra/use-val c/init-offset)
                                   :pan-y-start   (ra/use-val c/init-offset)
                                   :pan-y         (ra/use-val c/init-offset)
                                   :pinch-x-start (ra/use-val c/init-offset)
                                   :pinch-x       (ra/use-val c/init-offset)
                                   :pinch-y-start (ra/use-val c/init-offset)
                                   :pinch-y       (ra/use-val c/init-offset)
                                   :pinch-x-max   (ra/use-val js/Infinity)
                                   :pinch-y-max   (ra/use-val js/Infinity)
                                   :rotate        (ra/use-val c/init-rotation)
                                   :rotate-scale  (ra/use-val c/min-scale)}
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
                                              (ra/get-val (:scale animations))
                                              rescale
                                              set-full-height?))
         (utils/handle-zoom-out-signal zoom-out-signal index (ra/get-val (:scale animations)) rescale)
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
               [ra/view
                {:style (style/container dimensions animations @set-full-height?)}
                [ra/fast-image
                 {:source    {:uri (:image content)}
                  :native-ID (when focused? :shared-element)
                  :style     (style/image dimensions animations border-radius)}]]]))]))]))
