(ns quo2.components.text-combinations.title.style
  (:require
    [quo2.foundations.colors :as colors]))

(defn title-container
  [container-style]
  (merge
   {:justify-content    :center
    :padding-horizontal 20}
   container-style))

(def title-text
  {:color colors/white})

(def subtitle-text
  {:color      colors/white
   :margin-top 8})
