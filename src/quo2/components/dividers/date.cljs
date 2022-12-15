(ns quo2.components.dividers.date
  (:require [react-native.core :as rn]
            [clojure.string :as string]
            [quo2.components.separator :as separator]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(defn date [value]
  [rn/view {:margin-vertical 16
            :padding-left 52}
   [text/text {:weight              :medium
               :accessibility-label :message-datemark-text
               :size :label
               :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)
                       :margin-bottom 4}}
    (string/capitalize value)]
   [separator/separator]])
