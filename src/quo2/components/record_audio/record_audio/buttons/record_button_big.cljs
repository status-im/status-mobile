(ns quo2.components.record-audio.record-audio.buttons.record-button-big
  (:require [quo2.components.icon :as icons]
            [quo2.components.record-audio.record-audio.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn :refer [use-effect]]
            [react-native.reanimated :as reanimated]
            [react-native.audio-toolkit :as audio]
            [taoensso.timbre :as log]
            [cljs-bean.core :as bean]
            [reagent.core :as reagent]
            [quo2.components.record-audio.record-audio.helpers :refer
             [animate-linear
              animate-linear-with-delay
              animate-linear-with-delay-loop
              animate-easing
              set-value]]
            worklets.reacord-audio))

(def ^:private scale-to-each 1.8)
(def ^:private scale-to-total 2.6)
(def ^:private scale-padding 0.16)
(def ^:private opacity-from-lock 1)
(def ^:private opacity-from-default 0.5)
(def ^:private signal-anim-duration 3900)
(def ^:private signal-anim-duration-2 1950)

(def ^:private animated-ring
  (reagent/adapt-react-class
   (rn/memo
    (fn [props]
      (let [{:keys [scale opacity color]} (bean/bean props)]
        (reagent/as-element
         [reanimated/view {:style (style/animated-circle scale opacity color)}]))))))

(defn record-button-big
  [recording? ready-to-send? ready-to-lock? ready-to-delete? record-button-is-animating?
   record-button-at-initial-position? locked? reviewing-audio? recording-timer recording-length-ms
   clear-timeout touch-active? recorder-ref reload-recorder-fn idle? on-send on-cancel]
  [:f>
   (fn []
     (let [scale                (reanimated/use-shared-value 1)
           opacity              (reanimated/use-shared-value 0)
           opacity-from         (if @ready-to-lock? opacity-from-lock opacity-from-default)
           animations           (map
                                 (fn [index]
                                   (let [ring-scale (worklets.reacord-audio/ring-scale scale
                                                                                       (* scale-padding
                                                                                          index))]
                                     {:scale   ring-scale
                                      :opacity (reanimated/interpolate ring-scale
                                                                       [1 scale-to-each]
                                                                       [opacity-from 0])}))
                                 (range 0 5))
           rings-color          (cond
                                  @ready-to-lock?   (colors/theme-colors colors/neutral-80-opa-5-opaque
                                                                         colors/neutral-80)
                                  @ready-to-delete? colors/danger-50
                                  :else             colors/primary-50)
           translate-y          (reanimated/use-shared-value 0)
           translate-x          (reanimated/use-shared-value 0)
           button-color         colors/primary-50
           icon-color           (if (and (not (colors/dark?)) @ready-to-lock?) colors/black colors/white)
           icon-opacity         (reanimated/use-shared-value 1)
           red-overlay-opacity  (reanimated/use-shared-value 0)
           gray-overlay-opacity (reanimated/use-shared-value 0)
           complete-animation   (fn []
                                  (cond
                                    (and @ready-to-lock? (not @record-button-is-animating?))
                                    (do
                                      (reset! locked? true)
                                      (reset! ready-to-lock? false))
                                    (and (not @locked?) (not @reviewing-audio?))
                                    (audio/stop-recording
                                     @recorder-ref
                                     (fn []
                                       (cond
                                         @ready-to-send?
                                         (when on-send
                                           (on-send (audio/get-recorder-file-path @recorder-ref)))
                                         @ready-to-delete?
                                         (when on-cancel
                                           (on-cancel)))
                                       (reload-recorder-fn)
                                       (reset! recording? false)
                                       (reset! ready-to-send? false)
                                       (reset! ready-to-delete? false)
                                       (reset! ready-to-lock? false)
                                       (reset! idle? true)
                                       (js/setTimeout #(reset! idle? false) 1000)
                                       (js/clearInterval @recording-timer)
                                       (reset! recording-length-ms 0)
                                       (log/debug "[record-audio] stop recording - success"))
                                     #(log/error "[record-audio] stop recording - error: " %))))
           start-animation      (fn []
                                  (set-value opacity 1)
                                  (animate-linear scale 2.6 signal-anim-duration)
                                  ;; TODO: Research if we can implement this with withSequence method
                                  ;; from Reanimated 2
                                  ;; GitHub issue [#14561]:
                                  ;; https://github.com/status-im/status-mobile/issues/14561
                                  (reset! clear-timeout
                                    (js/setTimeout
                                     (fn []
                                       (set-value scale scale-to-each)
                                       (animate-linear-with-delay-loop scale
                                                                       scale-to-total
                                                                       signal-anim-duration-2
                                                                       0))
                                     signal-anim-duration)))
           stop-animation       (fn []
                                  (set-value opacity 0)
                                  (reanimated/cancel-animation scale)
                                  (set-value scale 1)
                                  (when @clear-timeout (js/clearTimeout @clear-timeout)))
           start-y-animation    (fn []
                                  (reset! record-button-at-initial-position? false)
                                  (reset! record-button-is-animating? true)
                                  (animate-easing translate-y -64 250)
                                  (animate-linear-with-delay icon-opacity 0 33.33 76.66)
                                  (js/setTimeout (fn []
                                                   (reset! record-button-is-animating? false)
                                                   (when-not @touch-active? (complete-animation)))
                                                 250))
           reset-y-animation    (fn []
                                  (animate-easing translate-y 0 300)
                                  (animate-linear icon-opacity 1 500)
                                  (js/setTimeout (fn []
                                                   (reset! record-button-at-initial-position? true))
                                                 500))
           start-x-animation    (fn []
                                  (reset! record-button-at-initial-position? false)
                                  (reset! record-button-is-animating? true)
                                  (animate-easing translate-x -64 250)
                                  (animate-linear-with-delay icon-opacity 0 33.33 76.66)
                                  (animate-linear red-overlay-opacity 1 33.33)
                                  (js/setTimeout (fn []
                                                   (reset! record-button-is-animating? false)
                                                   (when-not @touch-active? (complete-animation)))
                                                 250))
           reset-x-animation    (fn []
                                  (animate-easing translate-x 0 300)
                                  (animate-linear icon-opacity 1 500)
                                  (animate-linear red-overlay-opacity 0 100)
                                  (js/setTimeout (fn []
                                                   (reset! record-button-at-initial-position? true))
                                                 500))
           start-x-y-animation  (fn []
                                  (reset! record-button-at-initial-position? false)
                                  (reset! record-button-is-animating? true)
                                  (animate-easing translate-y -44 200)
                                  (animate-easing translate-x -44 200)
                                  (animate-linear-with-delay icon-opacity 0 33.33 33.33)
                                  (animate-linear gray-overlay-opacity 1 33.33)
                                  (js/setTimeout (fn []
                                                   (reset! record-button-is-animating? false)
                                                   (when-not @touch-active? (complete-animation)))
                                                 200))
           reset-x-y-animation  (fn []
                                  (animate-easing translate-y 0 300)
                                  (animate-easing translate-x 0 300)
                                  (animate-linear icon-opacity 1 500)
                                  (animate-linear gray-overlay-opacity 0 800)
                                  (js/setTimeout (fn []
                                                   (reset! record-button-at-initial-position? true))
                                                 800))]
       (use-effect (fn []
                     (cond
                       @recording?
                       (start-animation)
                       (not @ready-to-lock?)
                       (stop-animation)))
                   [@recording?])
       (use-effect (fn []
                     (if @ready-to-lock?
                       (start-x-y-animation)
                       (reset-x-y-animation)))
                   [@ready-to-lock?])
       (use-effect (fn []
                     (if @ready-to-send?
                       (start-y-animation)
                       (reset-y-animation)))
                   [@ready-to-send?])
       (use-effect (fn []
                     (if @ready-to-delete?
                       (start-x-animation)
                       (reset-x-animation)))
                   [@ready-to-delete?])
       [reanimated/view
        {:style          (style/record-button-big-container translate-x translate-y opacity)
         :pointer-events :none}
        [:<>
         (map-indexed
          (fn [id animation]
            ^{:key id}
            [animated-ring
             {:scale   (:scale animation)
              :opacity (:opacity animation)
              :color   rings-color}])
          animations)]
        [rn/view {:style (style/record-button-big-body button-color)}
         [reanimated/view {:style (style/record-button-big-red-overlay red-overlay-opacity)}]
         [reanimated/view {:style (style/record-button-big-gray-overlay gray-overlay-opacity)}]
         [reanimated/view {:style (style/record-button-big-icon-container icon-opacity)}
          (if @locked?
            [rn/view {:style style/stop-icon}]
            [icons/icon :i/audio {:color icon-color}])]]]))])
