(ns syng-im.discovery.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub :get-discoveries-by-tag
  (fn [db [_ tag limit]]
    (let [discoveries (reaction (:discoveries @db))
          tag'        (or tag (:current-tag @db))
          filter-tag  (filter #(some #{tag'} (map :name (:tags %))))
          xform       (if limit
                        (comp filter-tag (take limit))
                        filter-tag)]
      (->> @discoveries
           (into [] xform)
           (reaction)))))

(register-sub :get-popular-tags
  (fn [db [_ limit]]
    (reaction (take limit (:tags @db)))))

