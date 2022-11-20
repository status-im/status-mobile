(ns status-im.ui2.screens.group.styles
  (:require [quo.design-system.colors :as colors]
            [quo2.foundations.colors :as quo2.colors]))

(def group-container
  {:flex             1
   :flex-direction   :column
   :background-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)})

(defn no-contact-text []
  {:margin-bottom     20
   :margin-horizontal 50
   :text-align        :center
   :color             colors/gray})

(def toolbar-header-container
  {:align-items :center})

(defn toolbar-sub-header []
  {:color colors/gray})

(def no-contacts
  {:flex 1
   :justify-content :center
   :align-items :center})

(defn search-container []
  {:border-bottom-color colors/gray-lighter
   :background-color    (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)
   :padding-horizontal  20
   :padding-top         10})

(defn members-title []
  {:color         colors/gray
   :margin-top    14
   :margin-bottom 4})
