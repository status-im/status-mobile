(ns quo.components.profile.link-card.style 
  (:require    [quo.foundations.colors :as colors]))

(defn container
  [theme]
  {:width 160
   :height 88
   :border-width 1
   :border-radius 16
   :padding 12
   :border-color (colors/theme-colors
                  colors/neutral-80-opa-5
                  colors/white-opa-5
                  theme)})

(def icon-container
  {:margin-bottom 4})
