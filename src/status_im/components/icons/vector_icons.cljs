(ns status-im.components.icons.vector-icons
  (:require-macros [status-im.utils.slurp :refer [slurp]])
  (:require [reagent.core :as r]
            [status-im.utils.platform :refer [ios?]]
            [status-im.components.styles :as common]
            [status-im.components.react :as rn]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def conf (.parse js/JSON (slurp "resources/fontello-config.json")))

(defn adapt-class [class]
  (when class
    (r/adapt-react-class class)))

(def icon-class (adapt-class (.createIconSetFromFontello rn-dependencies/vector-icons conf)))

(defn check-name [n]
  (if (= n :options)
    (if ios? :dots_horizontal :dots_vertical)
    n))

(defn icon [opts]
  (let [{:keys [name color]} (:source opts)]
    [rn/view (merge {:height          24
                     :width           24
                     :align-items     :center
                     :justify-content :center}
                    (:style opts))
     [icon-class {:name  (check-name (or name opts))
                  :color (case color
                           :dark common/icon-dark-color
                           :gray common/icon-gray-color
                           :blue common/color-light-blue
                           :white common/color-white
                           :red common/icon-red-color
                           common/icon-dark-color)
                  :size  24}]]))