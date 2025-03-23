(ns status-im.common.refreshable-flat-list.view
  (:require [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn view
  [{:keys [refresh-control] :as props}]
  [rn/flat-list
   (merge {:refresh-control (reagent/as-element
                             refresh-control)}
          (dissoc props :refresh-control))])
