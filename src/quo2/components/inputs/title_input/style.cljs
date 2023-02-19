(ns quo2.components.inputs.title-input.style
  (:require [quo2.foundations.colors :as colors]))

(defn get-focused-placeholder-color
  [blur?]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-20 (colors/alpha colors/white 0.2))
    (colors/theme-colors colors/neutral-30 colors/neutral-60)))

(defn get-placeholder-color
  [blur?]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 (colors/alpha colors/white 0.3))
    (colors/theme-colors colors/neutral-40 colors/neutral-50)))

(defn- get-disabled-color
  [blur?]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 (colors/alpha colors/white 0.3))
    (colors/theme-colors colors/neutral-40 colors/neutral-50)))

(defn- get-char-count-color
  [blur?]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 (colors/alpha colors/white 0.4))
    (colors/theme-colors colors/neutral-40 colors/neutral-50)))

(defn get-selection-color
  [customization-color blur?]
  (if blur?
    (colors/theme-colors colors/neutral-100 colors/white)
    (colors/custom-color customization-color (if colors/dark? 60 50))))

(def text-input-container {:flex 1})

(defn title-text
  [disabled? blur?]
  {:text-align-vertical :bottom
   :color               (when disabled? (get-disabled-color blur?))})

(defn char-count
  [blur?]
  {:color (get-char-count-color blur?)})

(def container
  {:flex-direction  :row
   :flex            1
   :justify-content :center
   :align-items     :center})

(def counter-container
  {:padding-top 8})
