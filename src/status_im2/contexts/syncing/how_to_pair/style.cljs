(ns status-im2.contexts.syncing.how-to-pair.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]))

(def container-outer
  {:flex               (if platform/ios? 4.5 5)
   :padding-top        20
   :padding-bottom     8
   :padding-horizontal 16
   :background-color   colors/neutral-95})

(def heading
  {:margin-bottom 12
   :color         colors/white})

(def tabs-container
  {:margin-top    8
   :margin-bottom 12})

(def paragraph
  {:margin-vertical 8
   :color           colors/white})

(def hr
  {:margin-top          12
   :margin-bottom       6
   :border-bottom-color colors/white-opa-10
   :border-bottom-width 1})

(def image
  {:border-radius 16})

(def container-image
  {:flex-direction   :row
   :aspect-ratio     1
   :justify-content  :flex-end
   :align-items      :flex-end
   :overflow         :hidden
   :background-color colors/white-opa-5
   :border-radius    16})

(def list-text
  {:margin-horizontal 2
   :color             colors/white})

(def list-icon
  {:height          18
   :width           18
   :border-radius   6
   :border-width    1
   :border-color    colors/white-opa-10
   :align-items     :center
   :justify-content :center})

(def list-icon-text
  {:color colors/white})

(def button-grey
  {:margin-horizontal 2})

(def button-grey-placeholder
  {:margin-horizontal 2})

(def button-primary
  {:margin-horizontal 2})

(def numbered-list
  {:margin-top 12})

(def numbered-list-item
  {:flex-direction :row
   :align-items    :center})
