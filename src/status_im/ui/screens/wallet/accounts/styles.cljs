(ns status-im.ui.screens.wallet.accounts.styles
  (:require [quo.animated :as animated]
            [status-im.ui.components.colors :as colors]))

(defn container [{:keys [minimized]}]
  (when-not minimized
    {:padding-bottom     8
     :padding-horizontal 16}))

(defn value-container [{:keys [minimized animation]}]
  (when minimized
    {:opacity animation}))

(defn value-text [{:keys [minimized]}]
  {:font-size   (if minimized 20 32)
   :line-height 40
   :color       colors/black})

(defn accounts-mnemonic [{:keys [animation]}]
  {:opacity         (animated/mix animation 1 0)
   :flex            1
   :justify-content :center
   :position        :absolute
   :top             0
   :bottom          0
   :left            0})

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
