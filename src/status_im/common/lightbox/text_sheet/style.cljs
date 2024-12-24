(ns status-im.common.lightbox.text-sheet.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.reanimated :as reanimated]
    [status-im.common.lightbox.constants :as constants]))

(defn sheet-container
  [{:keys [height top]}]
  (reanimated/apply-animations-to-style
   {:height height
    :top    top}
   {:position :absolute
    :left     0
    :right    0}))

(def bar-container
  {:height          constants/bar-container-height
   :left            0
   :right           0
   :top             0
   :justify-content :center
   :align-items     :center})

(def bar
  {:width            32
   :height           4
   :border-radius    100
   :background-color colors/white-opa-10})

(defn top-gradient
  [{:keys [derived-value]} {:keys [top]} insets max-height]
  (let [initial-top (- (+ (:top insets)
                          constants/top-view-height))
        height      (+ (:top insets)
                       constants/top-view-height
                       (* constants/line-height 2))]
    (reanimated/apply-animations-to-style
     {:opacity (reanimated/interpolate derived-value
                                       [max-height (+ max-height constants/line-height)]
                                       [0 1])
      :top     (reanimated/interpolate top
                                       [0 (- max-height)]
                                       [initial-top (- initial-top max-height)]
                                       {:extrapolateLeft  "clamp"
                                        :extrapolateRight "clamp"})}
     {:position :absolute
      :left     0
      :height   height
      :right    0
      :z-index  1})))

(defn bottom-gradient
  [bottom-inset]
  (let [gradient-distance (+ constants/small-list-height bottom-inset)]
    {:position :absolute
     :left     0
     :right    0
     :height   (+ gradient-distance (* 2 constants/line-height))
     :bottom   (- gradient-distance)
     :opacity  0.8
     :z-index  1}))
