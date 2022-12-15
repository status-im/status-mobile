(ns quo2.components.dividers.date
  (:require [react-native.core :as rn]
            [clojure.string :as string]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(defn date [value]
  [rn/view {:margin-vertical 16
            :padding-left 52}
   [text/text {:weight              :medium
               :accessibility-label :message-datemark-text
               :size :label
               :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
    (string/capitalize value)]
   [rn/view {:width "100%"
             :height 1
             :background-color (colors/theme-colors colors/neutral-10 colors/neutral-90)
             :margin-top 4}]])
