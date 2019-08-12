(ns status-im.ui.screens.wallet.account.styles
  (:require [status-im.ui.components.colors :as colors]))

(defn card [window-width color]
  {:width            (- window-width 30)
   :height           161
   :background-color color
   :shadow-offset    {:width 0 :height 2}
   :shadow-radius    8
   :shadow-opacity   1
   :shadow-color     "rgba(0, 9, 26, 0.12)"
   :elevation        2
   :border-radius    8
   :justify-content  :space-between})

(def divider
  {:height           52
   :width            1
   :background-color (colors/alpha colors/black 0.2)
   :shadow-offset    {:width 0 :height 2}
   :shadow-radius    8
   :shadow-opacity   1
   :shadow-color     "rgba(0, 9, 26, 0.12)"})