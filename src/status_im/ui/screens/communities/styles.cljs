(ns status-im.ui.screens.communities.styles)

(def category-item
  {:flex           1
   :flex-direction :row
   :align-items    :center
   :height         52
   :padding-left   18})

(defn card-redesign [window-width color]
  {:width            window-width
   :background-color color
   :shadow-offset    {:width 0 :height 2}
   :shadow-radius    20
   :shadow-opacity   1
   :shadow-color     "rgba(9, 16, 28, 0.04)"
   :border-radius    20
   :justify-content  :space-between
   :elevation        2})