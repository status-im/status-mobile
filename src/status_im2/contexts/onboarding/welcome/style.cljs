(ns status-im2.contexts.onboarding.welcome.style)

(def default-margin 20)

(defn page-container
  [insets]
  {:flex        1
   :padding-top (:top insets)})

(defn page-illustration
  [width]
  {:resize-mode   :stretch
   :resize-method :scale
   :width         width
   :margin-top    12
   :margin-bottom 4})

(defn buttons
  [insets]
  {:position      :absolute
   :left          0
   :right         0
   :padding-left  default-margin
   :padding-right default-margin
   :bottom        (+ 12 (:bottom insets))})

(def bottom-shadow
  {:position :absolute
   :height   64
   :top      0
   :bottom   0
   :left     0
   :right    0})
