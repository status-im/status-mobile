(ns quo2.components.buttons.slide-button.animations
  (:require
   [quo2.components.buttons.slide-button.consts
    :refer [track-padding
            thumb-size
            timing-duration
            threshold-frac]]
   [react-native.gesture :as gesture]
   [quo.react :as react]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

(defn init-animations [] {:x-pos (reanimated/use-shared-value 0)})

(defn calc-usable-track [track-width]
  [0 (- (or @track-width 200) (* track-padding 2) thumb-size)])

(defn clamp-track [x-pos track-width]
  (let [track-dim (calc-usable-track track-width)]
    (reanimated/interpolate
     x-pos
     track-dim
     track-dim
     {:extrapolateLeft  "clamp"
      :extrapolateRight "clamp"})))

(defn interpolate-track-cover [x-pos track-width]
  (let [track-dim (calc-usable-track track-width)
        clamped (clamp-track x-pos track-width)]
    (reanimated/interpolate
     clamped
     track-dim
     (-> track-dim
         vec
         (assoc 0 (/ thumb-size 2)))
     {:extrapolateLeft  "clamp"
      :extrapolateRight "clamp"})))

(defn drag-gesture [{:keys [x-pos]} track-width thumb-state]
  (let [offset (react/state 0)
        complete-threshold (* @track-width threshold-frac)]

    (-> (gesture/gesture-pan)
        (gesture/enabled true)
        (gesture/on-update  (fn [event]
                              (let [x-translation (oops/oget event "translationX")
                                    x (+ x-translation @offset)]
                                (doall [(reanimated/set-shared-value x-pos x)
                                        (cond (not= @thumb-state :dragging) (reset! thumb-state :dragging))
                                        (when (>= x (- track-width thumb-size))
                                          (reset! thumb-state :complete))]))))

        (gesture/on-end (fn [event]
                          (let [x-translation (oops/oget event "translationX")
                                x (+ x-translation @offset)]
                            (if (<= x complete-threshold)
                              (reset! thumb-state :incomplete)
                              (reset! thumb-state :complete)))))

        (gesture/on-start  (fn [_]
                             (reset! offset (reanimated/get-shared-value x-pos)))))))

(defn animate-slide
  [value to-position]
  (reanimated/animate-shared-value-with-timing
   value to-position timing-duration :linear))

(defn animate-complete
  [value end-position]
  (reanimated/with-sequence
    (animate-slide value end-position)))


