(ns quo2.components.notifications.count-down-circle
  (:require
   ["react-native-countdown-circle-timer" :refer [CountdownCircleTimer]]
   [quo2.foundations.colors :as colors]
   [quo2.theme :as theme]
   [reagent.core :as reagent]))

(def count-down-circle-timer (reagent/adapt-react-class CountdownCircleTimer))

(def ^:private themes
  {:color {:light colors/neutral-80-opa-40
           :dark  colors/white-opa-40}})

(defn circle-timer
  [{:keys [color duration size]}]
  [:f>
   (fn []
     [count-down-circle-timer
      {:isPlaying     true
       :duration      (or duration 4)
       :colors        (or color (get-in themes [:color (theme/get-theme)]))
       :strokeWidth   1
       :strokeLinecap :square
       :size          (or size 9)}])])
