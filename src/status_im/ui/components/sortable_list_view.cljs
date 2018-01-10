(ns status-im.ui.components.sortable-list-view
  (:require [reagent.core :as r]
            [status-im.ui.components.react :as react]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(def sortable-listview-class
  (r/adapt-react-class rn-dependencies/sortable-listview))

(defn sortable-list-view [{:keys [on-row-moved render-row] :as props}]
  [sortable-listview-class
   (assoc props :on-row-moved #(on-row-moved (js->clj % :keywordize-keys true))
                :render-row #(render-row (js->clj % :keywordize-keys true)))])

(defn- touchable [inner]
  [react/touchable-highlight (js->clj (.-props (r/current-component)))
   [react/view
    inner]])

(defn sortable-item [inner]
  (react/list-item [touchable inner]))
