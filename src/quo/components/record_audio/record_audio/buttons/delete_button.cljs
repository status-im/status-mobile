(ns quo.components.record-audio.record-audio.buttons.delete-button
  (:require
    [quo.components.icon :as icons]
    [quo.components.record-audio.record-audio.helpers :as helpers]
    [quo.components.record-audio.record-audio.style :as style]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]))

(defn delete-button
  [recording? ready-to-delete? reviewing-audio? force-show-controls?]
  (let [opacity                   (reanimated/use-shared-value (if force-show-controls? 1 0))
        translate-x               (reanimated/use-shared-value (if force-show-controls? 35 20))
        scale                     (reanimated/use-shared-value (if force-show-controls? 0.75 1))
        connector-opacity         (reanimated/use-shared-value 0)
        connector-width           (reanimated/use-shared-value 24)
        connector-height          (reanimated/use-shared-value 12)
        border-radius-first-half  (reanimated/use-shared-value 8)
        border-radius-second-half (reanimated/use-shared-value 8)
        start-x-animation         (rn/use-callback
                                   (fn []
                                     (helpers/animate-linear-with-delay translate-x 12 50 133.33)
                                     (helpers/animate-easing-with-delay connector-opacity 1 0 93.33)
                                     (helpers/animate-easing-with-delay connector-width 56 83.33 80)
                                     (helpers/animate-easing-with-delay connector-height 56 83.33 80)
                                     (helpers/animate-easing-with-delay border-radius-first-half
                                                                        28
                                                                        83.33
                                                                        80)
                                     (helpers/animate-easing-with-delay border-radius-second-half
                                                                        28
                                                                        83.33
                                                                        80)))
        reset-x-animation         (rn/use-callback
                                   (fn []
                                     (helpers/animate-linear translate-x 0 100)
                                     (helpers/set-value connector-opacity 0)
                                     (helpers/set-value connector-width 24)
                                     (helpers/set-value connector-height 12)
                                     (helpers/set-value border-radius-first-half 8)
                                     (helpers/set-value border-radius-second-half 16)))
        fade-in-animation         (rn/use-callback
                                   (fn []
                                     (helpers/animate-linear translate-x 0 200)
                                     (helpers/animate-linear opacity 1 200)))
        fade-out-animation        (rn/use-callback
                                   (fn []
                                     (helpers/animate-linear
                                      translate-x
                                      (if reviewing-audio? 35 20)
                                      200)
                                     (if reviewing-audio?
                                       (helpers/animate-linear scale 0.75 200)
                                       (helpers/animate-linear opacity 0 200))
                                     (helpers/set-value connector-opacity 0)
                                     (helpers/set-value connector-width 24)
                                     (helpers/set-value connector-height 12)
                                     (helpers/set-value border-radius-first-half 8)
                                     (helpers/set-value border-radius-second-half 16))
                                   [reviewing-audio?])
        fade-out-reset-animation  (rn/use-callback
                                   (fn []
                                     (helpers/animate-linear opacity 0 200)
                                     (helpers/animate-linear-with-delay translate-x 20 0 200)
                                     (helpers/animate-linear-with-delay scale 1 0 200)))]
    (rn/use-effect (fn []
                     (if recording?
                       (fade-in-animation)
                       (fade-out-animation)))
                   [recording?])
    (rn/use-effect (fn []
                     (when-not reviewing-audio?
                       (fade-out-reset-animation)))
                   [reviewing-audio?])
    (rn/use-effect (fn []
                     (cond
                       ready-to-delete?
                       (start-x-animation)
                       recording?
                       (reset-x-animation)))
                   [ready-to-delete?])
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
        :size  20}]]]))
