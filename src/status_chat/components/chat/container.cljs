(ns status-chat.components.chat.container
  (:require [react-native.core :as rn]
            [react-native.safe-area :as safe-area]))

(defn view
  [& children]
  (into [rn/view
         {:style {:flex               1
                  :padding-horizontal 8
                  :padding-bottom     8
                  :margin-top         safe-area/top}}]
        children))
