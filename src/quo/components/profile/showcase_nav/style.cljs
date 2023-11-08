(ns quo.components.profile.showcase-nav.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [state theme]
  {:padding-top      12
   :padding-left     20
   :padding-bottom   12
   :background-color (case state
                       :default (colors/theme-colors colors/white colors/neutral-95 theme)
                       :transparent)})

(def button-container
  {:margin-right 8})
