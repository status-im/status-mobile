(ns quo.components.common.drawer-bar.style
  (:require
    [quo.foundations.colors :as colors]
    [quo.theme :as theme]))

(def handle-container
  {:padding-vertical 8
   :height           20
   :align-items      :center})

(defn handle
  [{:keys [theme]}]
  {:width            32
   :height           4
   :background-color (colors/theme-colors colors/neutral-100 colors/white theme)
   :opacity          (theme/theme-value 0.05 0.1 theme)
   :border-radius    100})
