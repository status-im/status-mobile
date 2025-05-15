(ns quo.components.list-items.status-list-item.style
  (:require [quo.foundations.colors :as colors]))

(def container
  {:padding-vertical   5
   :padding-horizontal 20
   :flex-direction     :row})

(defn title
  [theme]
  {:color (colors/theme-colors colors/neutral-100 colors/white theme)})

(defn description
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/white-opa-40 theme)})
