(ns status-im.contexts.onboarding.share-usage.style
  (:require [quo.foundations.colors :as colors]))

(defn page-illustration
  [width]
  {:flex          1
   :margin-top    12
   :margin-bottom 10
   :width         width})

(defn buttons
  [insets]
  {:margin        20
   :margin-bottom (+ 12 (:bottom insets))})

(def page
  {:flex             1
   :background-color colors/neutral-100})
