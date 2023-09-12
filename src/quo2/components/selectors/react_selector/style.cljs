(ns quo2.components.selectors.react-selector.style
  (:require [quo2.foundations.colors :as colors]))

(defn- pinned-colors
  [theme]
  {:background-color (colors/theme-colors colors/neutral-80-opa-5
                                          colors/white-opa-5
                                          theme)
   :border-color     (colors/theme-colors colors/neutral-80-opa-5
                                          colors/white-opa-5
                                          theme)})

(defn reaction
  [neutral? pinned? theme]
  (cond-> {:flex-direction     :row
           :justify-content    :center
           :align-items        :center
           :padding-horizontal 8
           :border-radius      8
           :height             24
           :border-width       1
           :border-color       (colors/theme-colors colors/neutral-20
                                                    colors/neutral-80
                                                    theme)
           :background-color   (colors/theme-colors colors/neutral-10
                                                    colors/neutral-80-opa-40)}
    pinned?  (merge (pinned-colors theme))
    neutral? (merge {:background-color :transparent})))

(def reaction-count {:margin-left 4})
