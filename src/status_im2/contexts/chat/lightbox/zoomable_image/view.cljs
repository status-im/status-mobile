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
       (if (= (reanimated/get-shared-value scale) c/min-scale)
         (let [translate-x (utils/get-double-tap-offset width screen-width (oget e "x"))
               translate-y (utils/get-double-tap-offset height screen-height (oget e "y"))]
           (when (> c/double-tap-scale x-threshold-scale)
             (reanimated/set-shared-value pan-x (reanimated/with-timing translate-x))
             (reanimated/set-shared-value pan-x-start translate-x))
           (when (> c/double-tap-scale y-threshold-scale)
             (reanimated/set-shared-value pan-y (reanimated/with-timing translate-y))
             (reanimated/set-shared-value pan-y-start translate-y))
           (rescale c/double-tap-scale))
         (rescale c/min-scale))))))

;; not using on-finalize because on-finalize gets called always regardless the gesture executed or not
(defn finalize-pinch
  [{:keys [width height screen-height screen-width x-threshold-scale y-threshold-scale]}
   {:keys [saved-scale scale pinch-x pinch-y pinch-x-start pinch-y-start pan-y pan-y-start pan-x
           pan-x-start]}
   {:keys [pan-x-enabled? pan-y-enabled?]}]
  (let [curr-offset-y (+ (reanimated/get-shared-value pan-y) (reanimated/get-shared-value pinch-y))
        max-offset-y  (utils/get-max-offset height screen-height (reanimated/get-shared-value scale))
        max-offset-y  (if (neg? curr-offset-y) (- max-offset-y) max-offset-y)
        curr-offset-x (+ (reanimated/get-shared-value pan-x) (reanimated/get-shared-value pinch-x))
        max-offset-x  (utils/get-max-offset width screen-width (reanimated/get-shared-value scale))
        max-offset-x  (if (neg? curr-offset-x) (- max-offset-x) max-offset-x)]
    (when (and (> (reanimated/get-shared-value scale) y-threshold-scale)
               (< (reanimated/get-shared-value scale) c/max-scale)
               (> (Math/abs curr-offset-y) (Math/abs max-offset-y)))
      (reanimated/set-shared-value pinch-y (reanimated/with-timing c/init-offset))
      (reanimated/set-shared-value pinch-y-start c/init-offset)
      (reanimated/set-shared-value pan-y (reanimated/with-timing max-offset-y))
      (reanimated/set-shared-value pan-y-start max-offset-y))
    (when (and (> (reanimated/get-shared-value scale) x-threshold-scale)
               (< (reanimated/get-shared-value scale) c/max-scale)
               (> (Math/abs curr-offset-x) (Math/abs max-offset-x)))
      (reanimated/set-shared-value pinch-x (reanimated/with-timing c/init-offset))
      (reanimated/set-shared-value pinch-x-start c/init-offset)
      (reanimated/set-shared-value pan-x (reanimated/with-timing max-offset-x))
      (reanimated/set-shared-value pan-x-start max-offset-x))
    (reset! pan-x-enabled? (> (reanimated/get-shared-value scale) x-threshold-scale))
    (reset! pan-y-enabled? (> (reanimated/get-shared-value scale) y-threshold-scale))
    (when platform/android?
      (rf/dispatch [:chat.ui/lightbox-scale (reanimated/get-shared-value saved-scale)]))))

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
    (gesture/on-update
     (fn [e]
       (let [new-scale   (* (oget e "scale") (reanimated/get-shared-value saved-scale))
             scale-diff  (utils/get-scale-diff new-scale (reanimated/get-shared-value saved-scale))
             new-pinch-x (utils/get-pinch-position scale-diff screen-width @focal-x)
             new-pinch-y (utils/get-pinch-position scale-diff screen-height @focal-y)]
         (when (and (>= new-scale c/max-scale)
                    (= (reanimated/get-shared-value pinch-x-max) js/Infinity))
           (reanimated/set-shared-value pinch-x-max (reanimated/get-shared-value pinch-x))
           (reanimated/set-shared-value pinch-y-max (reanimated/get-shared-value pinch-y)))
         (reanimated/set-shared-value pinch-x
                                      (+ new-pinch-x (reanimated/get-shared-value pinch-x-start)))
         (reanimated/set-shared-value pinch-y
                                      (+ new-pinch-y (reanimated/get-shared-value pinch-y-start)))
         (reanimated/set-shared-value scale new-scale))))
    (gesture/on-end
     (fn []
       (cond
         (< (reanimated/get-shared-value scale) c/min-scale)
         (rescale c/min-scale)
         (> (reanimated/get-shared-value scale) c/max-scale)
         (do
           (reanimated/set-shared-value pinch-x
                                        (reanimated/with-timing (reanimated/get-shared-value
                                                                 pinch-x-max)))
           (reanimated/set-shared-value pinch-x-start (reanimated/get-shared-value pinch-x-max))
           (reanimated/set-shared-value pinch-x-max js/Infinity)
           (reanimated/set-shared-value pinch-y
                                        (reanimated/with-timing (reanimated/get-shared-value
                                                                 pinch-y-max)))
           (reanimated/set-shared-value pinch-y-start (reanimated/get-shared-value pinch-y-max))
           (reanimated/set-shared-value pinch-y-max js/Infinity)
           (reanimated/set-shared-value scale (reanimated/with-timing c/max-scale))
           (reanimated/set-shared-value saved-scale c/max-scale))
         :else
         (do
           (reanimated/set-shared-value saved-scale (reanimated/get-shared-value scale))
           (reanimated/set-shared-value pinch-x-start (reanimated/get-shared-value pinch-x))
           (reanimated/set-shared-value pinch-y-start (reanimated/get-shared-value pinch-y))
           (when (< (reanimated/get-shared-value scale) x-threshold-scale)
             (utils/center-x animations false))
           (when (< (reanimated/get-shared-value scale) y-threshold-scale)
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
                         (reanimated/set-shared-value pan-x
                                                      (+ (reanimated/get-shared-value pan-x-start)
                                                         (oget e "translationX")))))
    (gesture/on-end
     (fn [e]
       (let [curr-offset (+ (reanimated/get-shared-value pan-x)
                            (reanimated/get-shared-value pinch-x-start))
             max-offset  (utils/get-max-offset width screen-width (reanimated/get-shared-value scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)
             velocity    (* (oget e "velocityX") c/velocity-factor)]
         (cond
           (< (reanimated/get-shared-value scale) x-threshold-scale)
           (rescale c/min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (reanimated/set-shared-value pan-x (reanimated/with-timing max-offset))
             (reanimated/set-shared-value pan-x-start max-offset)
             (reanimated/set-shared-value pinch-x (reanimated/with-timing c/init-offset))
             (reanimated/set-shared-value pinch-x-start c/init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (reanimated/get-shared-value pinch-x-start))
                 upper-bound (- (Math/abs max-offset) (reanimated/get-shared-value pinch-x-start))]
             (reanimated/set-shared-value pan-x-start (reanimated/get-shared-value pan-x))
             (reanimated/animate-shared-value-with-decay pan-x velocity [lower-bound upper-bound])
             (reanimated/animate-shared-value-with-decay pan-x-start
                                                         velocity
                                                         [lower-bound upper-bound]))))))))


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
                         (reanimated/set-shared-value pan-y
                                                      (+ (reanimated/get-shared-value pan-y-start)
                                                         (oget e "translationY")))))
    (gesture/on-end
     (fn [e]
       (let [curr-offset (+ (reanimated/get-shared-value pan-y)
                            (reanimated/get-shared-value pinch-y-start))
             max-offset  (utils/get-max-offset height screen-height (reanimated/get-shared-value scale))
             max-offset  (if (neg? curr-offset) (- max-offset) max-offset)
             velocity    (* (oget e "velocityY") c/velocity-factor)]
         (cond
           (< (reanimated/get-shared-value scale) y-threshold-scale)
           (rescale c/min-scale)
           (> (Math/abs curr-offset) (Math/abs max-offset))
           (do
             (reanimated/set-shared-value pan-y (reanimated/with-timing max-offset))
             (reanimated/set-shared-value pan-y-start max-offset)
             (reanimated/set-shared-value pinch-y (reanimated/with-timing c/init-offset))
             (reanimated/set-shared-value pinch-y-start c/init-offset))
           :else
           (let [lower-bound (- (- (Math/abs max-offset)) (reanimated/get-shared-value pinch-y-start))
                 upper-bound (- (Math/abs max-offset) (reanimated/get-shared-value pinch-y-start))]
             (reanimated/set-shared-value pan-y-start (reanimated/get-shared-value pan-y))
             (reanimated/animate-shared-value-with-decay pan-y velocity [lower-bound upper-bound])
             (reanimated/animate-shared-value-with-decay pan-y-start
                                                         velocity
                                                         [lower-bound upper-bound]))))))))

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
             animations           {:scale         (reanimated/use-shared-value initial-scale)
                                   :saved-scale   (reanimated/use-shared-value initial-scale)
                                   :pan-x-start   (reanimated/use-shared-value c/init-offset)
                                   :pan-x         (reanimated/use-shared-value c/init-offset)
                                   :pan-y-start   (reanimated/use-shared-value c/init-offset)
                                   :pan-y         (reanimated/use-shared-value c/init-offset)
                                   :pinch-x-start (reanimated/use-shared-value c/init-offset)
                                   :pinch-x       (reanimated/use-shared-value c/init-offset)
                                   :pinch-y-start (reanimated/use-shared-value c/init-offset)
                                   :pinch-y       (reanimated/use-shared-value c/init-offset)
                                   :pinch-x-max   (reanimated/use-shared-value js/Infinity)
                                   :pinch-y-max   (reanimated/use-shared-value js/Infinity)
                                   :rotate        (reanimated/use-shared-value c/init-rotation)
                                   :rotate-scale  (reanimated/use-shared-value c/min-scale)}
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
                                              (reanimated/get-shared-value (:scale animations))
                                              rescale
                                              set-full-height?))
         (utils/handle-zoom-out-signal zoom-out-signal
                                       index
                                       (reanimated/get-shared-value (:scale animations))
                                       rescale)
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
