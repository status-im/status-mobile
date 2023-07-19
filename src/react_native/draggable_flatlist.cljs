(ns react-native.draggable-flatlist
  (:require
    [react-native.flat-list :as rn-flat-list]
    [reagent.core :as reagent]
    ["react-native-draggable-flatlist" :default DraggableFlatList]))


(def rn-draggable-flatlist (reagent/adapt-react-class DraggableFlatList))

(defn draggable-flatlist
  [props]
  [rn-draggable-flatlist (rn-flat-list/base-list-props props)])
