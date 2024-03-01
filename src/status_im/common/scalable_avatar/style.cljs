(ns status-im.common.scalable-avatar.style)

(defn wrapper
  [{:keys [scale margin-top margin border-color]}]
  [{:transform     [{:scale scale}]
    :margin-top    margin-top
    :margin-left   margin
    :margin-bottom margin}
   {:border-width  4
    :border-color  border-color
    :border-radius 100}])
