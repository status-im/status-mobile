(ns syng-im.discovery.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.models.discoveries :refer [current-tag
                                                current-tag-updated?]]))

(register-sub :get-discoveries-by-tag
  (fn [db [_ tag limit]]
    (let [discoveries (reaction (:discoveries @db))]
      (->> @discoveries
           (into [] (comp (filter #(some #{tag} (map :name (:tags %))))
                          (take limit)))
           (reaction)))))

(register-sub :get-popular-tags
  (fn [db [_ limit]]
    (reaction (take limit (:tags @db)))))

(register-sub :get-current-tag
  (fn [db _]
    (let [current-tag-updated (-> (current-tag-updated? @db)
                                  (reaction))]
      (reaction
        (let [_ @current-tag-updated]
          (current-tag @db))))))

