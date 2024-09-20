(ns status-im.contexts.preview.status-im.style
  (:require
    [quo.foundations.colors :as colors]))

(defn main
  [theme]
  {:flex               1
   :padding-bottom     8
   :padding-horizontal 16
   :background-color   (colors/theme-colors colors/white colors/neutral-90 theme)})
