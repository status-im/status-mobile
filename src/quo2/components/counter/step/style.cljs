(ns quo2.components.counter.step.style
  (:require
    [quo2.foundations.colors :as colors]))

(def container-base
  {:align-items     :center
   :justify-content :center
   :border-radius   6
   :height          20})

(defn neutral-border-color
  [in-blur-view? theme]
  (if in-blur-view?
    (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-10 theme)
    (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)))

(def active-background-color (colors/custom-color :blue 50 10))
(def complete-background-color (colors/custom-color :blue 50))

(defn container
  [size type in-blur-view? theme]
  (cond-> container-base
    (#{1 2} size) (assoc :width 20)
    (= size 3) (assoc :width 28)

    (= type :neutral)
    (assoc :border-width 1
           :border-color (neutral-border-color in-blur-view? theme))

    (= type :active)
    (assoc :background-color active-background-color)

    (= type :complete)
    (assoc :background-color complete-background-color)))

(defn text-color
  ([type] (text-color type nil))
  ([type theme]
   (case type
     (:neutral :active) (colors/theme-colors colors/neutral-100-opa-100 colors/white theme)
     :complete          colors/white)))
