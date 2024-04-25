(ns status-im.contexts.onboarding.welcome.style)

(def default-margin 20)

(defn page-container
  [insets]
  {:flex        1
   :padding-top (:top insets)})

(defn page-illustration
  [{:keys [width height]}]
  {:width  width
   ;; Aspect ratio 2:3
   :height (/ height 1.5)})

(def page-title
  {:margin-top        12
   :margin-horizontal 20
   :margin-bottom     8})

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
