(ns quo.components.text-combinations.page-top.style
  (:require [quo.foundations.colors :as colors]))

(def top-container
  {:padding-vertical   12
   :padding-horizontal 20
   :row-gap            8})

(def header
  {:flex-direction  :row
   :justify-content :space-between})

(def header-title
  {:flex           1
   :flex-direction :row
   :column-gap     8})

(def header-counter
  {:margin-left     20
   :margin-bottom   2
   :justify-content :flex-end})

(def context-tag-description
  {:align-items :flex-start})

(def summary-description
  {:row-gap 4})

(def summary-description-row
  {:flex-direction :row
   :align-items    :center
   :column-gap     4})

(def image-text-description
  {:flex-direction :row
   :column-gap     8
   :align-items    :center})

(def community-logo
  {:width         24
   :height        24
   :border-radius 12})

(def community-logo-ring
  {:position      :absolute
   :top           0
   :left          0
   :right         0
   :bottom        0
   :border-radius 12
   :border-width  1
   :border-color  colors/neutral-80-opa-5})

(def emoji-dash
  {:flex-direction :row})

(def emoji
  {:width  20
   :height 20})

(def header-counter-text {:color colors/neutral-40})

(defn input-container
  [theme input blur?]
  (when-not (= input :recovery-phrase)
    {:border-bottom-width 1
     :border-color        (if blur?
                            (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
                            (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))}))

(def search-input-container
  {:flex              0
   :margin-top        8
   :margin-bottom     12
   :margin-horizontal 20})

(def recovery-phrase-container
  {:padding-vertical nil
   :padding-top      4
   :padding-bottom   12})
