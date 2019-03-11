(ns status-im.ui.components.text-input.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as p]))

(def label
  {:font-size 14
   :color     colors/black})

(defn input-container [height]
  {:padding          16
   :justify-content  :center
   :margin-vertical  8
   :height           (or height 52)
   :border-radius    8
   :background-color colors/gray-lighter})

(defstyle input
  {:font-size 15
   :color     colors/black
   :padding   0
   :desktop   {:height 52}})

(defn error [label?]
  {:bottom-value (if label? -20 0)
   :color        colors/red-light
   :font-size    12})
