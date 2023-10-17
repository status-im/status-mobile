(ns quo.components.drawers.bottom-actions.style
  (:require
    [quo.foundations.colors :as colors]))

(def buttons-container
  {:flex-direction     :row
   :justify-content    :space-around
   :padding-vertical   12
   :padding-horizontal 20})

(def button-container
  {:flex 1})

(def button-container-2-actions
  (assoc button-container :margin-right 12))

(defn description
  [theme scroll?]
  {:color              (colors/theme-colors
                        (if scroll?
                          (colors/custom-color colors/neutral-80 70)
                          colors/neutral-50)
                        (if scroll?
                          colors/white-opa-70
                          colors/neutral-40)
                        theme)
   :text-align         :center
   :padding-horizontal 40})

(def scroll
  {:margin-top        21
   :margin-horizontal 120
   :margin-bottom     8
   :width             134
   :height            5})
