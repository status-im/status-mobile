(ns quo.components.text-combinations.channel-name.style
  (:require [quo.foundations.colors :as colors]))

(def container {:flex-direction :row})

(def icons-container
  {:flex-direction :row
   :padding-top    8
   :padding-bottom 4
   :margin-left    6})

(def icon {:width 20 :height 20})

(def icons-gap {:width 4})

(defn- blur-icon-color
  [theme]
  (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme))

(defn unlocked-icon-color
  [theme blur?]
  (if blur?
    (blur-icon-color theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn muted-icon-color
  [theme blur?]
  (if blur?
    (blur-icon-color theme)
    (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)))
