(ns quo.components.wallet.swap-input.style
  (:require [quo.foundations.colors :as colors]
            [quo.foundations.shadows :as shadows]
            [quo.foundations.typography :as typography]))

(defn- border-color
  [theme]
  (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))

(defn- loader-color
  [theme]
  (colors/theme-colors colors/neutral-5 colors/neutral-90 theme))

(defn content
  [typing? theme]
  (merge
   {:border-width     1
    :border-radius    16
    :border-color     (border-color theme)
    :background-color (colors/theme-colors colors/white colors/neutral-95 theme)}
   (when typing? (shadows/get 1 theme))))

(defn row-1
  [loading?]
  {:padding        12
   :gap            8
   :align-items    (if loading? :center :flex-end)
   :flex-direction :row})

(defn row-1-loader
  [theme]
  {:width            74
   :height           14
   :border-radius    6
   :background-color (loader-color theme)})

(def input-container
  {:flex           1
   :flex-direction :row
   :gap            5
   :height         32
   :align-items    :flex-end})

(defn input
  [disabled? error? theme]
  (assoc typography/font-semi-bold
         :font-size   27
         :flex-shrink 1
         :padding     0
         :color       (cond
                        error?    (colors/resolve-color :danger theme)
                        disabled? (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
                        :else     (colors/theme-colors colors/neutral-100 colors/white theme))
         :line-height 32))

(defn token-symbol
  [theme]
  {:padding-bottom 3
   :color          (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(defn row-2
  [align-right?]
  {:flex-direction  :row
   :justify-content (if align-right? :flex-end :space-between)
   :align-items     :center
   :padding         12})

(defn row-2-loader
  [theme]
  {:width            80
   :height           10
   :margin-vertical  7
   :border-radius    6
   :background-color (loader-color theme)})

(def fiat-amount
  {:color colors/neutral-50})

(def gradient-common
  {:width    64
   :position :absolute
   :top      0
   :bottom   0
   :z-index  1})

(def gradient-start
  (assoc gradient-common :left 0))

(def gradient-end
  (assoc gradient-common :right 0))

(defn button-loader
  [theme]
  {:width            100
   :height           24
   :border-radius    6
   :background-color (loader-color theme)})
