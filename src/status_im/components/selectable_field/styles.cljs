(ns status-im.components.selectable-field.styles
  (:require [status-im.utils.platform :refer [platform-specific]]))


(def selectable-field-container
  {})

(def label-container
  {:margin-bottom 13})

(def label
  {:color            "#838c93"
   :background-color :transparent
   :font-size        14})

(def text-container
  {:padding       0
   :margin-bottom 18
   :margin        0})

(def text
  {:font-size           16
   :color               "#555555"
   :margin-right        16
   :text-align-vertical :top})

(defn sized-text
  [height]
  (let [{:keys [additional-height
                margin-top]} (get-in platform-specific [:component-styles :sized-text])]
    (merge text {:height         (+ additional-height height)
                 :margin-bottom  0
                 :margin-top     margin-top
                 :padding-top    0
                 :padding-left   0
                 :margin-left    0
                 :padding-bottom 0})))

