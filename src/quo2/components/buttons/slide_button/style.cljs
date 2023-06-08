(ns quo2.components.buttons.slide-button.style
  (:require
   [quo2.foundations.colors :as colors]
   [quo2.components.buttons.slide-button.consts
    :refer [track-padding
            thumb-size]]
   [quo2.components.buttons.slide-button.animations
    :refer [clamp-track interpolate-track-cover]]
   [react-native.reanimated :as reanimated]
   [quo2.foundations.typography :as typography]))

(def slide-colors
  {:thumb (colors/custom-color-by-theme :blue 50 60)
   :text (:thumb slide-colors)
   :text-transparent colors/white-opa-40
   :track (colors/custom-color :blue 50 10)})

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn thumb-style
  [{:keys [x-pos]} track-width]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (clamp-track x-pos track-width)}]}
   {:width  thumb-size
    :height thumb-size
    :border-radius 14
    :z-index 4
    :background-color (:thumb slide-colors)}))

(def track-style {:align-self       :stretch
                  :align-items      :flex-start
                  :justify-content  :center
                  :padding          track-padding
                  :height           48
                  :border-radius    12
                  :background-color (:track slide-colors)})

(defn track-cover-style [{:keys [x-pos]} track-width]
  (reanimated/apply-animations-to-style
   {:left (interpolate-track-cover x-pos track-width)}
   (merge
    {:z-index 3
     :overflow :hidden} absolute-fill)))

(defn track-cover-text-container-style
  [track-width] {:position :absolute
                 :right 0
                 :top 0
                 :bottom 0
                 :align-items :center
                 :justify-content :center
                 :width @track-width})

(def track-text-style
  (merge {:color (:text slide-colors)}
         typography/paragraph-1
         typography/font-medium))


