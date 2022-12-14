(ns quo2.components.dividers.date
  (:require [react-native.core :as rn]
            [clojure.string :as string]
            [quo2.components.markdown.text :as quo2.text]
            [quo2.foundations.colors :as quo2.colors]))

(defn date [value]
  [rn/touchable-without-feedback
   {:on-press #(rn/dismiss-keyboard!)}
   [rn/view {:margin-vertical 16
             :padding-left 52}
    [quo2.text/text {:weight              :medium
                     :accessibility-label :message-datemark-text
                     :size :label
                     :style {:color (quo2.colors/theme-colors quo2.colors/neutral-50 quo2.colors/neutral-40)}}
     (string/capitalize value)]
    [rn/view {:width "100%"
              :height 1
              :background-color (quo2.colors/theme-colors quo2.colors/neutral-10 quo2.colors/neutral-90)
              :margin-top 4}]]])
