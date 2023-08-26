(ns quo2.foundations.shadows
  (:refer-clojure :exclude [get])
  (:require [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]))

(def ^:private shadows
  (let [dark-normal           {1 {:shadow-color   (colors/alpha colors/neutral-100 0.5)
                                  :shadow-offset  {:width 0 :height 4}
                                  :elevation      3
                                  :shadow-opacity 1
                                  :shadow-radius  20}
                               2 {:shadow-color   (colors/alpha colors/neutral-100 0.64)
                                  :shadow-offset  {:width 0 :height 4}
                                  :elevation      4
                                  :shadow-opacity 1
                                  :shadow-radius  20}
                               3 {:shadow-color   (colors/alpha colors/neutral-100 0.64)
                                  :shadow-offset  {:width 0 :height 12}
                                  :elevation      8
                                  :shadow-opacity 1
                                  :shadow-radius  20}
                               4 {:shadow-color   (colors/alpha colors/neutral-100 0.72)
                                  :shadow-offset  {:width 0 :height 16}
                                  :shadow-opacity 1
                                  :shadow-radius  20
                                  :elevation      15}}
        dark-normal-inverted  (-> dark-normal
                                  (update-in [:soft :shadow-offset :height] -)
                                  (update-in [:medium :shadow-offset :height] -)
                                  (update-in [:intense :shadow-offset :height] -)
                                  (update-in [:strong :shadow-offset :height] -))
        light-normal          {1 {:shadow-color   (colors/alpha colors/neutral-100 0.04)
                                  :shadow-offset  {:width 0 :height 4}
                                  :elevation      1
                                  :shadow-opacity 1
                                  :shadow-radius  16}
                               2 {:shadow-color   (colors/alpha colors/neutral-100 0.08)
                                  :shadow-offset  {:width 0 :height 4}
                                  :elevation      6
                                  :shadow-opacity 1
                                  :shadow-radius  16}
                               3 {:shadow-color   (colors/alpha colors/neutral-100 0.12)
                                  :shadow-offset  {:width 0 :height 12}
                                  :elevation      8
                                  :shadow-opacity 1
                                  :shadow-radius  16}
                               4 {:shadow-color   (colors/alpha colors/neutral-100 0.16)
                                  :shadow-offset  {:width 0 :height 16}
                                  :shadow-opacity 1
                                  :shadow-radius  16
                                  :elevation      13}}
        light-normal-inverted (-> light-normal
                                  (update-in [:soft :shadow-offset :height] -)
                                  (update-in [:medium :shadow-offset :height] -)
                                  (update-in [:intense :shadow-offset :height] -)
                                  (update-in [:strong :shadow-offset :height] -))]
    {:dark  {:normal dark-normal :inverted dark-normal-inverted}
     :light {:normal light-normal :inverted light-normal-inverted}}))

(defn get
  "Get the appropriate shadow map for a given shadow `weight`, `theme`, and `scale-type`.

  Return nil if no shadow is found.

  `weight` - int (required) from 1 to 4.
  `theme` - :light/:dark (optional).
  `scale-type` - :normal/:inverted (optional).
  "
  ([weight]
   (get weight (quo.theme/get-theme)))
  ([weight theme]
   (get weight theme :normal))
  ([weight theme scale-type]
   (get-in shadows [theme scale-type weight])))

(def inner-shadow
  {:shadow-color   (colors/alpha colors/neutral-100 0.08)
   :shadow-offset  {:width  0
                    :height 0}
   :shadow-opacity 1
   :shadow-radius  16
   :elevation      13})
