(ns status-im.ui.components.connectivity.styles
  (:require-macros [status-im.utils.styles :refer [defnstyle]])
  (:require [status-im.ui.components.colors :as colors]
            [status-im.utils.platform :as platform]))

(defnstyle text-wrapper [{:keys [top window-width pending? modal?]}]
  (cond->
   {:opacity          1.0
    :background-color colors/gray-notifications
    :height           35
    :position         :absolute}
    platform/desktop?
    (assoc
     :left 0
     :right 0)
    (not platform/desktop?)
    (assoc
     :ios {:z-index 0}
     :width window-width
     :top (+ top
             (if (and modal? platform/android?) 31 56)
             (if pending? 35 0)))))

(def text
  {:text-align :center
   :color      :white
   :font-size  14
   :top        8})
