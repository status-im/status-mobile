(ns quo2.components.dividers.divider-label.style
  (:require [quo2.foundations.colors :as colors]))

(defn get-height
  [tight?]
  (if tight? 34 42))

(defn- get-border-color
  [blur? theme]
  (colors/theme-colors (if blur? colors/neutral-80-opa-5 colors/neutral-10)
                       (if blur? colors/white-opa-5 colors/neutral-90)
                       theme))

(defn get-content-color
  [blur? theme]
  (colors/theme-colors (if blur? colors/neutral-80-opa-70 colors/neutral-50)
                       (if blur? colors/white-opa-70 colors/neutral-40)
                       theme))

(defn container
  [blur? tight? chevron theme]
  {:border-top-width 1
   :border-top-color (get-border-color blur? theme)
   :height           (get-height tight?)
   :padding-top      (if tight? 6 14)
   :padding-bottom   7
   :padding-left     (if (= :left chevron) 16 20)
   :padding-right    20
   :align-items      :center
   :flex-direction   :row})

(defn content
  [chevron]
  {:flex-direction (if (= chevron :right)
                     :row-reverse
                     :row)
   :align-items    :center
   :height         20
   :flex           1})

(defn text
  [blur? theme]
  {:color (get-content-color blur? theme)
   :flex  1})
