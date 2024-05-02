(ns quo.components.refreshable-scroll-view.view
  (:require [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn view
  [{:keys [refresh-control] :as props} children]
  [rn/scroll-view
   (merge {:refresh-control (reagent/as-element
                             refresh-control)}
          (dissoc props :refresh-control))
   children])
