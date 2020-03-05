(ns status-im.ui.screens.wallet.accounts.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.reanimated :as reanimated]
            [status-im.utils.platform :as platform]))

(def ^:const tabbar-height 56)
;; TODO(Ferossgp): get layout size of total-value
(def ^:const value-height (+ 40 22 8))
(def ^:const scroll-offset value-height)
(def ^:const minimized-value-line-height 28)

(defn topbar [{:keys [value offset inset-top]}]
  (merge
   {:flex-direction     :row
    :padding-horizontal 8
    :background-color   colors/white
    :padding-top        inset-top}
   (when platform/android?
     {:elevation (reanimated/interpolate
                  value
                  {:inputRange  [0 offset]
                   :outputRange [0 4]
                   :extrapolate (:clamp reanimated/extrapolate)})})
   (when platform/ios?
     {:shadow-opacity (reanimated/interpolate
                       value
                       {:inputRange  [0 offset]
                        :outputRange [0 1]
                        :extrapolate (:clamp reanimated/extrapolate)})
      :shadow-radius  16
      :z-index        2
      :shadow-color   (if (colors/dark?)
                        "rgba(0, 0, 0, 0.75)"
                        "rgba(0, 9, 26, 0.12)")
      :shadow-offset  {:width 0 :height 4}})))

(defn value-container [y]
  {:position  :absolute
   :left      8
   :top       0
   :transform [{:translateY
                (reanimated/interpolate
                 y
                 {:inputRange       [0 scroll-offset]
                  :outputRange      [scroll-offset
                                     (/ (- tabbar-height
                                           minimized-value-line-height) 2)]
                  :extrapolateRight (:clamp reanimated/extrapolate)})}]})

(defn value-text [y]
  {:font-size   (reanimated/interpolate
                 y
                 {:inputRange  [0 scroll-offset]
                  :outputRange [32 20]
                  :extrapolate (:clamp reanimated/extrapolate)})
   :color       colors/black
   :font-weight "600"})

(defn value-helper [y]
  {:opacity (reanimated/interpolate y
                                    {:inputRange  [0 scroll-offset]
                                     :outputRange [1 0]})})

(defn accounts-mnemonic [y]
  {:flex            1
   :justify-content :center
   :opacity         (reanimated/interpolate y
                                            {:inputRange  [0 scroll-offset]
                                             :outputRange [1 0]})})

(defn card-common []
  {:margin-vertical   16
   :margin-horizontal 8
   :width             156
   :height            145
   :shadow-offset     {:width 0 :height 2}
   :shadow-radius     8
   :shadow-opacity    1
   :shadow-color      (if (colors/dark?)
                        "rgba(0, 0, 0, 0.75)"
                        "rgba(0, 9, 26, 0.12)")
   :elevation         3
   :border-radius     8})

(defn card [color]
  (merge (card-common)
         {:background-color   color
          :justify-content    :space-between
          :padding-horizontal 12
          :padding-top        12
          :padding-bottom     6}))

(defn add-card []
  (merge (card-common)
         {:background-color colors/white
          :justify-content  :center
          :align-items      :center}))

(def send-button-container
  {:position        :absolute
   :z-index         2
   :align-items     :center
   :justify-content :center
   :left            0
   :right           0
   :bottom          16
   :height          40})

(defn send-button []
  {:width            40
   :height           40
   :background-color colors/blue
   :border-radius    20
   :align-items      :center
   :justify-content  :center
   :shadow-offset    {:width 0 :height 1}
   :shadow-radius    6
   :shadow-opacity   1
   :shadow-color     (if (colors/dark?)
                       "rgba(0, 0, 0, 0.75)"
                       "rgba(0, 12, 63, 0.2)")
   :elevation        2})
