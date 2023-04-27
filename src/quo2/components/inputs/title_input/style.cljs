(ns quo2.components.inputs.title-input.style
  (:require [quo2.foundations.colors :as colors]))

(defn get-focused-placeholder-color
  [blur? override-theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 override-theme)
    (colors/theme-colors colors/neutral-30 colors/neutral-60 override-theme))
)

(defn get-placeholder-color
  [blur? override-theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30 override-theme)
    (colors/theme-colors colors/neutral-40 colors/neutral-50 override-theme)))

(defn- get-disabled-color
  [blur? override-theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30)
    (colors/theme-colors colors/neutral-40 colors/neutral-50 override-theme)))

(defn- get-char-count-color
  [blur? override-theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40)
    (colors/theme-colors colors/neutral-40 colors/neutral-50 override-theme)))

(defn get-selection-color
  [customization-color blur? override-theme]
  (if blur?
    (colors/theme-colors colors/neutral-100 colors/white override-theme)
    (colors/custom-color customization-color (if (or (= :dark override-theme) (colors/dark?)) 60 50))))

(def text-input-container {:flex 1})

(defn title-text
  [disabled? blur? override-theme]
  {:text-align-vertical :bottom
   :color               (if disabled?
                          (get-disabled-color blur? override-theme)
                          (colors/theme-colors colors/neutral-100 colors/white override-theme))})

(defn char-count
  [blur? override-theme]
  {:color (get-char-count-color blur? override-theme)})

(def container
  {:flex-direction  :row
   :flex            1
   :justify-content :center
   :align-items     :center})

(def counter-container
  {:padding-top 8})
