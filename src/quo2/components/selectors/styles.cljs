(ns quo2.components.selectors.styles 
  (:require [quo2.foundations.colors :as colors]))

(defn get-color
  [checked? disabled? blurred-background?]
  (cond
    checked?
    (colors/custom-color-by-theme
     :primary
     50
     60
     (when disabled? 30)
     (when disabled? 30))
    blurred-background?
    (colors/theme-colors
     (colors/alpha colors/neutral-80 (if disabled? 0.05 0.1))
     (colors/alpha colors/white (if disabled? 0.05 0.1)))
    :else
    (colors/theme-colors
     (colors/alpha colors/neutral-20 (if disabled? 0.4 1))
     (colors/alpha colors/neutral-70 (if disabled? 0.3 1)))))

(defn checkbox-toggle [checked? disabled? blurred-background?]
  {:flex             1
   :border-radius    6
   :border-width     (if @checked? 0 1)
   :background-color (cond
                       @checked?
                       (get-color @checked? disabled? blurred-background?)
                       blurred-background?
                       (colors/theme-colors
                        colors/white-opa-5
                        colors/white-opa-10)
                       :else
                       (colors/theme-colors
                        colors/white
                        colors/neutral-80-opa-40))
   :border-color     (if @checked?
                       :none
                       (get-color @checked? disabled? blurred-background?))})