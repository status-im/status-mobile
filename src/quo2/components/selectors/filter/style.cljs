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
  [pressed? blur? theme]
  (let [dark? (= :dark theme)]
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
  [customization-color pressed? theme]
  (when pressed?
    (if customization-color
      (colors/custom-color-by-theme customization-color 50 60)
      (colors/theme-colors colors/primary-50 colors/primary-60 theme))))

(defn container-outer
  [customization-color pressed? theme]
  (merge container-default
         {:background-color (container-background-color customization-color pressed? theme)}))

(defn container-inner
  [pressed? blur? theme]
  (merge container-default
         {:border-width 1
          :border-color (container-border-color pressed? blur? theme)}))

(defn icon-color
  [pressed? theme]
  (if (and (not pressed?)
           (= :light theme))
    colors/neutral-100
    colors/white))
