(ns quo.components.counter.counter.style
  (:require
    [quo.foundations.colors :as colors]))

(defn get-color
  [type customization-color theme blur?]
  (case type
    :default   (colors/resolve-color customization-color theme)
    :secondary (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
    :grey      (if blur?
                 (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
                 (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))
    :outline   (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
    nil))

(defn container
  [{:keys [type container-style customization-color theme blur?]}]
  (cond-> [{:align-items        :center
            :justify-content    :center
            :align-self         :flex-start
            :border-radius      6
            :height             16
            :padding-horizontal 5
            :margin             2}
           container-style]
    (= type :outline)
    (conj {:border-width 1
           :border-color (get-color type customization-color theme blur?)})

    (not= type :outline)
    (conj {:background-color (get-color type customization-color theme blur?)})))
