(ns legacy.status-im.ui.components.list.header
  (:require
    [legacy.status-im.ui.components.spacing :as spacing]
    [legacy.status-im.ui.components.text :as text]
    [react-native.core :as rn]
    [utils.reagent :as reagent]))

(defn header
  [& this]
  (let [{:keys [color]
         :or   {color :secondary}}
        (apply reagent/props this)]
    [rn/view
     {:style (merge (:base spacing/padding-horizontal)
                    (:x-tiny spacing/padding-vertical))}
     (into [text/text
            {:color color
             :style {:margin-top 10}}]
           (apply reagent/children this))]))
