(ns status-chat.components.chat.body
  (:require [react-native.core :as rn]))

(defn view
  [& children]
  (into [rn/view {:style {:flex 1 :padding-vertical 20}}] children))
