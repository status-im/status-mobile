(ns quo2.components.record-audio.record-audio.buttons.send-button
  (:require [quo2.components.icon :as icons]
            [quo2.components.record-audio.record-audio.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [react-native.core :refer [use-effect]]
            [quo2.components.record-audio.record-audio.helpers :refer
             [animate-linear
              animate-linear-with-delay
              animate-easing-with-delay
              set-value]]))

(defn send-button
  [recording? ready-to-send? reviewing-audio?]
  [:f>
   (fn []
     (let [opacity                   (reanimated/use-shared-value 0)
           translate-y               (reanimated/use-shared-value 20)
           connector-opacity         (reanimated/use-shared-value 0)
           width                     (reanimated/use-shared-value 12)
           height                    (reanimated/use-shared-value 24)
           border-radius-first-half  (reanimated/use-shared-value 16)
           border-radius-second-half (reanimated/use-shared-value 8)
           start-y-animation         (fn []
                                       (animate-linear-with-delay translate-y 12 50 133.33)
                                       (animate-easing-with-delay connector-opacity 1 0 93.33)
                                       (animate-easing-with-delay width 56 83.33 80)
                                       (animate-easing-with-delay height 56 83.33 80)
                                       (animate-easing-with-delay border-radius-first-half 28 83.33 80)
                                       (animate-easing-with-delay border-radius-second-half 28 83.33 80))
           reset-y-animation         (fn []
                                       (animate-linear translate-y 0 100)
                                       (set-value connector-opacity 0)
                                       (set-value width 12)
                                       (set-value height 24)
                                       (set-value border-radius-first-half 16)
                                       (set-value border-radius-second-half 8))
           fade-in-animation         (fn []
                                       (animate-linear translate-y 0 200)
                                       (animate-linear opacity 1 200))
           fade-out-animation        (fn []
                                       (animate-linear
                                        translate-y
                                        (if @reviewing-audio? 76 20)
                                        200)
                                       (when-not @reviewing-audio?
                                         (animate-linear opacity 0 200))
                                       (set-value connector-opacity 0)
                                       (set-value width 24)
                                       (set-value height 12)
                                       (set-value border-radius-first-half 8)
                                       (set-value border-radius-second-half 16))
           fade-out-reset-animation  (fn []
                                       (animate-linear opacity 0 200)
                                       (animate-linear-with-delay translate-y 20 0 200)
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
                     (when-not @reviewing-audio?
                       (fade-out-reset-animation)))
                   [@reviewing-audio?])
       (use-effect (fn []
                     (cond
                       @ready-to-send?
                       (start-y-animation)
                       @recording?     (reset-y-animation)))
                   [@ready-to-send?])
       [:<>
        [reanimated/view {:style (style/send-button-container opacity)}
         [reanimated/view
          {:style (style/send-button-connector connector-opacity
                                               width
                                               height
                                               border-radius-first-half
                                               border-radius-second-half)}]]
        [reanimated/view
         {:style          (style/send-button translate-y opacity)
          :pointer-events :none}
         [icons/icon :i/arrow-up
          {:color           colors/white
           :size            20
           :container-style style/send-icon-container}]]]))])
