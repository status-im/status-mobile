(ns status-im.ui.components.checkbox.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.ui.components.styles :as styles]))

(def wrapper
  {:padding 16})

(defnstyle icon-check-container [checked?]
  {:background-color (if checked? styles/color-light-blue styles/color-gray5)
   :alignItems     :center
   :justifyContent :center
   :android        {:border-radius 2
                    :width         17
                    :height        17}
   :ios            {:border-radius 50
                    :width         24
                    :height        24}})

(def check-icon
  {:width  12
   :height 12})