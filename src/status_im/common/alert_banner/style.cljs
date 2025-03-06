(ns status-im.common.alert-banner.style)

(def border-radius 20)

(def second-banner-wrapper
  {:margin-top              (- border-radius)
   :overflow                :hidden
   :border-top-left-radius  border-radius
   :border-top-right-radius border-radius})

(defn hole-view
  [background-color]
  {:padding-top      11
   :align-items      :center
   :height           60
   :background-color background-color})
