(ns status-im.common.contact-list.style
  (:require [quo.foundations.colors :as colors]))

(def contacts-section-footer
  {:height 8})

(defn contacts-section-header
  [theme]
  {:background-color (colors/theme-colors colors/white colors/neutral-95 theme)})
