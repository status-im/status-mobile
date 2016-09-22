(ns status-im.components.text-field.styles)


(def text-field-container
  {:position       :relative
   :height         72
   :padding-top    30
   :padding-bottom 7})

(def text-input
  {:font-size           16
   :height              34
   :line-height         34
   :padding-bottom      5
   :text-align-vertical :top})

(defn label [top font-size color]
  {:position         :absolute
   :top              top
   :left             0
   :color            color
   :font-size        font-size
   :background-color :transparent})

(def label-float
  {})

(defn underline-container [background-color]
  {:background-color background-color
   :height           1
   :align-items      :center})

(defn underline [background-color width]
  {:background-color background-color
   :height           1
   :width            width})

(defn error-text [color]
  {:color            color
   :background-color :transparent
   :font-size        12})
