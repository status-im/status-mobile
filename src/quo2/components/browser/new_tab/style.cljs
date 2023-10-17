(ns quo2.components.browser.new-tab.style
  (:require [quo2.foundations.colors :as colors]))

(def gap
  {:height 8})

(defn container
  [customization-color theme]
  {:width            160
   :height           172
   :justify-content  :center
   :align-items      :center
   :border-radius    16
   :border-width     1
   :border-style     :dashed
   :border-color     (colors/resolve-color customization-color theme 40)
   :background-color (colors/resolve-color customization-color theme 10)})
