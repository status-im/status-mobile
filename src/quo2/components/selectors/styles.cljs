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

(defn checkbox-prefill
  [blurred-background? disabled?]
  {:height           21
   :width            21
   :border-radius    6
   :background-color (if blurred-background?
                       (colors/theme-colors
                        (colors/alpha colors/neutral-80
                                      (if disabled? 0.05 0.1))
                        (colors/alpha colors/white (if disabled? 0.05 0.1)))
                       (colors/theme-colors
                        (colors/alpha colors/neutral-20
                                      (if disabled? 0.3 1))
                        (colors/alpha colors/neutral-70
                                      (if disabled? 0.3 1))))})

(defn checkbox
  [blurred-background? disabled? checked?]
  {:flex             1
   :border-radius    6
   :border-width     (if checked? 0 1)
   :background-color (cond
                       checked?
                       (get-color checked? disabled? blurred-background?)
                       blurred-background?
                       (colors/theme-colors
                        colors/white-opa-5
                        colors/white-opa-10)
                       :else
                       (colors/theme-colors
                        colors/white
                        colors/neutral-80-opa-40))
   :border-color     (if checked?
                       :none
                       (get-color checked?
                                  disabled?
                                  blurred-background?))})

(defn checkbox-toggle
  [checked? disabled? blurred-background?]
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

(defn radio
  [checked? disabled? blurred-background?]
  {:height           20
   :width            20
   :border-radius    20
   :border-width     1
   :border-color     (get-color checked?
                                disabled?
                                blurred-background?)
   :background-color (when-not blurred-background?
                       (colors/theme-colors colors/white
                                            (colors/alpha colors/neutral-80
                                                          0.4)))})

(defn toggle
  [checked? disabled? blurred-background?]
  {:height           20
   :width            30
   :border-radius    20
   :background-color (get-color checked?
                                disabled?
                                blurred-background?)})

(defn toggle-inner
  [checked? disabled? blurred-background?]
  {:margin-left      (if checked? 12 2)
   :height           16
   :width            16
   :background-color (if blurred-background?
                       (colors/theme-colors
                        (colors/alpha colors/white (if disabled? 0.4 1))
                        (colors/alpha colors/white (if disabled? 0.3 1)))
                       (colors/theme-colors
                        (colors/alpha colors/white 1)
                        (colors/alpha colors/white (if disabled? 0.4 1))))
   :border-radius    20
   :margin-right     :auto
   :margin-top       :auto
   :margin-bottom    :auto})

(defn radio-inner
  [checked? disabled? blurred-background?]
  {:margin-left      :auto
   :height           14
   :width            14
   :background-color (when checked? (get-color checked? disabled? blurred-background?))
   :border-radius    20
   :margin-right     :auto
   :margin-top       :auto
   :margin-bottom    :auto})
