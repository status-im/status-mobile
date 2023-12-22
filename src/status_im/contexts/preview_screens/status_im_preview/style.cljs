(ns status-im.contexts.preview-screens.status-im-preview.style
  (:require
    [quo.foundations.colors :as colors]))

(defn main
  []
  {:flex               1
   :padding-bottom     8
   :padding-horizontal 16
   :background-color   (colors/theme-colors colors/white colors/neutral-90)})
