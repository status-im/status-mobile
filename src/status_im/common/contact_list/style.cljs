(ns status-im.common.contact-list.style
  (:require [quo.foundations.colors :as colors]))

(defn contacts-section-header
  [first-item? theme]
  {:background-color (colors/theme-colors colors/white colors/neutral-95 theme)
   :padding-top      (if first-item? 0 8)})
