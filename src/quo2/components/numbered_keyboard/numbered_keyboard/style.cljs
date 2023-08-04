(ns quo2.components.numbered-keyboard.numbered-keyboard.style)

(def container
  {:flex               1
   :padding-top        8
   :padding-bottom     0
   :border-width 1
   :border-color :red
   :justify-content :space-between
   :align-items :center})

(defn keyboard-item
  [position]
  {:margin-bottom 6
   :flex 1})


(def row-container
  {:flex-direction     :row
   :align-items :space-between
   :flex 1
   :justify-content :center})
