(ns quo2.components.record-audio.record-audio.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defn animated-circle
  [scale opacity color]
  (reanimated/apply-animations-to-style
   {:transform [{:scale scale}]
    :opacity   opacity}
   {:width           56
    :height          56
    :border-width    1
    :border-color    color
    :border-radius   28
    :position        :absolute
    :justify-content :center
    :align-items     :center
    :z-index         0}))

(defn record-button-big-container
  [translate-x translate-y opacity]
  (reanimated/apply-animations-to-style
   {:transform [{:translateY translate-y}
                {:translateX translate-x}]
    :opacity   opacity}
   {:position        :absolute
    :bottom          0
    :right           0
    :width           96
    :height          96
    :align-items     :center
    :justify-content :center
    :z-index         0}))

(defn record-button-big-body
  [button-color]
  {:width            56
   :height           56
   :border-radius    28
   :justify-content  :center
   :align-items      :center
   :background-color button-color
   :overflow         :hidden})

(defn record-button-big-red-overlay
  [red-overlay-opacity]
  (reanimated/apply-animations-to-style
   {:opacity red-overlay-opacity}
   {:position         :absolute
    :top              0
    :left             0
    :right            0
    :bottom           0
    :background-color colors/danger-50}))

(defn record-button-big-gray-overlay
  [gray-overlay-opacity]
  (reanimated/apply-animations-to-style
   {:opacity gray-overlay-opacity}
   {:position         :absolute
    :top              0
    :left             0
    :right            0
    :bottom           0
    :background-color (colors/theme-colors colors/neutral-80-opa-5-opaque colors/neutral-80)}))

(defn record-button-big-icon-container
  [icon-opacity]
  (reanimated/apply-animations-to-style
   {:opacity icon-opacity}
   {}))

(def stop-icon
  {:width            13
   :height           13
   :border-radius    4
   :background-color colors/white})

(defn send-button-container
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:justify-content :center
    :align-items     :center
    :position        :absolute
    :width           56
    :height          56
    :top             0
    :right           20}))

(defn send-button-connector
  [opacity width height border-radius-first-half border-radius-second-half]
  (reanimated/apply-animations-to-style
   {:opacity                    opacity
    :width                      width
    :height                     height
    :border-bottom-left-radius  border-radius-second-half
    :border-top-left-radius     border-radius-first-half
    :border-top-right-radius    border-radius-first-half
    :border-bottom-right-radius border-radius-second-half}
   {:justify-content  :center
    :align-items      :center
    :align-self       :center
    :background-color colors/primary-50
    :z-index          0}))

(defn send-button
  [translate-y opacity]
  (reanimated/apply-animations-to-style
   {:transform [{:translateY translate-y}]
    :opacity   opacity}
   {:justify-content  :center
    :align-items      :center
    :background-color colors/primary-50
    :width            32
    :height           32
    :border-radius    16
    :position         :absolute
    :top              0
    :right            32
    :z-index          10}))

(def send-icon-container
  {:z-index 10})

(defn lock-button-container
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:transform       [{:rotate "45deg"}]
    :justify-content :center
    :align-items     :center
    :position        :absolute
    :width           56
    :height          56
    :top             20
    :left            20}))

(defn lock-button-connector
  [opacity width height border-radius-first-half border-radius-second-half]
  (reanimated/apply-animations-to-style
   {:opacity                    opacity
    :width                      width
    :height                     height
    :border-bottom-left-radius  border-radius-first-half
    :border-top-left-radius     border-radius-first-half
    :border-top-right-radius    border-radius-second-half
    :border-bottom-right-radius border-radius-second-half}
   {:justify-content  :center
    :align-items      :center
    :align-self       :center
    :background-color (colors/theme-colors colors/neutral-80-opa-5-opaque colors/neutral-80)
    :overflow         :hidden}))

(defn lock-button
  [translate-x-y opacity]
  (reanimated/apply-animations-to-style
   {:transform [{:translateX translate-x-y}
                {:translateY translate-x-y}]
    :opacity   opacity}
   {:width            32
    :height           32
    :justify-content  :center
    :align-items      :center
    :background-color (colors/theme-colors colors/neutral-80-opa-5-opaque colors/neutral-80)
    :border-radius    16
    :position         :absolute
    :top              24
    :left             24
    :overflow         :hidden
    :z-index          12}))

(defn delete-button-container
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:justify-content :center
    :align-items     :center
    :position        :absolute
    :width           56
    :height          56
    :bottom          20
    :left            0}))

(defn delete-button-connector
  [opacity width height border-radius-first-half border-radius-second-half]
  (reanimated/apply-animations-to-style
   {:opacity                    opacity
    :width                      width
    :height                     height
    :border-bottom-left-radius  border-radius-first-half
    :border-top-left-radius     border-radius-first-half
    :border-top-right-radius    border-radius-second-half
    :border-bottom-right-radius border-radius-second-half}
   {:justify-content  :center
    :align-items      :center
    :align-self       :center
    :background-color colors/danger-50
    :z-index          0}))

(defn delete-button
  [translate-x opacity]
  (reanimated/apply-animations-to-style
   {:transform [{:translateX translate-x}]
    :opacity   opacity}
   {:width            32
    :height           32
    :justify-content  :center
    :align-items      :center
    :background-color colors/danger-50
    :border-radius    16
    :position         :absolute
    :top              76
    :left             0
    :z-index          11}))

(defn record-button-container
  [opacity]
  (reanimated/apply-animations-to-style
   {:opacity opacity}
   {:margin-bottom 32
    :margin-right  32}))

(def input-container
  {:width           140
   :height          140
   :align-items     :flex-end
   :justify-content :flex-end})