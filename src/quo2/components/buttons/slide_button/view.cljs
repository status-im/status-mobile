(ns quo2.components.buttons.slide-button.view
  (:require
   [quo2.components.buttons.slide-button.style
    :refer [thumb-style
            track-style
            track-cover-style
            track-text-style
            track-cover-text-container-style]]
   [quo2.components.buttons.slide-button.animations
    :refer [init-animations drag-gesture animate-slide animate-complete]]
   [react-native.gesture :as gesture]
   [react-native.core :as rn :refer [use-effect]]
   [quo.react :as react]
   [oops.core :as oops]
   [react-native.reanimated :as reanimated]))

(defn slider [{:keys [on-complete on-state-change track-text track-icon]}]
  (let [animations (init-animations)
        track-width (react/state nil)
        thumb-state (react/state :rest)
        reset-thumb-state #(reset! thumb-state :rest)
        on-track-layout (fn [evt]
                          (let [width (oops/oget evt "nativeEvent" "layout" "width")]
                            (reset! track-width width)))]

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


