(ns quo.components.wallet.wallet-overview.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container-info
  [{:keys [metrics]}]
  {:padding-horizontal 20
   :padding-top        12
   :padding-bottom     32
   :flex-grow          1
   :height             (if (= metrics :none) 78 98)})

(def container-info-top
  {:flex-direction  :row
   :justify-content :space-between})

(def container-info-bottom
  {:flex-direction :row
   :padding-top    4})

(defn color-metrics
  [metrics theme]
  (if (= metrics :positive)
    (colors/theme-colors colors/success-50 colors/success-60 theme)
    (colors/theme-colors colors/danger-50 colors/danger-60 theme)))

(defn color-text-heading
  [theme]
  (colors/theme-colors colors/neutral-100 colors/white theme))

(defn color-text-paragraph
  [theme]
  (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme))

(defn style-text-heading
  [theme]
  {:color (color-text-heading theme)})

(defn style-text-paragraph
  [theme]
  {:color (color-text-paragraph theme)})

(defn dot-separator
  [metrics theme]
  {:background-color  (color-metrics metrics theme)
   :margin-horizontal 4
   :width             2
   :height            2})

(defn loading-bar
  [width height margin-right theme]
  {:width            width
   :height           height
   :border-radius    6
   :background-color (colors/theme-colors colors/neutral-5 colors/neutral-90 theme)
   :margin-right     margin-right})
