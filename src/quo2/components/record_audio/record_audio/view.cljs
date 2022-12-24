(ns quo2.components.record-audio.record-audio.view
  (:require
   [cljs-bean.core :as bean]
   [oops.core :as oops]
   [quo.react :refer [effect! memo]]
   [quo2.components.buttons.button :as button]
   [quo2.components.icon :as icons]
   [quo2.components.record-audio.record-audio.style :as style]
   [quo2.foundations.colors :as colors]
   [react-native.core :as rn]
   [react-native.reanimated :as reanimated]
   [reagent.core :as reagent]
   [status-im.utils.utils :as utils]))

(def ^:private scale-to-each 1.8)
(def ^:private scale-to-total 2.6)
(def ^:private scale-padding 0.16)
(def ^:private opacity-from-lock 1)
(def ^:private opacity-from-default 0.5)
(def ^:private signal-anim-duration 3900)
(def ^:private signal-anim-duration-2 1950)

(def ^:private animated-ring
  (reagent/adapt-react-class
   (memo
    (fn [props]
      (let [{:keys [scale opacity color]} (bean/bean props)]
        (reagent/as-element
         [reanimated/view {:style (style/animated-circle scale opacity color)}]))))))

(def ^:private record-button-area-big
  {:width  56
   :height 56
   :x      64
   :y      64})

(def ^:private record-button-area
  {:width  48
   :height 48
   :x      68
   :y      68})

(defn- delete-button-area
  [active?]
  {:width  (if active? 72 82)
   :height 56
   :x      (if active? -16 -32)
   :y      (if active? 64 70)})

(defn- lock-button-area
  [active?]
  {:width  (if active? 72 100)
   :height (if active? 72 102)
   :x      -32
   :y      -32})

(defn- send-button-area
  [active?]
  {:width  56
   :height (if active? 72 92)
   :x      68
   :y      (if active? -16 -32)})

(defn- touch-inside-area?
  [{:keys [location-x location-y ignore-min-y? ignore-max-y? ignore-min-x? ignore-max-x?]}
   {:keys [width height x y]}]
  (let [max-x (+ x width)
        max-y (+ y height)]
    (and
     (and
      (or ignore-min-x? (>= location-x x))
      (or ignore-max-x? (<= location-x max-x)))
     (and
      (or ignore-min-y? (>= location-y y))
      (or ignore-max-y? (<= location-y max-y))))))

(def record-audio-worklets (js/require "../src/js/record_audio_worklets.js"))

(defn- ring-scale
  [scale substract]
  (.ringScale ^js record-audio-worklets
              scale
              substract))

(defn- record-button-big
  [recording? ready-to-send? ready-to-lock? ready-to-delete? record-button-is-animating?
   record-button-at-initial-position? locked? reviewing-audio? recording-timer recording-length-ms
   clear-timeout touch-active?]
  [:f>
   (fn []
     (let [scale                (reanimated/use-shared-value 1)
           opacity              (reanimated/use-shared-value 0)
           opacity-from         (if @ready-to-lock? opacity-from-lock opacity-from-default)
           animations           (map
                                 (fn [index]
                                   (let [ring-scale (ring-scale scale (* scale-padding index))]
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
                                    (do
                                      (reset! recording? false)
                                      (reset! ready-to-send? false)
                                      (reset! ready-to-delete? false)
                                      (reset! ready-to-lock? false)
                                      (utils/clear-interval @recording-timer)
                                      (reset! recording-length-ms 0))))
           start-animation      (fn []
                                  (reanimated/set-shared-value opacity 1)
                                  (reanimated/animate-shared-value-with-timing scale
                                                                               2.6
                                                                               signal-anim-duration
                                                                               :linear)
                                  ;; TODO: Research if we can implement this with withSequence method
                                  ;; from Reanimated 2
                                  ;; GitHub issue [#14561]:
                                  ;; https://github.com/status-im/status-mobile/issues/14561
                                  (reset! clear-timeout
                                    (utils/set-timeout
                                     #(do (reanimated/set-shared-value scale scale-to-each)
                                          (reanimated/animate-shared-value-with-delay-repeat
                                           scale
                                           scale-to-total
                                           signal-anim-duration-2
                                           :linear
                                           0
                                           -1))
                                     signal-anim-duration)))
           stop-animation       (fn []
                                  (reanimated/set-shared-value opacity 0)
                                  (reanimated/cancel-animation scale)
                                  (reanimated/set-shared-value scale 1)
                                  (when @clear-timeout (utils/clear-timeout @clear-timeout)))
           start-y-animation    (fn []
                                  (reset! record-button-at-initial-position? false)
                                  (reset! record-button-is-animating? true)
                                  (reanimated/animate-shared-value-with-timing translate-y
                                                                               -64
                                                                               250
                                                                               :easing1)
                                  (reanimated/animate-shared-value-with-delay icon-opacity
                                                                              0       33.33
                                                                              :linear 76.66)
                                  (utils/set-timeout (fn []
                                                       (reset! record-button-is-animating? false)
                                                       (when-not @touch-active? (complete-animation)))
                                                     250))
           reset-y-animation    (fn []
                                  (reanimated/animate-shared-value-with-timing translate-y
                                                                               0
                                                                               300
                                                                               :easing1)
                                  (reanimated/animate-shared-value-with-timing icon-opacity
                                                                               1
                                                                               500
                                                                               :linear)
                                  (utils/set-timeout (fn []
                                                       (reset! record-button-at-initial-position? true))
                                                     500))
           start-x-animation    (fn []
                                  (reset! record-button-at-initial-position? false)
                                  (reset! record-button-is-animating? true)
                                  (reanimated/animate-shared-value-with-timing translate-x
                                                                               -64
                                                                               250
                                                                               :easing1)
                                  (reanimated/animate-shared-value-with-delay icon-opacity
                                                                              0       33.33
                                                                              :linear 76.66)
                                  (reanimated/animate-shared-value-with-timing red-overlay-opacity
                                                                               1
                                                                               33.33
                                                                               :linear)
                                  (utils/set-timeout (fn []
                                                       (reset! record-button-is-animating? false)
                                                       (when-not @touch-active? (complete-animation)))
                                                     250))
           reset-x-animation    (fn []
                                  (reanimated/animate-shared-value-with-timing translate-x
                                                                               0
                                                                               300
                                                                               :easing1)
                                  (reanimated/animate-shared-value-with-timing icon-opacity
                                                                               1
                                                                               500
                                                                               :linear)
                                  (reanimated/animate-shared-value-with-timing red-overlay-opacity
                                                                               0
                                                                               100
                                                                               :linear)
                                  (utils/set-timeout (fn []
                                                       (reset! record-button-at-initial-position? true))
                                                     500))
           start-x-y-animation  (fn []
                                  (reset! record-button-at-initial-position? false)
                                  (reset! record-button-is-animating? true)
                                  (reanimated/animate-shared-value-with-timing translate-y
                                                                               -44
                                                                               200
                                                                               :easing1)
                                  (reanimated/animate-shared-value-with-timing translate-x
                                                                               -44
                                                                               200
                                                                               :easing1)
                                  (reanimated/animate-shared-value-with-delay icon-opacity
                                                                              0       33.33
                                                                              :linear 33.33)
                                  (reanimated/animate-shared-value-with-timing gray-overlay-opacity
                                                                               1
                                                                               33.33
                                                                               :linear)
                                  (utils/set-timeout (fn []
                                                       (reset! record-button-is-animating? false)
                                                       (when-not @touch-active? (complete-animation)))
                                                     200))
           reset-x-y-animation  (fn []
                                  (reanimated/animate-shared-value-with-timing translate-y
                                                                               0
                                                                               300
                                                                               :easing1)
                                  (reanimated/animate-shared-value-with-timing translate-x
                                                                               0
                                                                               300
                                                                               :easing1)
                                  (reanimated/animate-shared-value-with-timing icon-opacity
                                                                               1
                                                                               500
                                                                               :linear)
                                  (reanimated/animate-shared-value-with-timing gray-overlay-opacity
                                                                               0
                                                                               800
                                                                               :linear)
                                  (utils/set-timeout (fn []
                                                       (reset! record-button-at-initial-position? true))
                                                     800))]
       (effect! #(cond
                   @recording?
                   (start-animation)
                   (not @ready-to-lock?)
                   (stop-animation))
                [@recording?])
       (effect! #(if @ready-to-lock?
                   (start-x-y-animation)
                   (reset-x-y-animation))
                [@ready-to-lock?])
       (effect! #(if @ready-to-send?
                   (start-y-animation)
                   (reset-y-animation))
                [@ready-to-send?])
       (effect! #(if @ready-to-delete?
                   (start-x-animation)
                   (reset-x-animation))
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

(defn- send-button
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
                                       (reanimated/animate-shared-value-with-delay translate-y
                                                                                   12      50
                                                                                   :linear 133.33)
                                       (reanimated/animate-shared-value-with-delay connector-opacity
                                                                                   1        0
                                                                                   :easing1 93.33)
                                       (reanimated/animate-shared-value-with-delay width
                                                                                   56       83.33
                                                                                   :easing1 80)
                                       (reanimated/animate-shared-value-with-delay height
                                                                                   56       83.33
                                                                                   :easing1 80)
                                       (reanimated/animate-shared-value-with-delay
                                        border-radius-first-half
                                        28       83.33
                                        :easing1 80)
                                       (reanimated/animate-shared-value-with-delay
                                        border-radius-second-half
                                        28       83.33
                                        :easing1 80))
           reset-y-animation         (fn []
                                       (reanimated/animate-shared-value-with-timing translate-y
                                                                                    0
                                                                                    100
                                                                                    :linear)
                                       (reanimated/set-shared-value connector-opacity 0)
                                       (reanimated/set-shared-value width 12)
                                       (reanimated/set-shared-value height 24)
                                       (reanimated/set-shared-value border-radius-first-half 16)
                                       (reanimated/set-shared-value border-radius-second-half 8))
           fade-in-animation         (fn []
                                       (reanimated/animate-shared-value-with-timing translate-y
                                                                                    0
                                                                                    200
                                                                                    :linear)
                                       (reanimated/animate-shared-value-with-timing opacity
                                                                                    1
                                                                                    200
                                                                                    :linear))
           fade-out-animation        (fn []
                                       (reanimated/animate-shared-value-with-timing translate-y
                                                                                    (if @reviewing-audio?
                                                                                      76
                                                                                      20)
                                                                                    200
                                                                                    :linear)
                                       (when-not @reviewing-audio?
                                         (reanimated/animate-shared-value-with-timing opacity
                                                                                      0
                                                                                      200
                                                                                      :linear))
                                       (reanimated/set-shared-value connector-opacity 0)
                                       (reanimated/set-shared-value width 24)
                                       (reanimated/set-shared-value height 12)
                                       (reanimated/set-shared-value border-radius-first-half 8)
                                       (reanimated/set-shared-value border-radius-second-half 16))
           fade-out-reset-animation  (fn []
                                       (reanimated/animate-shared-value-with-timing opacity
                                                                                    0
                                                                                    200
                                                                                    :linear)
                                       (reanimated/animate-shared-value-with-delay translate-y
                                                                                   20      0
                                                                                   :linear 200)
                                       (reanimated/set-shared-value connector-opacity 0)
                                       (reanimated/set-shared-value width 24)
                                       (reanimated/set-shared-value height 12)
                                       (reanimated/set-shared-value border-radius-first-half 8)
                                       (reanimated/set-shared-value border-radius-second-half 16))]
       (effect! #(if @recording?
                   (fade-in-animation)
                   (fade-out-animation))
                [@recording?])
       (effect! #(when-not @reviewing-audio?
                   (fade-out-reset-animation))
                [@reviewing-audio?])
       (effect! #(cond
                   @ready-to-send?
                   (start-y-animation)
                   @recording?     (reset-y-animation))
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

(defn- lock-button
  [recording? ready-to-lock? locked?]
  [:f>
   (fn []
     (let [translate-x-y             (reanimated/use-shared-value 20)
           opacity                   (reanimated/use-shared-value 0)
           connector-opacity         (reanimated/use-shared-value 0)
           width                     (reanimated/use-shared-value 24)
           height                    (reanimated/use-shared-value 12)
           border-radius-first-half  (reanimated/use-shared-value 8)
           border-radius-second-half (reanimated/use-shared-value 8)
           start-x-y-animation       (fn []
                                       (reanimated/animate-shared-value-with-delay translate-x-y
                                                                                   8       50
                                                                                   :linear 116.66)
                                       (reanimated/animate-shared-value-with-delay connector-opacity
                                                                                   1        0
                                                                                   :easing1 80)
                                       (reanimated/animate-shared-value-with-delay width
                                                                                   56       83.33
                                                                                   :easing1 63.33)
                                       (reanimated/animate-shared-value-with-delay height
                                                                                   56       83.33
                                                                                   :easing1 63.33)
                                       (reanimated/animate-shared-value-with-delay
                                        border-radius-first-half
                                        28       83.33
                                        :easing1 63.33)
                                       (reanimated/animate-shared-value-with-delay
                                        border-radius-second-half
                                        28       83.33
                                        :easing1 63.33))
           reset-x-y-animation       (fn []
                                       (reanimated/animate-shared-value-with-timing translate-x-y
                                                                                    0
                                                                                    100
                                                                                    :linear)
                                       (reanimated/set-shared-value connector-opacity 0)
                                       (reanimated/set-shared-value width 24)
                                       (reanimated/set-shared-value height 12)
                                       (reanimated/set-shared-value border-radius-first-half 8)
                                       (reanimated/set-shared-value border-radius-second-half 16))
           fade-in-animation         (fn []
                                       (reanimated/animate-shared-value-with-timing translate-x-y
                                                                                    0
                                                                                    220
                                                                                    :linear)
                                       (reanimated/animate-shared-value-with-timing opacity
                                                                                    1
                                                                                    220
                                                                                    :linear))
           fade-out-animation        (fn []
                                       (reanimated/animate-shared-value-with-timing translate-x-y
                                                                                    20
                                                                                    200
                                                                                    :linear)
                                       (reanimated/animate-shared-value-with-timing opacity
                                                                                    0
                                                                                    200
                                                                                    :linear)
                                       (reanimated/set-shared-value connector-opacity 0)
                                       (reanimated/set-shared-value width 24)
                                       (reanimated/set-shared-value height 12)
                                       (reanimated/set-shared-value border-radius-first-half 8)
                                       (reanimated/set-shared-value border-radius-second-half 16))]
       (effect! #(if @recording?
                   (fade-in-animation)
                   (fade-out-animation))
                [@recording?])
       (effect! #(cond
                   @ready-to-lock?
                   (start-x-y-animation)
                   (and @recording? (not @locked?))
                   (reset-x-y-animation))
                [@ready-to-lock?])
       (effect! #(if @locked?
                   (fade-out-animation)
                   (reset-x-y-animation))
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
           :size  20}]]]))])

(defn- delete-button
  [recording? ready-to-delete?]
  [:f>
   (fn []
     (let [opacity                   (reanimated/use-shared-value 0)
           translate-x               (reanimated/use-shared-value 20)
           connector-opacity         (reanimated/use-shared-value 0)
           width                     (reanimated/use-shared-value 24)
           height                    (reanimated/use-shared-value 12)
           border-radius-first-half  (reanimated/use-shared-value 8)
           border-radius-second-half (reanimated/use-shared-value 8)
           start-x-animation         (fn []
                                       (reanimated/animate-shared-value-with-delay translate-x
                                                                                   12      50
                                                                                   :linear 133.33)
                                       (reanimated/animate-shared-value-with-delay connector-opacity
                                                                                   1        0
                                                                                   :easing1 93.33)
                                       (reanimated/animate-shared-value-with-delay width
                                                                                   56       83.33
                                                                                   :easing1 80)
                                       (reanimated/animate-shared-value-with-delay height
                                                                                   56       83.33
                                                                                   :easing1 80)
                                       (reanimated/animate-shared-value-with-delay
                                        border-radius-first-half
                                        28       83.33
                                        :easing1 80)
                                       (reanimated/animate-shared-value-with-delay
                                        border-radius-second-half
                                        28       83.33
                                        :easing1 80))
           reset-x-animation         (fn []
                                       (reanimated/animate-shared-value-with-timing translate-x
                                                                                    0
                                                                                    100
                                                                                    :linear)
                                       (reanimated/set-shared-value connector-opacity 0)
                                       (reanimated/set-shared-value width 24)
                                       (reanimated/set-shared-value height 12)
                                       (reanimated/set-shared-value border-radius-first-half 8)
                                       (reanimated/set-shared-value border-radius-second-half 16))
           fade-in-animation         (fn []
                                       (reanimated/animate-shared-value-with-timing translate-x
                                                                                    0
                                                                                    200
                                                                                    :linear)
                                       (reanimated/animate-shared-value-with-timing opacity
                                                                                    1
                                                                                    200
                                                                                    :linear))
           fade-out-animation        (fn []
                                       (reanimated/animate-shared-value-with-timing translate-x
                                                                                    20
                                                                                    200
                                                                                    :linear)
                                       (reanimated/animate-shared-value-with-timing opacity
                                                                                    0
                                                                                    200
                                                                                    :linear)
                                       (reanimated/set-shared-value connector-opacity 0)
                                       (reanimated/set-shared-value width 24)
                                       (reanimated/set-shared-value height 12)
                                       (reanimated/set-shared-value border-radius-first-half 8)
                                       (reanimated/set-shared-value border-radius-second-half 16))]
       (effect! #(if @recording?
                   (fade-in-animation)
                   (fade-out-animation))
                [@recording?])
       (effect! #(cond
                   @ready-to-delete?
                   (start-x-animation)
                   @recording?
                   (reset-x-animation))
                [@ready-to-delete?])
       [:<>
        [reanimated/view {:style (style/delete-button-container opacity)}
         [reanimated/view
          {:style (style/delete-button-connector connector-opacity
                                                 width
                                                 height
                                                 border-radius-first-half
                                                 border-radius-second-half)}]]
        [reanimated/view
         {:style          (style/delete-button translate-x opacity)
          :pointer-events :none}
         [icons/icon :i/delete
          {:color colors/white
           :size  20}]]]))])

(defn- record-button
  [recording? reviewing-audio?]
  [:f>
   (fn []
     (let [opacity        (reanimated/use-shared-value 1)
           show-animation #(reanimated/set-shared-value opacity 1)
           hide-animation #(reanimated/set-shared-value opacity 0)]
       (effect! #(if (or @recording? @reviewing-audio?)
                   (hide-animation)
                   (show-animation))
                [@recording? @reviewing-audio?])
       [reanimated/view {:style (style/record-button-container opacity)}
        [button/button
         {:type                :outline
          :size                32
          :width               32
          :accessibility-label :mic-button}
         [icons/icon :i/audio {:color (colors/theme-colors colors/neutral-100 colors/white)}]]]))])

(defn input-view
  []
  [:f>
   (fn []
     (let [recording?                         (reagent/atom false)
           locked?                            (reagent/atom false)
           ready-to-send?                     (reagent/atom false)
           ready-to-lock?                     (reagent/atom false)
           ready-to-delete?                   (reagent/atom false)
           reviewing-audio?                   (reagent/atom false)
           clear-timeout                      (atom nil)
           record-button-at-initial-position? (atom true)
           record-button-is-animating?        (atom false)
           touch-active?                      (atom false)
           recording-timer                    (atom nil)
           recording-length-ms                (atom 0)
           on-start-should-set-responder
           (fn [^js e]
             (when-not @locked?
               (let [pressed-record-button? (touch-inside-area?
                                             {:location-x    (oops/oget e "nativeEvent.locationX")
                                              :location-y    (oops/oget e "nativeEvent.locationY")
                                              :ignore-min-y? false
                                              :ignore-max-y? false
                                              :ignore-min-x? false
                                              :ignore-max-x? false}
                                             record-button-area)]
                 (when-not @reviewing-audio?
                   (reset! recording? pressed-record-button?)
                   (when pressed-record-button?
                     ;; TODO: By now we just track recording length, we need to add actual audio
                     ;; recording logic
                     ;; GitHub issue [#14558]: https://github.com/status-im/status-mobile/issues/14558
                     (reset! recording-timer (utils/set-interval #(reset! recording-length-ms
                                                                    (+ @recording-length-ms 500))
                                                                 500))))
                 (reset! touch-active? true)))
             true)
           on-responder-move
           (fn [^js e]
             (when-not @locked?
               (let [location-x              (oops/oget e "nativeEvent.locationX")
                     location-y              (oops/oget e "nativeEvent.locationY")
                     page-x                  (oops/oget e "nativeEvent.pageX")
                     page-y                  (oops/oget e "nativeEvent.pageY")
                     moved-to-send-button?   (touch-inside-area?
                                              {:location-x    location-x
                                               :location-y    location-y
                                               :ignore-min-y? true
                                               :ignore-max-y? false
                                               :ignore-min-x? false
                                               :ignore-max-x? true}
                                              (send-button-area @ready-to-send?))
                     moved-to-delete-button? (touch-inside-area?
                                              {:location-x    location-x
                                               :location-y    location-y
                                               :ignore-min-y? false
                                               :ignore-max-y? true
                                               :ignore-min-x? true
                                               :ignore-max-x? false}
                                              (delete-button-area @ready-to-delete?))
                     moved-to-lock-button?   (touch-inside-area?
                                              {:location-x    location-x
                                               :location-y    location-y
                                               :ignore-min-y? false
                                               :ignore-max-y? false
                                               :ignore-min-x? false
                                               :ignore-max-x? false}
                                              (lock-button-area @ready-to-lock?))
                     moved-to-record-button? (and
                                              (touch-inside-area?
                                               {:location-x    location-x
                                                :location-y    location-y
                                                :ignore-min-y? false
                                                :ignore-max-y? false
                                                :ignore-min-x? false
                                                :ignore-max-x? false}
                                               record-button-area-big)
                                              (not= location-x page-x)
                                              (not= location-y page-y))]
                 (cond
                   (and
                    (or
                     (and moved-to-record-button? @ready-to-lock?)
                     (and (not @locked?) moved-to-lock-button? @record-button-at-initial-position?))
                    (not @ready-to-delete?)
                    (not @ready-to-send?)
                    @recording?)
                   (reset! ready-to-lock? moved-to-lock-button?)
                   (and
                    (or
                     (and moved-to-record-button? @ready-to-delete?)
                     (and moved-to-delete-button? @record-button-at-initial-position?))
                    (not @ready-to-lock?)
                    (not @ready-to-send?)
                    @recording?)
                   (reset! ready-to-delete? moved-to-delete-button?)
                   (and
                    (or
                     (and moved-to-record-button? @ready-to-send?)
                     (and moved-to-send-button? @record-button-at-initial-position?))
                    (not @ready-to-lock?)
                    (not @ready-to-delete?)
                    @recording?)
                   (reset! ready-to-send? moved-to-send-button?)))))
           on-responder-release
           (fn [^js e]
             (let [on-record-button? (touch-inside-area?
                                      {:location-x    (oops/oget e "nativeEvent.locationX")
                                       :location-y    (oops/oget e "nativeEvent.locationY")
                                       :ignore-min-y? false
                                       :ignore-max-y? false
                                       :ignore-min-x? false
                                       :ignore-max-x? false}
                                      (if @reviewing-audio? record-button-area record-button-area-big))]
               (cond
                 (and @reviewing-audio? on-record-button?)
                 (reset! reviewing-audio? false)
                 (and @ready-to-lock? (not @record-button-is-animating?))
                 (do
                   (reset! locked? true)
                   (reset! ready-to-lock? false))
                 (and (not @reviewing-audio?) on-record-button?)
                 (do
                   (when (>= @recording-length-ms 500) (reset! reviewing-audio? true))
                   (reset! locked? false)
                   (reset! recording? false)
                   (reset! ready-to-lock? false)
                   (utils/clear-interval @recording-timer)
                   (reset! recording-length-ms 0))
                 (and (not @locked?) (not @reviewing-audio?) (not @record-button-is-animating?))
                 (do
                   (reset! recording? false)
                   (reset! ready-to-send? false)
                   (reset! ready-to-delete? false)
                   (reset! ready-to-lock? false)
                   (utils/clear-interval @recording-timer)
                   (reset! recording-length-ms 0))))
             (reset! touch-active? false))]
       (fn []
         [rn/view
          {:style                         style/input-container
           :pointer-events                :box-only
           :on-start-should-set-responder on-start-should-set-responder
           :on-responder-move             on-responder-move
           :on-responder-release          on-responder-release}
          [delete-button recording? ready-to-delete?]
          [lock-button recording? ready-to-lock? locked?]
          [send-button recording? ready-to-send? reviewing-audio?]
          [record-button-big
           recording?
           ready-to-send?
           ready-to-lock?
           ready-to-delete?
           record-button-is-animating?
           record-button-at-initial-position?
           locked?
           reviewing-audio?
           recording-timer
           recording-length-ms
           clear-timeout
           touch-active?]
          [record-button recording? reviewing-audio?]])))])
