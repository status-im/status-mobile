(ns syng-im.utils.listview
  (:require-macros [natal-shell.data-source :refer [data-source clone-with-rows]])
  (:require [syng-im.components.realm]))

(defn to-datasource [items]
  (clone-with-rows (data-source {:rowHasChanged not=}) items))

(defn to-realm-datasource [items]
  (-> (cljs.core/clj->js {:rowHasChanged not=})
      (js/RealmReactNative.ListView.DataSource.)
      (clone-with-rows items)))


(defn clone-with-rows2 [ds rows]
  (.cloneWithRows ds (reduce (fn [ac el] (.push ac el) ac)
                             (clj->js []) rows)))

(defn to-datasource2 [items]
  (clone-with-rows2 (data-source {:rowHasChanged not=}) items))
