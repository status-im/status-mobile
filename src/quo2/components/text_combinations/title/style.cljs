(ns quo2.components.text-combinations.title.style
  (:require
    [quo2.foundations.colors :as colors]))

(def title-container
  {:justify-content    :center
   :margin-top         12
   :padding-horizontal 20})

(def title-text
  {:color colors/white})

(def subtitle-text
  {:color         colors/white
   :margin-bottom 8})
