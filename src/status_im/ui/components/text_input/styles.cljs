(ns status-im.ui.components.text-input.styles
  (:require-macros [status-im.utils.styles :refer [defstyle defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as p]))

(defn label [editable]
  (merge
   {:margin-vertical 10}
   (when-not editable {:color colors/gray})))

(defn input-container [height editable]
  (merge
   {:padding          16
    :justify-content  :center
    :height           (or height 52)
    :border-radius    8
    :background-color (when editable colors/gray-lighter)}
   (when-not editable
     {:border-color colors/gray-lighter
      :border-width 1})))

(defstyle input
  {:padding             0
   :text-align-vertical :top
   :desktop             {:height 52}})

(defn error [label?]
  {:bottom-value (if label? 20 0)
   :container-style {:shadow-offset    {:width 0 :height 1}
                     :shadow-radius    6
                     :shadow-opacity   1
                     :shadow-color     colors/gray
                     :elevation        2}
   :font-size    12})
