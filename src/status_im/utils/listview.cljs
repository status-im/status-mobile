(ns status-im.utils.listview
  (:require-macros [natal-shell.data-source :refer [data-source]]))

(defn clone-with-rows [ds rows]
  (.cloneWithRows ds (reduce (fn [ac el] (.push ac el) ac)
                             (clj->js []) rows)))

(defmacro data-source [config]
  (js/ReactNative.ListView.DataSource. (clj->js config)))

(defn to-datasource [items]
  (clone-with-rows (data-source {:rowHasChanged not=}) items))

(defn clone-with-rows-inverted [ds rows]
  (let [rows (reduce (fn [ac el] (.push ac el) ac)
                     (clj->js []) (reverse rows))
        row-ids (.reverse (.map rows (fn [_ index] index)))]
    (.cloneWithRows ds rows row-ids)))

(defn to-datasource-inverted [items]
  (clone-with-rows-inverted (data-source {:rowHasChanged not=}) items))
