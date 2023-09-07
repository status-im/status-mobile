(ns quo2.components.counter.counter.style
  (:require [quo2.foundations.colors :as colors]))

(defn get-color
  [type customization-color theme]
  (case type
    :default   (colors/custom-color customization-color 50)
    :secondary (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
    :grey      (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
    :outline   (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
    nil))

(defn container
  [{:keys [type label container-style customization-color theme value max-value]}]
  (let [width (case (count label)
                1 16
                2 20
                28)]
    (cond-> (merge
             {:align-items     :center
              :justify-content :center
              :border-radius   6
              :width           width
              :height          16}
             container-style)
      (= type :outline)
      (merge {:border-width 1
              :border-color (get-color type customization-color theme)})
      (not= type :outline)
      (assoc :background-color (get-color type customization-color theme))
      (> value max-value)
      (assoc :padding-left 0.5))))
