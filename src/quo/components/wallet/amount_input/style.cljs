(ns quo.components.wallet.amount-input.style
  (:require
    [quo.foundations.colors :as colors]))

(def container
  {:flex-direction  :row
   :justify-content :center
   :align-items     :center})

(def input-container {:flex 1})

(defn input-text
  [theme type]
  {:padding    0
   :text-align :center
   :color      (if (= type :error)
                 (colors/resolve-color :danger theme)
                 (colors/theme-colors colors/neutral-100 colors/white theme))})
