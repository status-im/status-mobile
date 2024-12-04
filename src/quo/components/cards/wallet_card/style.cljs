(ns quo.components.cards.wallet-card.style
  (:require [quo.foundations.colors :as colors]
            [quo.foundations.shadows :as shadows]))

(defn root-container
  [theme container-style]
  (merge (shadows/get 2 theme)
         {:border-radius      16
          :padding-vertical   10
          :padding-horizontal 12
          :width              161
          :background-color   (colors/theme-colors colors/white colors/neutral-90 theme)}
         container-style))

(def top-container
  {:flex-direction  :row
   :height          32
   :justify-content :space-between
   :margin-bottom   8})

(def image
  {:height 32
   :width  32})

(defn title
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(defn subtitle
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})
