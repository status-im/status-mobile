(ns quo2.components.selectors.filter.style
  (:require [quo2.foundations.colors :as colors]))

(def container-default
  {:width           32
   :height          32
   :border-radius   10
   :align-items     :center
   :justify-content :center
   :padding         6})

(defn container-border-color
  [pressed? blur? override-theme]
  (let [dark? (= :dark override-theme)]
    (cond
      (and (not pressed?) (not dark?) (not blur?))
      colors/neutral-20

      (and (not pressed?) dark? (not blur?))
      colors/neutral-80

      (and pressed? (not dark?) blur?)
      colors/neutral-80-opa-20

      (or (and pressed? (not dark?) (not blur?))
          (and (not pressed?) (not dark?) blur?))
      colors/neutral-80-opa-10

      (or (and pressed? dark? (not blur?))
          (and (not pressed?) dark? blur?)
          (and pressed? dark? blur?))
      colors/white-opa-10

      :else
      nil)))

(defn container-background-color
  [pressed? override-theme]
  (when pressed?
    (if (= :dark override-theme)
      colors/primary-60
      colors/primary-50)))

(defn container-outer
  [pressed? override-theme]
  (merge container-default
         {:background-color (container-background-color pressed? override-theme)}))

(defn container-inner
  [pressed? blur? override-theme]
  (merge container-default
         {:border-width 1
          :border-color (container-border-color pressed? blur? override-theme)}))

(defn icon-color
  [pressed? override-theme]
  (if (and (not pressed?)
           (= :light override-theme))
    colors/neutral-100
    colors/white))
