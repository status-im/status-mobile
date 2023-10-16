(ns status-im.ui.components.list.footer
  (:require
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.ui.components.spacing :as spacing]
    [status-im.ui.components.text :as text]))

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
