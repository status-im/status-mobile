(ns quo.components.list.footer
  (:require [quo.components.text :as text]
            [quo.design-system.spacing :as spacing]
            [quo.react-native :as rn]
            [reagent.core :as reagent]))

(defn footer
  []
  (let [this (reagent/current-component)
        {:keys [color]
         :or   {color :secondary}}
        (reagent/props this)]
    [rn/view
     {:style (merge (:base spacing/padding-horizontal)
                    (:small spacing/padding-vertical))}
     (into [text/text {:color color}]
           (reagent/children this))]))
