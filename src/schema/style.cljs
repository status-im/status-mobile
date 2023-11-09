(ns schema.style)

(defn container
  [{:keys [bottom-inset]}]
  {:align-items               :center
   :background-color          "#cc0000"
   :border-bottom-left-radius 8
   :border-top-left-radius    8
   :justify-content           :center
   :padding                   6
   :padding-horizontal        16
   :position                  :absolute
   :bottom                    (+ 12 bottom-inset)
   :right                     0
   :z-index                   10000000})

(def text
  {:font-family "Inter-SemiBold"
   :font-size   13
   :color       "#ddd"})
