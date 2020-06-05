(ns quo.components.list.footer
  (:require [quo.react-native :as rn]
            [quo.design-system.spacing :as spacing]
            [quo.components.text :as text]))

(defn footer [& children]
  [rn/view {:style (merge (:base spacing/padding-horizontal)
                          (:small spacing/padding-vertical))}
   (into [text/text {:color :secondary}]
         children)])

