(ns status-im.ui.screens.communities.styles
  (:require [quo.design-system.colors :as colors]))

(def category-item
  {:flex           1
   :flex-direction :row
   :align-items    :center
   :height         52
   :margin-left    18})

(def reorder-categories-item
  {:accessibility-label :reorder-categories-item
   :flex-direction      :row
   :height              60
   :align-items         :center
   :margin-left         18})

(defn reorder-categories-text []
  {:font-size   17
   :margin-left 10
   :color       (colors/get-color :text-01)
   :flex        1})

(defn reorder-categories-button [margin-right]
  {:accessibility-label :reorder-categories-button
   :width               30
   :height              30
   :border-radius       15
   :margin              10
   :margin-right        margin-right
   :border-width        1
   :justify-content     :center
   :align-items         :center
   :border-color        colors/black})
