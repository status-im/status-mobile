(ns status-im.common.scalable-avatar.style)

(defn wrapper
  [border-color scale]
  [{:transform-origin "bottom left"
    :border-width     4
    :border-color     border-color
    :border-radius    100
    :transform        [{:scale 1} {:translate-y 4}]}
   {:transform [{:scale scale} {:translate-y 4}]}])
