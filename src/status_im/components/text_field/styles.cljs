(ns status-im.components.text-field.styles)


(def text-field-container
  {:position :relative
   :height 72
   :paddingTop 30
   :paddingBottom 7})

(def text-input
  {:fontSize 16
   :height 34
   :lineHeight 34
   :paddingBottom 5
   :textAlignVertical :top})

(defn label [top font-size color]
  {:position :absolute
   :top top
   :left 0
   :color color
   :fontSize font-size
   :backgroundColor :transparent})

(def label-float
  {})

(defn underline-container [backgroundColor]
  {:backgroundColor backgroundColor
   :height 1
   :alignItems :center})

(defn underline [backgroundColor width]
  {:backgroundColor backgroundColor
   :height 1
   :width width})

(defn error-text [color]
  {:color color
   :fontSize 12})
