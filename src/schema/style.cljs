(ns schema.style)

(defn container
  [{:keys [bottom-inset]}]
  {:align-items               :center
   :flex-direction            :row
   :background-color          "#cc0000"
   :border-bottom-left-radius 8
   :border-top-left-radius    8
   :justify-content           :center
   :padding-vertical          6
   :padding-right             16
   :position                  :absolute
   :bottom                    (+ 12 bottom-inset)
   :right                     0
   :z-index                   10000000})

(def icon
  {:margin-horizontal 8})

(def text
  {:font-family "Inter-SemiBold"
   :font-size   13
   :color       "#ddd"})

(def text-suffix
  {:font-style :italic
   :font-size  9})
