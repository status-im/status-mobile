(ns quo2.components.wallet.transaction-progress.style
  (:require [quo2.foundations.colors :as quo2.colors] 
            [quo2.foundations.typography :as typography]))

(def card-styles
  {:width         355
   :border-width  1
   :border-color  (quo2.colors/theme-colors quo2.colors/neutral-10 quo2.colors/neutral-40)  
   :border-radius 16
   :margin-right  8})

(def card-header-styles
  {:flex-direction      "row"
   :padding-horizontal  12
   :padding-vertical    10
   :border-bottom-width 1
   :border-color        (quo2.colors/theme-colors quo2.colors/neutral-10 quo2.colors/neutral-40)  
   :align-items         :center})

(def card-status-styles
  {:flex-direction      "row"
   :padding-horizontal  12
   :padding-vertical    2
   :align-items         :center})

(def card-header-left-styles
  {:flex  1})

(def card-header-center-styles
  {:flex        7
   :align-items :flex-start})

(def card-header-right-styles
  {:flex         3
   :align-items :flex-end})

(def card-header-title-styles 
  (merge
  {:color (quo2.colors/theme-colors quo2.colors/neutral-100 quo2.colors/white)}
   typography/font-semi-bold 
   typography/paragraph-1))

(def card-status-text-styles 
  (merge 
   {:color (quo2.colors/theme-colors quo2.colors/neutral-100 quo2.colors/white)} 
   typography/font-regular 
   typography/paragraph-2))

(def card-progress-text-styles 
  (merge 
   {:color (quo2.colors/theme-colors quo2.colors/neutral-100 quo2.colors/neutral-40)}
   typography/font-regular 
   typography/paragraph-2))

(def progress-bar-styles
  {:width 8
   :margin 2
   :heigth 12})

(def progress-bar-child-styles
  {:position      "absolute"
   :bottom        0
   :left          0
   :width         12
   :height        8
   :border-width  1
   :border-color  quo2.colors/neutral-10
   :border-radius 4
   :transform [{:rotate "-90deg"}]})

(def progress-bar-circle-horizontal-styles
  {:flex-direction    "row"
   :margin-vertical   5
   :margin-horizontal 15})

(def progress-bar-circle-horizontal-child-left-styles
   {:width         "5%"
    :height        12
    :border-radius 4
    :margin        2})

(def progress-bar-circle-horizontal-child-right-styles
   {:border-radius 4
    :width        "95%"
    :height       12
    :margin       2})