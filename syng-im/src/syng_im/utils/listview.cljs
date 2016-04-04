(ns syng-im.utils.listview
  (:require-macros [natal-shell.data-source :refer [data-source clone-with-rows]])
  (:require [syng-im.components.realm]))

(defn to-datasource [items]
  (-> (data-source {:rowHasChanged (fn [row1 row2]
                                     (not= row1 row2))})
      (clone-with-rows items)))

(defn to-realm-datasource [items]
  (-> (js/RealmReactNative.ListView.DataSource. (cljs.core/clj->js {:rowHasChanged (fn [row1 row2]
                                                                                  (not= row1 row2))}))
      (clone-with-rows items)))