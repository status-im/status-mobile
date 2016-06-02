(ns status-im.utils.listview
  (:require-macros [natal-shell.data-source :refer [data-source]]))

(defn clone-with-rows [ds rows]
  (.cloneWithRows ds (reduce (fn [ac el] (.push ac el) ac)
                             (clj->js []) rows)))

(defn to-datasource [items]
  (clone-with-rows (data-source {:rowHasChanged not=}) items))
