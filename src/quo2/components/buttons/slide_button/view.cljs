(ns quo2.components.buttons.slide-button.view
  (:require
   [react-native.gesture :as gesture]
   [react-native.core :refer [use-effect]]
   [quo.react :as react]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

(defn init-animations [] {:x-pos (reanimated/use-shared-value 0)})

(def threshold-frac 0.7)
(def thumb-width 40)

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
                                        (when (>= x (- track-width thumb-width))
                                          (reset! thumb-state :complete))]))))

        (gesture/on-end (fn [event]
                          (let [x-translation (oops/oget event "translationX")
                                x (+ x-translation @offset)]
                            (if (<= x complete-threshold)
                              (reset! thumb-state :incomplete)
                              (reset! thumb-state :complete)))))

        (gesture/on-start  (fn [_]
                             (reset! offset (reanimated/get-shared-value x-pos)))))))

(defn thumb-style
  [{:keys [x-pos]}]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x x-pos}]}
   {:width  40
    :height 40
    :border-radius 14
    :background-color :blue}))

(def track-style {:width            "100%"
                  :height           40
                  :border-radius    12
                  :background-color :red})

(defn log-slider-state
  [state]
  (use-effect #(do
                 (println (str "thumb-state: " @state)))
              [@state]))

(defn animate-slide
  [value to-position]
  (reanimated/animate-shared-value-with-timing
   value to-position 300 :linear))

(defn animate-complete
  [value end-position]
  (reanimated/with-sequence
    (animate-slide value end-position)))

(defn slider [{:keys [on-complete on-state-change]}]
  (let [animations (init-animations)
        track-width (react/state nil)
        thumb-state (react/state :rest)
        reset-thumb-state #(reset! thumb-state :rest)
        on-track-layout (fn [evt]
                          (let [width (oops/oget evt "nativeEvent" "layout" "width")]
                            (reset! track-width width)))]

    ;;(log-slider-state thumb-state)

    (use-effect
     (fn [] (cond
              (not (nil? on-state-change))
              (on-state-change @thumb-state)))
     [@thumb-state])

    (use-effect
     (fn []
       (let [x (animations :x-pos)]
         (case @thumb-state
           :complete (doall
                      [(animate-complete x @track-width)
                       (on-complete)])
           :incomplete (doall
                        [(reanimated/animate-shared-value-with-timing
                          x 0 300 :linear)
                         (reset-thumb-state)])
           nil)))
     [@thumb-state @track-width])

    [gesture/gesture-detector {:gesture (drag-gesture animations track-width thumb-state)}
     [reanimated/view {:style track-style
                       :on-layout (when-not
                                   (some? @track-width)
                                    on-track-layout)}
      [reanimated/view {:style (thumb-style animations)}]]]))

;; TODO 
;; - allow disabling the button through props
;; - figure out the themes and colors
;; 
;; PROPS:
;; - disabled
;; - on-complete
;; - track-icon
;; - track-text

(defn slide-button [{:keys [on-complete on-state-change]} as props]
  [:f> slider {:on-complete on-complete
               :on-state-change on-state-change}])


