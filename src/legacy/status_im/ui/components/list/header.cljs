(ns legacy.status-im.ui.components.list.header
  (:require
    [legacy.status-im.ui.components.spacing :as spacing]
    [legacy.status-im.ui.components.text :as text]
    [react-native.core :as rn]
    [react-native.utils :as rn.utils]))

(defn header
  [& argv]
  (let [[props children] (rn.utils/get-props-and-children argv)
        {:keys [color]
         :or   {color :secondary}}
        props]
    [rn/view
     {:style (merge (:base spacing/padding-horizontal)
                    (:x-tiny spacing/padding-vertical))}
     (into [text/text
            {:color color
             :style {:margin-top 10}}]
           children)]))
