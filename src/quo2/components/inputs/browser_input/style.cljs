(ns quo2.components.inputs.browser-input.style
  (:require [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(def clear-icon-container
  {:align-items     :center
   :height          20
   :justify-content :center
   :margin-left     8
   :width           20})

(def favicon-icon-container
  {:margin-right 4})

(defn input
  [disabled?]
  (assoc (text/text-style {:size   :paragraph-1
                           :weight :regular})
         :flex             1
         :min-height       32
         :min-width        120
         :opacity          (if disabled? 0.3 1)
         :padding-vertical 5))

(def lock-icon-container
  {:margin-left 2})

(def input-container
  {:align-items    :center
   :flex           1
   :flex-direction :row})

(def label-container
  {:align-items    :center
   :flex-direction :row
   :margin-bottom  24
  })

(defn text
  []
  (assoc (text/text-style {:size   :paragraph-1
                           :weight :medium})
         :color
         (colors/theme-colors colors/neutral-100 colors/white)))
