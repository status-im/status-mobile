(ns status-im.ui.screens.ens.styles)

(def help-message-label
  {:flex 1
   :margin-top 16
   :margin-horizontal 16
   :font-size 14
   :text-align :center})

(defn finalized-icon-wrapper [color]
  {:width            40
   :height           40
   :border-radius    30
   :background-color color
   :align-items      :center
   :justify-content  :center})