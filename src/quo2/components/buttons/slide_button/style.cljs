(ns quo2.components.buttons.slide-button.style
  (:require
    [quo2.components.buttons.slide-button.constants :as constants]
    [quo2.components.buttons.slide-button.utils :as utils]
    [react-native.reanimated :as reanimated]
    [quo2.foundations.typography :as typography]))

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(defn thumb-container
  [{:keys [interpolate-track thumb-size customization-color theme]}]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (interpolate-track :track-clamp)}]}
   {:background-color (utils/slider-color :main customization-color theme)
    :border-radius    12
    :height           thumb-size
    :width            thumb-size
    :align-items      :center
    :overflow         :hidden
    :justify-content  :center}))

(defn arrow-icon-container
  [interpolate-track]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (interpolate-track :arrow-icon-position)}]}
   {:flex            1
    :align-items     :center
    :justify-content :center}))

(defn action-icon
  [interpolate-track size]
  (reanimated/apply-animations-to-style
   {:transform [{:translate-x (interpolate-track :action-icon-position)}]}
   {:height          size
    :width           size
    :position        :absolute
    :align-items     :center
    :left            0
    :top             0
    :flex-direction  :row
    :justify-content :space-around}))

(defn track
  [{:keys [disabled? customization-color height theme]}]
  {:align-items      :flex-start
   :justify-content  :center
   :border-radius    14
   :height           height
   :align-self       :stretch
   :padding          constants/track-padding
   :opacity          (if disabled? 0.3 1)
   :background-color (utils/slider-color :track customization-color theme)})

(defn track-cover
  [interpolate-track]
  (reanimated/apply-animations-to-style
   {:left (interpolate-track :track-cover)}
   (assoc absolute-fill :overflow :hidden)))

(defn track-cover-text-container
  [track-width]
  {:position        :absolute
   :right           0
   :top             0
   :bottom          0
   :align-items     :center
   :justify-content :center
   :flex-direction  :row
   :width           track-width})

(defn track-text
  [customization-color theme]
  (-> typography/paragraph-1
      (merge typography/font-medium)
      (assoc :color (utils/slider-color :main customization-color theme))))
