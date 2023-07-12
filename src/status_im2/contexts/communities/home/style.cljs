(ns status-im2.contexts.communities.home.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(def header-height 245)

(def tabs
  {:padding-horizontal 20
   :padding-top        8
   :padding-bottom     12})

(def blur
  {:position :absolute
   :top      0
   :right    0
   :left     0
   :bottom   0})

(defn empty-state-container
  [top]
  {:margin-top      (+ header-height top)
   :margin-bottom   44
   :flex            1
   :justify-content :center})

(def empty-state-placeholder
  {:height           120
   :width            120
   :background-color colors/danger-50})

(defn header-spacing
  [top]
  {:height (+ header-height top)})

(defn blur-container
  [top]
  {:overflow    (if platform/ios? :visible :hidden)
   :position    :absolute
   :z-index     1
   :top         0
   :right       0
   :left        0
   :padding-top top})

;;;; CARD ANIMATION
(def card-bottom-override {:margin-bottom 16}) ; Original 8 + 8 from tabs top padding
(def card-height (+ 56 16)) ; Card height + its vertical margins
(def card-total-height (+ card-height 8)) ; added 8 from tabs top padding
(def card-opacity-factor (/ 100 card-height 100))

(defn- value-in-range
  "Returns `num` if is in the range [`lower-bound` `upper-bound`]
  if `num` exceeds a given bound, then returns the bound exceeded."
  [num [lower-bound upper-bound]]
  (max lower-bound (min num upper-bound)))

(defn set-animated-card-values
  [{:keys [scroll-offset height translation-y opacity]}]
  (let [new-height        (- card-total-height scroll-offset)
        new-opacity       (* (- card-height scroll-offset) card-opacity-factor)
        new-translation-y (- scroll-offset)]
    (reanimated/set-shared-value height (value-in-range new-height [0 card-total-height]))
    (reanimated/set-shared-value opacity (value-in-range new-opacity [0 1]))
    (reanimated/set-shared-value translation-y
                                 (value-in-range new-translation-y [(- card-total-height) 0]))))

(defn animated-card-container
  [height opacity]
  (reanimated/apply-animations-to-style {:height height :opacity opacity}
                                        {:overflow :hidden}))

(defn animated-card-translation
  [translate-y]
  (reanimated/apply-animations-to-style {:transform [{:translate-y translate-y}]}
                                        {}))
