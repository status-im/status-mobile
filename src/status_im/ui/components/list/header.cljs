(ns status-im.ui.components.list.header
  (:require
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.ui.components.spacing :as spacing]
    [status-im.ui.components.text :as text]))

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
