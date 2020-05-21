(ns quo.components.list.header
  (:require [quo.react-native :as rn]
            [quo.design-system.spacing :as spacing]
            [quo.components.text :as text]))

(defn header [& children]
  [rn/view {:style (merge (:base spacing/padding-horizontal)
                          (:x-tiny spacing/padding-vertical))}
   (into [text/text {:color :secondary
                     :style {:margin-top 10}}]
         children)])
