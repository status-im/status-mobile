(ns syng-im.utils.listview
  (:require-macros [natal-shell.data-source :refer [data-source clone-with-rows]]))

(defn to-datasource [msgs]
  (-> (data-source {:rowHasChanged (fn [row1 row2]
                                     (not= row1 row2))})
      (clone-with-rows msgs)))