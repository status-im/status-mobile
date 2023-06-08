(ns quo2.components.buttons.slide-button.view
  (:require
   [react-native.gesture :as gesture]
   [quo2.foundations.colors :as colors]
   [react-native.core :as rn :refer [use-effect]]
   [quo.react :as react]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]
   [quo2.foundations.typography :as typography]))

(defn init-animations [] {:x-pos (reanimated/use-shared-value 0)})

(def threshold-frac 0.7)
(def thumb-size 40)
(def track-padding 4)

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

(def slide-colors
  {:thumb (colors/custom-color-by-theme :blue 50 60)
   :text (:thumb slide-colors)
   :text-transparent colors/white-opa-40
   :track (colors/custom-color :blue 50 10)})

(defn thumb-style
  [{:keys [x-pos]} track-width]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (clamp-track x-pos track-width)}]}
   {:width  thumb-size
    :height thumb-size
    :border-radius 14
    :z-index 4
    :background-color (:thumb slide-colors)}))

(def track-style {:align-self       :stretch
                  :align-items      :flex-start
                  :justify-content  :center
                  :padding          track-padding
                  :height           48
                  :border-radius    12
                  :background-color (:track slide-colors)})

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn track-cover-style [{:keys [x-pos]} track-width]
  (reanimated/apply-animations-to-style
   {:left (interpolate-track-cover x-pos track-width)}
   (merge
    {:z-index 3
     :overflow :hidden} absolute-fill)))

(defn track-cover-text-container-style
  [track-width] {:position :absolute
                 :right 0
                 :top 0
                 :bottom 0
                 :align-items :center
                 :justify-content :center
                 :width @track-width})

(def track-text-style
  (merge {:color (:text slide-colors)}
         typography/paragraph-1
         typography/font-medium))

(defn log-slider-state
  [state]
  (use-effect #(do
                 (println (str "thumb-state: " @state)))
              [@state]))

(def timing-duration 200)

(defn animate-slide
  [value to-position]
  (reanimated/animate-shared-value-with-timing
   value to-position timing-duration :linear))

(defn animate-complete
  [value end-position]
  (reanimated/with-sequence
    (animate-slide value end-position)))

; (defn text-container [{:keys [x-pos text]}]
;   [])
;

(defn slider [{:keys [on-complete on-state-change track-text track-icon]}]
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
                        [(animate-slide x 0)
                         (reset-thumb-state)])
           nil)))
     [@thumb-state @track-width])

    [gesture/gesture-detector {:gesture (drag-gesture animations track-width thumb-state)}
     [reanimated/view {:style track-style
                       :on-layout (when-not
                                   (some? @track-width)
                                    on-track-layout)}
      [reanimated/view {:style (track-cover-style animations track-width)}
       [rn/view {:style (track-cover-text-container-style  track-width)}
        [rn/text {:style track-text-style} track-text]]]

      [reanimated/view {:style (thumb-style animations track-width)}]]]))

;; TODO 
;; - allow disabling the button through props
;; - figure out the themes and colors
;; - add documentation
;; 
;; PROPS:
;; - disabled
;; - on-complete (DONE)
;; - track-icon
;; - track-text
;; - size

(defn slide-button [{:keys [on-complete on-state-change track-text track-icon]} as props]
  [:f> slider {:on-complete on-complete
               :on-state-change on-state-change
               :track-text track-text
               :track-icon track-icon}])


