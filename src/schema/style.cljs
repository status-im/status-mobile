(ns schema.style)

(def container
  {:position                  :absolute
   :right                     0
   :bottom                    0
   :border-top-left-radius    8
   :border-bottom-left-radius 8
   :justify-content           :center
   :align-items               :center
   :padding                   6
   :padding-horizontal        16
   :background-color          "#cc0000"
   :z-index                   10000000})

(def text
  {:font-family "Inter-SemiBold"
   :font-size   13
   :color       "#dddddd"})
