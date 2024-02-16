(ns legacy.status-im.ui.screens.wallet.accounts.styles
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.colors :as colors]))

(def dot-size 6)

(def dot-container
  {:height             200
   :flex-direction     :column
   :flex-wrap          :wrap
   :padding-horizontal 8})

(defn dot-selector
  []
  {:flex-direction  :row
   :justify-content :space-between
   :align-items     :center})

(defn dot-style
  [selected]
  {:background-color  (if selected colors/blue colors/blue-light)
   :overflow          :hidden
   :opacity           1
   :margin-horizontal 3
   :width             dot-size
   :height            dot-size
   :border-radius     3})

(defn container
  [{:keys [minimized]}]
  (when-not minimized
    {:padding-bottom     8
     :padding-horizontal 16}))

(defn value-container
  [{:keys [minimized animation]}]
  (when minimized
    {:opacity animation}))

(defn value-text
  [{:keys [minimized]}]
  {:font-size   (if minimized 20 32)
   :line-height 40
   :color       colors/black})

(defn accounts-mnemonic
  [{:keys [animation]}]
  {:opacity         (if animation 1 0)
   :flex            1
   :justify-content :center
   :position        :absolute
   :top             0
   :bottom          0
   :left            0})

(def card-margin 8)
(defn page-width
  [card-width]
  (+ card-width (* card-margin 2)))

(defn card-common
  [card-width]
  {:margin        card-margin
   :width         card-width
   :height        82
   :border-radius 16})

(defn card
  [color card-width]
  (merge (card-common card-width)
         {:background-color   (if (string/blank? color)
                                colors/blue
                                color)
          :justify-content    :space-between
          :padding-horizontal 12
          :padding-top        12
          :padding-bottom     10}))

(defn add-card
  [card-width]
  (merge (card-common card-width)
         {:background-color colors/white
          :flex-direction   :row
          :border-width     1
          :border-color     colors/gray-lighter
          :justify-content  :center
          :align-items      :center}))

(def add-text
  {:color       colors/blue
   :margin-left 8
   :font-weight "500"
   :font-size   15
   :line-height 22})

(def card-name
  {:color       colors/white-persist
   :font-weight "500"
   :font-size   15
   :line-height 22})

(def card-address
  {:number-of-lines 1
   :ellipsize-mode  :middle
   :size            :small
   :monospace       true
   :style           {:width       110
                     :font-size   15
                     :line-height 22
                     :color       colors/white-transparent-70-persist}})

(def card-value
  {:color       colors/white-persist
   :font-size   22
   :font-weight "500"})

(def card-value-currency
  {:color       colors/white-persist
   :font-size   22
   :font-weight "500"})

(def card-icon-more
  {:border-radius    32
   :width            36
   :height           36
   :justify-content  :center
   :align-items      :center
   :background-color colors/black-transparent})

(def card-icon-type
  {:border-radius    32
   :width            36
   :height           36
   :justify-content  :center
   :align-items      :center
   :margin-left      :auto
   :margin-right     12
   :background-color colors/white-persist})

(def send-button-container
  {:position        :absolute
   :z-index         2
   :align-items     :center
   :justify-content :center
   :left            0
   :right           0
   :bottom          56
   :height          40})

(defn send-button
  []
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
