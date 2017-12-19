(ns status-im.ui.components.text-field.styles
  (:require [status-im.utils.platform :refer [platform-specific]]))


(def text-field-container
  {:position       :relative
   :height         72
   :padding-top    30
   :padding-bottom 34})

(def text-input
  {:font-size           16
   :height              34
   :line-height         34
   :padding-bottom      5
   :text-align-vertical :top})

(defn label [top font-size color]
  (let [input-label-style (get-in platform-specific [:component-styles :input-label])]
    (merge input-label-style
           {:position         :absolute
            :top              top
            :color            color
            :font-size        font-size
            :background-color :transparent})))

(def label-float
  {})

(defn underline-container [background-color]
  {:background-color background-color
   :align-items      :center})

(defn underline [background-color width height]
  {:background-color background-color
   :height           height
   :width            width})

(defn error-text [color]
  (let [input-error-text-style (get-in platform-specific [:component-styles :input-error-text])]
    (merge input-error-text-style
           {:color            color
            :background-color :transparent
            :font-size        12
            :line-height      20})))
