(ns status-chat.components.chat.header
  (:require [quo.foundations.colors :as quo.colors]
            [react-native.core :as rn]))

(defn view
  [& children]
  (into [rn/view
         {:style {:height           80
                  :border-radius    20
                  :background-color quo.colors/white-opa-20}}]
        children))
