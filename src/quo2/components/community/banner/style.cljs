(ns quo2.components.community.banner.style
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.shadows :as shadows]))

(defn community-card
  [theme]
  (assoc
   (shadows/get 2 theme)
   :border-radius     16
   :justify-content   :space-between
   :background-color  (colors/theme-colors colors/white colors/neutral-90 theme)
   :flex-direction    :row
   :margin-horizontal 20
   :margin-top        8
   :margin-bottom     8
   :height            56
   :padding-right     12))

(def banner-content
  {:flex           1
   :padding-top    8
   :padding-bottom 8
   :border-radius  12})

(def banner-title
  {:flex               1
   :padding-horizontal 12})

(def discover-illustration
  {:position :absolute
   :top      -4
   :right    0})
