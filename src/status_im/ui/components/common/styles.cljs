(ns status-im.ui.components.common.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]))

(def separator-wrapper
  {:background-color colors/white})

(defstyle separator
  {:android {:height 0}
   :ios     {:height           1
             :background-color colors/black-transparent
             :opacity          0.5}})

(def list-separator
  {:margin-left 72})

(def network-container
  {:flex-direction     :row
   :padding-horizontal 13
   :padding-vertical   11
   :align-items        :center})

(def label-action-text
  {:color colors/blue})

(defn logo-container [size]
  {:width            size
   :height           size
   :border-radius    size
   :background-color colors/blue
   :align-items      :center
   :justify-content  :center})

(defn logo [icon-size]
  {:width  icon-size
   :height icon-size
   :container-style {}})

(def bottom-button
  {:flex-direction :row
   :align-items    :center})

(defn button [style background? disabled?]
  (merge
   {:padding-vertical   12
    :padding-horizontal 42
    :border-radius      8
    :background-color   (cond disabled?
                              colors/gray-lighter
                              background?
                              colors/blue-transparent-10)}
   style))

(def button-label
  {:text-align :center
   :color      colors/blue})

(defn counter-container [size]
  {:width            size
   :height           size
   :border-radius    (/ size 2)
   :background-color colors/blue
   :align-items      :center
   :justify-content  :center})

(defn counter-label [size]
  {:font-size  (inc (/ size 2))
   :typography :main-medium
   :color      colors/white
   :text-align :center})

(def image-contain
  {:align-self      :stretch
   :align-items     :center
   :justify-content :center})
