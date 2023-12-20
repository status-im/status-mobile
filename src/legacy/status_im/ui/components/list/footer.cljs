(ns legacy.status-im.ui.components.list.footer
  (:require
    [legacy.status-im.ui.components.spacing :as spacing]
    [legacy.status-im.ui.components.text :as text]
    [react-native.core :as rn]
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
