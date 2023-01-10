(ns quo2.components.record-audio.record-audio.buttons.lock-button
  (:require [quo2.components.icon :as icons]
            [quo2.components.record-audio.record-audio.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.reanimated :as ra]
            [react-native.core :refer [use-effect]]
            [quo2.components.record-audio.record-audio.helpers :refer
             [animate-linear-with-delay
              animate-easing-with-delay
              animate-linear
              set-value]]))

(defn lock-button
  [recording? ready-to-lock? locked?]
  [:f>
   (fn []
     (let [translate-x-y             (ra/use-val 20)
           opacity                   (ra/use-val 0)
           connector-opacity         (ra/use-val 0)
           width                     (ra/use-val 24)
           height                    (ra/use-val 12)
           border-radius-first-half  (ra/use-val 8)
           border-radius-second-half (ra/use-val 8)
           start-x-y-animation       (fn []
                                       (animate-linear-with-delay translate-x-y 8 50 116.66)
                                       (animate-easing-with-delay connector-opacity 1 0 80)
                                       (animate-easing-with-delay width 56 83.33 63.33)
                                       (animate-easing-with-delay height 56 83.33 63.33)
                                       (animate-easing-with-delay border-radius-first-half
                                                                  28
                                                                  83.33
                                                                  63.33)
                                       (animate-easing-with-delay border-radius-second-half
                                                                  28
                                                                  83.33
                                                                  63.33))
           reset-x-y-animation       (fn []
                                       (animate-linear translate-x-y 0 100)
                                       (set-value connector-opacity 0)
                                       (set-value width 24)
                                       (set-value height 12)
                                       (set-value border-radius-first-half 8)
                                       (set-value border-radius-second-half 16))
           fade-in-animation         (fn []
                                       (animate-linear translate-x-y 0 220)
                                       (animate-linear opacity 1 220))
           fade-out-animation        (fn []
                                       (animate-linear translate-x-y 20 200)
                                       (animate-linear opacity 0 200)
                                       (set-value connector-opacity 0)
                                       (set-value width 24)
                                       (set-value height 12)
                                       (set-value border-radius-first-half 8)
                                       (set-value border-radius-second-half 16))]
       (use-effect (fn []
                     (if @recording?
                       (fade-in-animation)
                       (fade-out-animation)))
                   [@recording?])
       (use-effect (fn []
                     (cond
                       @ready-to-lock?
                       (start-x-y-animation)
                       (and @recording? (not @locked?))
                       (reset-x-y-animation)))
                   [@ready-to-lock?])
       (use-effect (fn []
                     (if @locked?
                       (fade-out-animation)
                       (reset-x-y-animation)))
                   [@locked?])
       [:<>
        [ra/view {:style (style/lock-button-container opacity)}
         [ra/view
          {:style (style/lock-button-connector connector-opacity
                                               width
                                               height
                                               border-radius-first-half
                                               border-radius-second-half)}]]
        [ra/view
         {:style          (style/lock-button translate-x-y opacity)
          :pointer-events :none}
         [icons/icon (if @ready-to-lock? :i/locked :i/unlocked)
          {:color (colors/theme-colors colors/black colors/white)
           :size  20}]]]))])
