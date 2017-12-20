(ns status-im.ui.components.sortable-list-view
  (:require [reagent.core :as r]
            [status-im.ui.components.react :refer [view
                                                touchable-highlight
                                                list-item]]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def sortable-listview-class
  (r/adapt-react-class rn-dependencies/sortable-listview))

(defn sortable-list-view [{:keys [on-row-moved render-row] :as props}]
  [sortable-listview-class
   (assoc props :on-row-moved #(on-row-moved (js->clj % :keywordize-keys true))
                :render-row #(render-row (js->clj % :keywordize-keys true)))])

(defn touchable [inner]
  [touchable-highlight (js->clj (.-props (r/current-component)))
   [view
    inner]])

(defn sortable-item [inner]
  (list-item [touchable inner]))
