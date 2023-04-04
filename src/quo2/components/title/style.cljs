(ns quo2.components.title.style
  (:require
    [quo2.foundations.colors :as colors]))

(def default-margin 20)

(def title-container
  {:justify-content    :center
   :margin-top         12
   :padding-horizontal default-margin})

(def title-text
  {:color colors/white})

(def subtitle-text
  {:color         colors/white
   :margin-bottom 8})

