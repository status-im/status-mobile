(ns quo2.components.record-audio.record-audio.buttons.delete-button
  (:require [quo2.components.icon :as icons]
            [quo2.components.record-audio.record-audio.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [react-native.core :refer [use-effect]]
            [quo2.components.record-audio.record-audio.helpers :refer
             [animate-linear-with-delay
              animate-easing-with-delay
              animate-linear
              set-value]]))

(defn delete-button
  [recording? ready-to-delete? reviewing-audio?]
  [:f>
   (fn []
     (let [opacity                   (reanimated/use-shared-value 0)
           translate-x               (reanimated/use-shared-value 20)
           scale                     (reanimated/use-shared-value 1)
           connector-opacity         (reanimated/use-shared-value 0)
           connector-width           (reanimated/use-shared-value 24)
           connector-height          (reanimated/use-shared-value 12)
           border-radius-first-half  (reanimated/use-shared-value 8)
           border-radius-second-half (reanimated/use-shared-value 8)
           start-x-animation         (fn []
                                       (animate-linear-with-delay translate-x 12 50 133.33)
                                       (animate-easing-with-delay connector-opacity 1 0 93.33)
                                       (animate-easing-with-delay connector-width 56 83.33 80)
                                       (animate-easing-with-delay connector-height 56 83.33 80)
                                       (animate-easing-with-delay border-radius-first-half 28 83.33 80)
                                       (animate-easing-with-delay border-radius-second-half 28 83.33 80))
           reset-x-animation         (fn []
                                       (animate-linear translate-x 0 100)
                                       (set-value connector-opacity 0)
                                       (set-value connector-width 24)
                                       (set-value connector-height 12)
                                       (set-value border-radius-first-half 8)
                                       (set-value border-radius-second-half 16))
           fade-in-animation         (fn []
                                       (animate-linear translate-x 0 200)
                                       (animate-linear opacity 1 200))
           fade-out-animation        (fn []
                                       (animate-linear
                                        translate-x
                                        (if @reviewing-audio? 35 20)
                                        200)
                                       (if @reviewing-audio?
                                         (animate-linear scale 0.75 200)
                                         (animate-linear opacity 0 200))
                                       (set-value connector-opacity 0)
                                       (set-value connector-width 24)
                                       (set-value connector-height 12)
                                       (set-value border-radius-first-half 8)
                                       (set-value border-radius-second-half 16))
           fade-out-reset-animation  (fn []
                                       (animate-linear opacity 0 200)
                                       (animate-linear-with-delay translate-x 20 0 200)
                                       (animate-linear-with-delay scale 1 0 200))]
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
                       @ready-to-delete?
                       (start-x-animation)
                       @recording?
                       (reset-x-animation)))
                   [@ready-to-delete?])
       [:<>
        [reanimated/view {:style (style/delete-button-container opacity)}
         [reanimated/view
          {:style (style/delete-button-connector connector-opacity
                                                 connector-width
                                                 connector-height
                                                 border-radius-first-half
                                                 border-radius-second-half)}]]
        [reanimated/view
         {:style          (style/delete-button scale translate-x opacity)
          :pointer-events :none}
         [icons/icon :i/delete
          {:color colors/white
           :size  20}]]]))])
