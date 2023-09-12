(ns quo2.components.inputs.title-input.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(defn get-focused-placeholder-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 theme)
    (colors/theme-colors colors/neutral-30 colors/neutral-60 theme)))

(defn get-placeholder-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-30 theme)
    (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)))

(defn get-char-count-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40)
    (colors/theme-colors colors/neutral-40 colors/neutral-50 theme)))

(defn get-selection-color
  [customization-color blur? theme]
  (colors/alpha (if blur?
                  (colors/theme-colors colors/neutral-100 colors/white theme)
                  (colors/custom-color customization-color
                                       (if (or (= :dark theme) colors/dark?) 60 50)))
                (if platform/ios? 1 0.2)))

(def text-input-container {:flex 1})

(defn title-text
  [theme]
  {:text-align-vertical :bottom
   :padding             0
   :color               (colors/theme-colors colors/neutral-100 colors/white theme)})

(defn char-count
  [blur? theme]
  {:color (get-char-count-color blur? theme)})

(defn container
  [disabled?]
  {:flex-direction  :row
   :opacity         (if disabled? 0.3 1)
   :justify-content :center
   :align-items     :center})

(defn counter-container
  [focused?]
  {:padding-top    (if focused? 12 9)
   :padding-bottom (if focused? 2 3)})
