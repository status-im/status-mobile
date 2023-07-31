(ns quo2.components.tabs.tabs.style
  (:require [oops.core :refer [oget]]
            [quo2.components.tabs.tab.view :as tab]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [react-native.masked-view :as masked-view]
            [reagent.core :as reagent]
            [utils.collection :as utils.collection]
            [utils.number]
            [react-native.gesture :as gesture]))

(def linear-gradient {:width  "100%"
                      :height "100%"})


(defn tab [{:keys [size default-tab-size number-of-items index style] }]
  {:margin-right  (if (= size default-tab-size) 12 8)
   :padding-right (when (= index (dec number-of-items))
                    (:padding-left style))})
