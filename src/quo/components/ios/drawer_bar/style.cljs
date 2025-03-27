(ns quo.components.ios.drawer-bar.style
  (:require
    [quo.context]
    [quo.foundations.colors :as colors]))

(def handle-container
  {:padding-vertical 8
   :height           20
   :align-items      :center})

(defn handle
  [theme]
  {:width            32
   :height           4
   :background-color (colors/theme-colors colors/neutral-100 colors/white theme)
   :opacity          (if (= theme :light) 0.05 0.1)
   :border-radius    100})
