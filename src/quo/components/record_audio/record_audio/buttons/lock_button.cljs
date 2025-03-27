(ns quo.components.record-audio.record-audio.buttons.lock-button
  (:require
    [quo.components.icon :as icons]
    [quo.components.record-audio.record-audio.helpers :as helpers]
    [quo.components.record-audio.record-audio.style :as style]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]))

(defn lock-button
  [recording? ready-to-lock? locked?]
  (let [theme                     (quo.context/use-theme)
        translate-x-y             (reanimated/use-shared-value 20)
        opacity                   (reanimated/use-shared-value 0)
        connector-opacity         (reanimated/use-shared-value 0)
        width                     (reanimated/use-shared-value 24)
        height                    (reanimated/use-shared-value 12)
        border-radius-first-half  (reanimated/use-shared-value 8)
        border-radius-second-half (reanimated/use-shared-value 8)
        start-x-y-animation       (rn/use-callback
                                   (fn []
                                     (helpers/animate-linear-with-delay translate-x-y 8 50 116.66)
                                     (helpers/animate-easing-with-delay connector-opacity 1 0 80)
                                     (helpers/animate-easing-with-delay width 56 83.33 63.33)
                                     (helpers/animate-easing-with-delay height 56 83.33 63.33)
                                     (helpers/animate-easing-with-delay border-radius-first-half
                                                                        28
                                                                        83.33
                                                                        63.33)
                                     (helpers/animate-easing-with-delay border-radius-second-half
                                                                        28
                                                                        83.33
                                                                        63.33)))
        reset-x-y-animation       (rn/use-callback
                                   (fn []
                                     (helpers/animate-linear translate-x-y 0 100)
                                     (helpers/set-value connector-opacity 0)
                                     (helpers/set-value width 24)
                                     (helpers/set-value height 12)
                                     (helpers/set-value border-radius-first-half 8)
                                     (helpers/set-value border-radius-second-half 16)))
        fade-in-animation         (rn/use-callback
                                   (fn []
                                     (helpers/animate-linear translate-x-y 0 220)
                                     (helpers/animate-linear opacity 1 220)))
        fade-out-animation        (rn/use-callback
                                   (fn []
                                     (helpers/animate-linear translate-x-y 20 200)
                                     (helpers/animate-linear opacity 0 200)
                                     (helpers/set-value connector-opacity 0)
                                     (helpers/set-value width 24)
                                     (helpers/set-value height 12)
                                     (helpers/set-value border-radius-first-half 8)
                                     (helpers/set-value border-radius-second-half 16)))]
    (rn/use-effect (fn []
                     (if recording?
                       (fade-in-animation)
                       (fade-out-animation)))
                   [recording?])
    (rn/use-effect (fn []
                     (cond
                       ready-to-lock?
                       (start-x-y-animation)
                       (and recording? (not locked?))
                       (reset-x-y-animation)))
                   [ready-to-lock?])
    (rn/use-effect (fn []
                     (if locked?
                       (fade-out-animation)
                       (reset-x-y-animation)))
                   [locked?])
    [:<>
     [reanimated/view {:style (style/lock-button-container opacity)}
      [reanimated/view
       {:style (style/lock-button-connector connector-opacity
                                            width
                                            height
                                            border-radius-first-half
                                            border-radius-second-half
                                            theme)}]]
     [reanimated/view
      {:style          (style/lock-button translate-x-y opacity theme)
       :pointer-events :none}
      [icons/icon (if ready-to-lock? :i/locked :i/unlocked)
       {:color (colors/theme-colors colors/black colors/white theme)
        :size  20}]]]))
