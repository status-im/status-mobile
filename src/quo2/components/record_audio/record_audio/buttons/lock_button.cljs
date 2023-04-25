(ns quo2.components.record-audio.record-audio.buttons.lock-button
  (:require [quo2.components.icon :as icons]
            [quo2.components.record-audio.record-audio.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [react-native.core :refer [use-effect]]
            [quo2.components.record-audio.record-audio.helpers :as helpers]))

(defn f-lock-button
  [recording? ready-to-lock? locked?]
  (let [translate-x-y             (reanimated/use-shared-value 20)
        opacity                   (reanimated/use-shared-value 0)
        connector-opacity         (reanimated/use-shared-value 0)
        width                     (reanimated/use-shared-value 24)
        height                    (reanimated/use-shared-value 12)
        border-radius-first-half  (reanimated/use-shared-value 8)
        border-radius-second-half (reanimated/use-shared-value 8)
        start-x-y-animation       (fn []
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
                                                                       63.33))
        reset-x-y-animation       (fn []
                                    (helpers/animate-linear translate-x-y 0 100)
                                    (helpers/set-value connector-opacity 0)
                                    (helpers/set-value width 24)
                                    (helpers/set-value height 12)
                                    (helpers/set-value border-radius-first-half 8)
                                    (helpers/set-value border-radius-second-half 16))
        fade-in-animation         (fn []
                                    (helpers/animate-linear translate-x-y 0 220)
                                    (helpers/animate-linear opacity 1 220))
        fade-out-animation        (fn []
                                    (helpers/animate-linear translate-x-y 20 200)
                                    (helpers/animate-linear opacity 0 200)
                                    (helpers/set-value connector-opacity 0)
                                    (helpers/set-value width 24)
                                    (helpers/set-value height 12)
                                    (helpers/set-value border-radius-first-half 8)
                                    (helpers/set-value border-radius-second-half 16))]
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
     [reanimated/view {:style (style/lock-button-container opacity)}
      [reanimated/view
       {:style (style/lock-button-connector connector-opacity
                                            width
                                            height
                                            border-radius-first-half
                                            border-radius-second-half)}]]
     [reanimated/view
      {:style          (style/lock-button translate-x-y opacity)
       :pointer-events :none}
      [icons/icon (if @ready-to-lock? :i/locked :i/unlocked)
       {:color (colors/theme-colors colors/black colors/white)
        :size  20}]]]))
