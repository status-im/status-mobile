(ns status-im.components.sortable-list-view
  (:require [reagent.core :as r]
            [status-im.components.react :refer [view
                                                touchable-highlight
                                                list-item]]))

(def sortable-list-view-class (r/adapt-react-class (js/require "react-native-sortable-listview")))

(defn sortable-list-view [{:keys [on-row-moved render-row] :as props}]
  [sortable-list-view-class
   (assoc props :on-row-moved #(on-row-moved (js->clj % :keywordize-keys true))
                :render-row #(render-row (js->clj % :keywordize-keys true)))])

(defn touchable [inner]
  [touchable-highlight (js->clj (.-props (r/current-component)))
   [view
    inner]])

(defn sortable-item [inner]
  (list-item [touchable inner]))
