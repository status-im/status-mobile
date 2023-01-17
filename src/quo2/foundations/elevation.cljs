(ns quo2.foundations.elevation
  (:require [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]))


(defn- get-inverted [inverted? number] (if inverted? (* -1 number) number))

(defn- get-scales [inverted?]
  (if theme/dark?
    {:elevation-1 {:shadow-color colors/neutral-100
                   :shadow-offset {:width 0
                                   :height (get-inverted inverted? 4)}
                   :elevation 1
                   :shadow-opacity 0.5
                   :shadow-radius 6.4}
     :elevation-2 {:shadow-color colors/neutral-100
                   :shadow-offset {:width 0
                                   :height (get-inverted inverted? 4)}
                   :elevation 2
                   :shadow-opacity 0.64
                   :shadow-radius 6.4}
     :elevation-3 {:shadow-color colors/neutral-100
                   :shadow-offset {:width 0
                                   :height (get-inverted inverted? 12)}
                   :elevation 5
                   :shadow-opacity 0.64
                   :shadow-radius 8}
     :elevation-4 {:shadow-color colors/neutral-100
                   :shadow-offset {:width 0
                                  :height (get-inverted inverted? 16)}
                   :shadow-opacity 0.72
                   :shadow-radius 10.24
                   :elevation 13}}
    {:elevation-1 {:shadow-color colors/neutral-100
                   :shadow-offset {:width 0
                                   :height (get-inverted inverted? 2)}
                   :elevation 1
                   :shadow-opacity 0.04
                   :shadow-radius 3.2}
     :elevation-2 {:shadow-color colors/neutral-100
                   :shadow-offset {:width 0
                                   :height (get-inverted inverted? 4)}
                   :elevation 2
                   :shadow-opacity 0.08
                   :shadow-radius 3.2}
     :elevation-3 {:shadow-color colors/neutral-100
                   :shadow-offset {:width 0
                                   :height (get-inverted inverted? 8)}
                   :elevation 5
                   :shadow-opacity 0.12
                   :shadow-radius 4.8}
     :elevation-4 {:shadow-color colors/neutral-100
                   :shadow-offset {:width 0
                                   :height (get-inverted inverted? 12)}
                   :shadow-opacity 0.16
                   :shadow-radius 8.96
                   :elevation 13}}))

(def normal-scale (get-scales false))

(def inverted-scale (get-scales true))

(def inner-shadow {:shadow-color colors/neutral-100
                   :shadow-offset {:width 0
                                   :height 0}
                   :shadow-opacity 0.08
                   :shadow-radius 0.64
                   :elevation 13})
