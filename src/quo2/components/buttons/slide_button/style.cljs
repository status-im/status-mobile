(ns quo2.components.buttons.slide-button.style
  (:require
   [quo2.foundations.colors :as colors]
   [quo2.components.buttons.slide-button.consts :as consts]
   [quo2.components.buttons.slide-button.animations :as anim]
   [react-native.reanimated :as reanimated]
   [quo2.foundations.typography :as typography]))

(def slide-colors
  {:thumb (colors/custom-color-by-theme :blue 50 60)
   :text (colors/custom-color-by-theme :blue 50 60)
   :text-transparent colors/white-opa-40
   :track (colors/custom-color :blue 50 10)})

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn thumb
  [{:keys [x-pos thumb-border-radius]} size track-width]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (anim/clamp-track x-pos track-width size)}]
    :border-radius thumb-border-radius}
   {:width  size
    :height size
    :align-items :center
    :justify-content :center
    :z-index 4
    :background-color (:thumb slide-colors)}))

(defn track-container
  [{:keys [track-container-padding]} height]
  (reanimated/apply-animations-to-style
   {:padding-horizontal track-container-padding}
   {:align-self       :stretch
    :align-items      :center
    :justify-content  :center
    :height height}))

(defn track
  [{:keys [track-border-radius track-scale]} disabled?]
  (reanimated/apply-animations-to-style
   {:border-radius    track-border-radius
    :transform [{:scale track-scale}]}
   {:align-items      :flex-start
    :justify-content  :center
    :align-self       :stretch
    :padding          consts/track-padding
    :opacity          (if disabled? 0.3 1)
    :background-color (:track slide-colors)}))

(defn track-cover [{:keys [x-pos]} track-width thumb-size]
  (reanimated/apply-animations-to-style
   {:left (anim/interpolate-track-cover x-pos track-width thumb-size)}
   (merge
    {:z-index 3
     :overflow :hidden} absolute-fill)))

(defn track-cover-text-container
  [track-width] {:position :absolute
                 :right 0
                 :top 0
                 :bottom 0
                 :align-items :center
                 :justify-content :center
                 :flex-direction :row
                 :width @track-width})

(def track-text
  (merge {:color (:text slide-colors)}
         typography/paragraph-1
         typography/font-medium))


