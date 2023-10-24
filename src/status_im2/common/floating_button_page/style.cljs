(ns status-im2.common.floating-button-page.style)

(def page-container
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0
   :z-index  100})

(defn keyboard-view-style
  [keyboard-shown?]
  {:border-width 1
   :border-color :blue
   ;:padding-bottom (if-not keyboard-shown? 12 0)
  })

