(ns status-im.common.scalable-avatar.style)

(defn wrapper
  [{:keys [scale top-margin side-margin border-color]}]
  [{:transform     [{:scale scale}]
    :margin-top    top-margin
    :margin-left   side-margin
    :margin-bottom side-margin}
   {:border-width  4
    :border-color  border-color
    :border-radius 100}])
