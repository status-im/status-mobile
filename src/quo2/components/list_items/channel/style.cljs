(ns quo2.components.list-items.channel.style
  (:require [quo2.foundations.colors :as colors]))

(defn- get-label-color
  [notification theme]
  (let [colors {:notification (colors/theme-colors colors/neutral-100
                                                   colors/white
                                                   theme)
                :mention      (colors/theme-colors colors/neutral-100
                                                   colors/white
                                                   theme)
                :mute         (colors/theme-colors colors/neutral-40
                                                   colors/neutral-60
                                                   theme)
                :default      (colors/theme-colors colors/neutral-50
                                                   colors/neutral-40
                                                   theme)}]
    (colors (or notification :default))))

(defn mute-notification-icon-color
  [theme]
  (colors/theme-colors colors/neutral-40
                       colors/neutral-60
                       theme))

(defn container
  [pressed? customization-color theme]
  {:height             48
   :border-radius      12
   :padding-horizontal 12
   :padding-vertical   8
   :align-items        :center
   :overflow           :hidden
   :background-color   (if pressed?
                         (colors/theme-colors
                          (colors/custom-color customization-color 50 5)
                          (colors/custom-color customization-color 60 5)
                          theme)
                         :transparent)
   :flex-direction     :row})

(defn label
  [notification theme]
  {:margin-horizontal 12
   :color             (get-label-color notification theme)
   :flex              1})

(defn counter
  [mentions]
  {:margin-horizontal (if (= (count (str mentions)) 1) 2 0)})
