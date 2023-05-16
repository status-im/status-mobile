(ns quo2.components.buttons.predictive-keyboard.style)

(defn wrapper
  [type]
  {:flex               1
   :min-height         48
   :padding-vertical   8
   :justify-content    :center
   :padding-horizontal (if (= type :words) 0 20)})

(def word-list {:align-items :center :padding-horizontal 20})
