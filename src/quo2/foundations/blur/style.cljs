(ns quo2.foundations.blur.style)

(defn container
  [container-style]
  (merge {:position :absolute
          :top      0
          :left     0
          :right    0
          :bottom   0}
         container-style))
