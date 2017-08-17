(ns status-im.components.list.views
  (:require [status-im.components.react :as rn]
            [reagent.core :as r]
            [status-im.components.common.common :as common]
            [status-im.utils.platform :as p]))

(def flat-list-class (rn/get-class "FlatList"))

(defn- wrap-render-fn [f]
  (fn [o]
    (let [{:keys [item index separators]} (js->clj o :keywordize-keys true)]
      (r/as-element (f item index separators)))))

(defn flat-list
  "A wrapper for FlatList.
   See https://facebook.github.io/react-native/docs/flatlist.html"
  ([data render-fn] (flat-list data render-fn {}))
  ([data render-fn props]
   [flat-list-class (merge {:data (clj->js data)
                            :renderItem (wrap-render-fn render-fn)
                            :keyExtractor (fn [_ i] i)}
                           (when p/ios? {:ItemSeparatorComponent (fn [] (r/as-element [common/list-separator]))})
                           props)]))