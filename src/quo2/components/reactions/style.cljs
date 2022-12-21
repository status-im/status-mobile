(ns quo2.components.reactions.style
  (:require [quo2.foundations.colors :as colors]))

(def reaction-styling
  {:flex-direction     :row
   :justify-content    :center
   :align-items        :center
   :padding-horizontal 8
   :border-radius      8
   :height             24})

(defn add-reaction []
  (merge reaction-styling
         {:padding-horizontal 9
          :border-width       1
          :border-color       (colors/theme-colors colors/neutral-30 colors/neutral-70)}))

(defn reaction [neutral?]
  (merge reaction-styling
         (cond-> {:background-color (colors/theme-colors (if neutral?
                                                           colors/neutral-30
                                                           :transparent)
                                                         (if neutral?
                                                           colors/neutral-70
                                                           :transparent))}
           (and (colors/dark?) (not neutral?))
           (assoc :border-color colors/neutral-70
                  :border-width 1)
           (and (not (colors/dark?)) (not neutral?))
           (assoc :border-color colors/neutral-30
                  :border-width 1))))
