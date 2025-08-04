(ns quo.components.text-combinations.username.style
  (:require [quo.foundations.colors :as colors]))

(def container
  {:flex-direction  :row
   :justify-content :flex-start
   :height          32})

(def username-text-container
  {:flex-direction :row
   :align-items    :flex-end
   :flex-shrink    1})

(defn real-name-text
  [theme blur?]
  {:color       (if blur?
                  (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
                  (colors/theme-colors colors/neutral-60 colors/neutral-40 theme))
   :flex-shrink 1})

(defn real-name-dot
  [theme blur?]
  (merge (real-name-text theme blur?)
         {:margin-horizontal 6
          :flex-shrink       0}))

(defn status-icon-container
  [name-type status]
  (cond-> {:flex-direction :row
           :margin-left    4}
    (#{:default :ens} name-type)      (assoc :margin-top    8
                                             :margin-bottom 4)
    (= :nickname name-type)           (assoc :margin-top    10
                                             :margin-bottom 2)
    (= status :untrustworthy-contact) (assoc :margin-right 2)))
