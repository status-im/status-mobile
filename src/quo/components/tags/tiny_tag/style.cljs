(ns quo.components.tags.tiny-tag.style
  (:require
    [quo.foundations.colors :as colors]))

(defn get-border-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-10 theme)
    (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)))

(defn get-label-color
  [blur? theme]
  (if blur?
    (colors/theme-colors colors/neutral-80-opa-70 colors/white-opa-70 theme)
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(def main
  {:justify-content :center
   :align-items     :center
   :height          16})

(defn inner
  [{:keys [blur?]} theme]
  {:border-width    1
   :border-radius   6
   :border-color    (get-border-color blur? theme)
   :justify-content :center
   :align-items     :center
   :padding-left    2
   :padding-right   3})

(defn label
  [{:keys [blur?]} theme]
  {:color (get-label-color blur? theme)})
