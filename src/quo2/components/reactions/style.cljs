(ns quo2.components.reactions.style
  (:require [quo2.foundations.colors :as colors]))

(def reaction-styling
  {:flex-direction     :row
   :justify-content    :center
   :align-items        :center
   :padding-horizontal 8
   :border-radius      8
   :height             24})

(defn add-reaction
  [theme]
  (merge reaction-styling
         {:padding-horizontal 8
          :border-width       1
          :border-color       (colors/theme-colors colors/neutral-20
                                                   colors/neutral-70
                                                   theme)}))

(defn reaction
  [neutral? theme]
  (merge reaction-styling
         {:border-color     (colors/theme-colors colors/neutral-20
                                                 colors/neutral-80
                                                 theme)
          :border-width     1
          :background-color (if neutral?
                              (colors/theme-colors colors/neutral-10
                                                   colors/neutral-80-opa-40)
                              :transparent)}))

(def reaction-count {:margin-left 4})
