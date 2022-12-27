(ns quo.components.list.header
  (:require [quo.components.text :as text]
            [quo.design-system.spacing :as spacing]
            [quo.react-native :as rn]
            [reagent.core :as reagent]))

(defn header
  []
  (let [this (reagent/current-component)
        {:keys [color]
         :or   {color :secondary}}
        (reagent/props this)]
    [rn/view
     {:style (merge (:base spacing/padding-horizontal)
                    (:x-tiny spacing/padding-vertical))}
     (into [text/text
            {:color color
             :style {:margin-top 10}}]
           (reagent/children this))]))
