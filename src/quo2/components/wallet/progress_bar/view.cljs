(ns quo2.components.wallet.progress-bar.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [utils.number]))

(defn get-border-color
  [network-state]
  (let [current-theme (theme/get-theme)]
    (cond
      (= current-theme :dark)
      (cond
        (= network-state "pending")  colors/neutral-70
        :else colors/white-opa-5) ; Add an :else to handle other cases
      :else colors/neutral-80-opa-5))) ; Add an :else to handle the case when current-theme is not :dark

(defn get-bg-color
  [network-state]
  (let [current-theme (theme/get-theme)]
    (cond
      (= current-theme :light)
      (cond
        (= network-state "pending")  colors/neutral-5
        (= network-state "confirmed")  colors/success-50
        (= network-state "finalised")  (colors/custom-color-by-theme :blue 50 60)
        (= network-state "error")  colors/danger-50)
      :else
      (cond
        (= network-state "pending")  colors/neutral-80
        (= network-state "confirmed")  colors/success-60
        (= network-state "finalised")  (colors/custom-color-by-theme :blue 60 60)
        (= network-state "error")  colors/danger-60))))

(defn progress-bar
  [{:keys [network-state width height marginHorizontal]}]
  (println network-state "network-state in Box")
  [rn/view
    {:accessibility-label :progress-bar
           :background-color    (colors/theme-colors colors/white colors/neutral-95)
           :padding             0}
   [rn/view
    {:style {:width            (utils.number/parse-int width)
            :height            (utils.number/parse-int height)
            :border-width      1
            :border-radius     3
            :border-color      (get-border-color network-state)
            :background-color  (get-bg-color network-state)
            :margin-horizontal marginHorizontal
            :margin-vertical   4}}    
    ]])
