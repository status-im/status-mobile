(ns status-im.discovery.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(defn- get-discoveries-by-tags [{:keys [discoveries current-tag]} tags limit]
  (let [tags'       (or tags [current-tag])
        filter-tag  (filter #(every? (->> (map :name (:tags %))
                                          (into (hash-set)))
                                     tags'))
        xform       (if limit
                      (comp filter-tag (take limit))
                      filter-tag)]
    (into [] xform discoveries)))

(register-sub :get-discoveries-by-tags
  (fn [db [_ tags limit]]
    (-> (get-discoveries-by-tags @db tags limit)
        (reaction))))

(register-sub :get-popular-tags
  (fn [db [_ limit]]
    (-> (take limit (:tags @db))
        (reaction))))

(register-sub :get-discovery-search-results
  (fn [db _]
    (let [tags (get-in @db [:discovery-search-tags])]
      (-> (get-discoveries-by-tags @db tags nil)
          (reaction)))))
