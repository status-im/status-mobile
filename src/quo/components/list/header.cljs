(ns quo.components.list.header
  (:require [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.design-system.spacing :as spacing]
            [quo.components.text :as text]))

(defn header []
  (let [this                       (reagent/current-component)
        {:keys [color]
         :or   {color :secondary}} (reagent/props this)]
    [rn/view {:style (merge (:base spacing/padding-horizontal)
                            (:x-tiny spacing/padding-vertical))}
     (into [text/text {:color color
                       :style {:margin-top 10}}]
           (reagent/children this))]))
