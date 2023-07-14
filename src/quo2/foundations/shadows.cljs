(ns quo2.foundations.shadows
  (:require [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]))

(defn- get-inverted
  [inverted? number]
  (if inverted? (* -1 number) number))

(defn- get-scales
  [inverted?]
  (if (theme/dark?)
    {:shadow-1 {:shadow-color   (colors/alpha colors/neutral-100 0.5)
                :shadow-offset  {:width  0
                                 :height (get-inverted inverted? 4)}
                :elevation      3
                :shadow-opacity 1
                :shadow-radius  20}
     :shadow-2 {:shadow-color   (colors/alpha colors/neutral-100 0.64)
                :shadow-offset  {:width  0
                                 :height (get-inverted inverted? 4)}
                :elevation      4
                :shadow-opacity 1
                :shadow-radius  20}
     :shadow-3 {:shadow-color   (colors/alpha colors/neutral-100 0.64)
                :shadow-offset  {:width  0
                                 :height (get-inverted inverted? 12)}
                :elevation      8
                :shadow-opacity 1
                :shadow-radius  20}
     :shadow-4 {:shadow-color   (colors/alpha colors/neutral-100 0.72)
                :shadow-offset  {:width  0
                                 :height (get-inverted inverted? 16)}
                :shadow-opacity 1
                :shadow-radius  20
                :elevation      15}}
    {:shadow-1 {:shadow-color   (colors/alpha colors/neutral-100 0.04)
                :shadow-offset  {:width  0
                                 :height (get-inverted inverted? 4)}
                :elevation      1
                :shadow-opacity 1
                :shadow-radius  16}
     :shadow-2 {:shadow-color   (colors/alpha colors/neutral-100 0.08)
                :shadow-offset  {:width  0
                                 :height (get-inverted inverted? 4)}
                :elevation      2
                :shadow-opacity 1
                :shadow-radius  16}
     :shadow-3 {:shadow-color   (colors/alpha colors/neutral-100 0.12)
                :shadow-offset  {:width  0
                                 :height (get-inverted inverted? 12)}
                :elevation      5
                :shadow-opacity 1
                :shadow-radius  16}
     :shadow-4 {:shadow-color   (colors/alpha colors/neutral-100 0.16)
                :shadow-offset  {:width  0
                                 :height (get-inverted inverted? 16)}
                :shadow-opacity 1
                :shadow-radius  16
                :elevation      13}}))

(def normal-scale (get-scales false))

(def inverted-scale (get-scales true))

(def inner-shadow
  {:shadow-color   (colors/alpha colors/neutral-100 0.08)
   :shadow-offset  {:width  0
                    :height 0}
   :shadow-opacity 1
   :shadow-radius  16
   :elevation      13})
