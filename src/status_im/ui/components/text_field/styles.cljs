(ns status-im.ui.components.text-field.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]]))

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

(defnstyle label [top font-size color]
  {:position         :absolute
   :top              top
   :color            color
   :font-size        font-size
   :background-color :transparent
   :ios              {:left 0}
   :android          {:left 4}})

(def label-float
  {})

(defn underline-container [background-color]
  {:background-color background-color
   :align-items      :center})

(defn underline [background-color width height]
  {:background-color background-color
   :height           height
   :width            width})

(defnstyle error-text [color]
  {:color            color
   :background-color :transparent
   :font-size        12
   :line-height      20
   :ios              {:margin-left 0}
   :android          {:margin-left 4}})
