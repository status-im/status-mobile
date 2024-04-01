(ns status-im.contexts.profile.settings.screens.password.change-password.style
  (:require
    [quo.foundations.colors :as colors]))

(def form-container
  {:flex               1
   :padding-horizontal 20})

(def heading
  {:margin-bottom 20
   :margin-top    12})

(def heading-subtitle {:color colors/white})

(def heading-title (assoc heading-subtitle :margin-bottom 8))

(def info-message
  {:margin-top 8})

(def space-between-inputs {:height 16})

(def error-container
  {:margin-top     8
   :flex-direction :row
   :align-items    :center})

(def warning-container
  {:margin-top        4
   :margin-horizontal 20
   :margin-bottom     12})

(defn loading-container
  [insets]
  {:flex            1
   :justify-content :space-between
   :padding-top     (:top insets)
   :padding-bottom  (:bottom insets)})

(def loading-content
  {:flex 1})

(def logout-container
  {:margin-horizontal 20
   :margin-vertical   12})

(def password-tips
  {:flex-direction    :row
   :margin-horizontal 20
   :justify-content   :space-between})

(def bottom-part
  {:position :absolute
   :bottom   0
   :left     0
   :right    0})

(def disclaimer-container
  {:margin-horizontal 20
   :margin-vertical   4})

(def button-container
  {:margin-vertical   12
   :margin-horizontal 20})
