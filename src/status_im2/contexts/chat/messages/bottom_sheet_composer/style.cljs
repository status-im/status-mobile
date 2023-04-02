(ns status-im2.contexts.chat.messages.bottom-sheet-composer.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [react-native.platform :as platform]
            [react-native.reanimated :as reanimated]))

(def handle-container-height 20)

(def actions-container-height 56)

(defn container
  [insets android-blur? focused? text? images?]
  (let [focusing?  (if platform/ios? focused? (not android-blur?))]
  (merge
    {:border-top-left-radius  20
     ;:background-color :black
     :border-top-right-radius 20
     :padding-horizontal      20
     :position                :absolute
     ;:height 400
     :bottom                  0
     :left                    0
     :right                   0
     :background-color        (colors/theme-colors colors/white colors/neutral-90)
     ;:background-color        (colors/theme-colors colors/white colors/neutral-90)
     ;:background-color        "rgba(255,255,255,1)"
     ;:background-color        :black
     :opacity                 (if (or focusing? text? images?) 1 (if platform/ios? 0.7 0.5))
     :z-index                 3
     :padding-bottom (:bottom insets)
     ;:padding-bottom          0
     }
    (if platform/ios?
      {:shadow-radius  20
       :shadow-opacity 0.1
       :shadow-color   "#09101C"
       :shadow-offset  {:width 0 :height -4}}
      {:elevation 10}))))

(defn handle-container
  []
  {
   :height           handle-container-height
   :left             0
   :right            0
   :top              0
   ;:background-color :red
   :z-index          1
   :justify-content  :center
   :align-items      :center})

(defn handle
  []
  {:width            32
   :height           4
   :border-radius    100
   :background-color (colors/theme-colors colors/neutral-100-opa-5 colors/white-opa-10)})

(defn input
  [keyboard-shown focused? expanded? saved-keyboard-height]
  (merge typography/paragraph-1
         {:min-height          (if platform/ios? 32 44)
          :text-align-vertical :top
          :flex                1
          ;:position (if (and platform/android? (or (<= (reanimated/get-shared-value value) input-height) animating))  :absolute :relative)
          ;:position   (if platform/android? :absolute :relative)

          :position            (if saved-keyboard-height :relative :absolute)
          :top                 0
          :left                0
          ;:right               (when (or (and focused? (not keyboard-shown)) expanded? platform/ios?) 0) ; to inc gesture detection area on Android
          :right               (when (or expanded? platform/ios?) 0) ; to inc gesture detection area on Android
          ;:overflow :hidden
          ;:padding-vertical 10
          ;:min-height input-height
          ;:min-height 40
          ;:min-height input-height
          ;:height 300
          ;:background-color    :green
          }))

(defn input-container
  [height max-height emojis-open?]
  (reanimated/apply-animations-to-style
    {
     ;:transform [{:translate-y value}]
     :height height
     }
    {
     :max-height       max-height
     ;:background-color :blue
     ;:overflow         (if emojis-open? :visible :hidden)
     :overflow         :hidden
     ;:overflow :hidden
     ;:padding-vertical 5
     ;:justify-content :flex-end
     ;:justify-content :center
     ;:align-items :center
     }))


(defn actions-container
  []
  {:height          actions-container-height
   ;:background-color :white
   ;:background-color :w
   ;:background-color "rgba(255,255,255,0.5)"
   ;:background-color (when (or (> (reanimated/get-shared-value value) input-height) platform/android?) :white)
   ;:background-color (when (> (reanimated/get-shared-value value) input-height) :white)
   :justify-content :space-between
   :align-items     :center
   :z-index         2

   :flex-direction  :row

   ;:background-color :yellow
   })

(defn background
  [opacity bg-bottom window-height height]
  (reanimated/apply-animations-to-style
    {:opacity   opacity
     :transform [{:translate-y bg-bottom}]}
    {:position         :absolute
     :left             0
     :right            0
     :bottom           0
     :height           window-height
     :background-color colors/neutral-95-opa-70
     :z-index          1}))

(defn blur-container
  [opacity height]
  (reanimated/apply-animations-to-style
    {:opacity opacity
     :height  height}
    {:position                :absolute
     :elevation               10
     :left                    0
     :right                   0
     :bottom                  0
     :border-top-right-radius 20
     :border-top-left-radius  20
     :overflow                :hidden}))

(defn text-overlay
  []
  {:height   (if platform/ios? (:line-height typography/paragraph-1) 32)
   :position :absolute
   :bottom   0
   :left     0
   :right    0})

(defn text-top-overlay
  [opacity z-index]
  (reanimated/apply-animations-to-style
    {:opacity opacity}
    {:height   80 ;; add height const
     :position :absolute
     :z-index  z-index
     :top      0
     :left     0
     :right    0}))



(defn record-audio-container
  [insets]
  {:align-items      :center
   :background-color :transparent
   :flex-direction   :row
   ;:position         :absolute
   ;:left             0
   ;:right            0
   ;:bottom           (- (:bottom insets) 7)
   })



