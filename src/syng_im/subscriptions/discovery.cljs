(ns syng-im.subscriptions.discovery
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.db :as db]
            [syng-im.utils.logging :as log]
            [syng-im.models.discoveries :refer [discovery-list
                                                current-tag
                                                get-tag-popular
                                                discoveries-by-tag
                                                current-tag-updated?
                                                discoveries-updated?]]))



(register-sub :get-discoveries
              (fn [db _]
                (let [discoveries-updated (-> (discoveries-updated? @db)
                                            (reaction))]
                  (reaction
                    (let [_ @discoveries-updated]
                      (discovery-list))))))

(register-sub :get-discoveries-by-tag
              (fn [db [_ tag limit]]
                (let [discoveries-updated (-> (discoveries-updated? @db)
                                            (reaction))]
                  (log/debug "Getting discoveries for: " tag)
                  (reaction
                    (let [_ @discoveries-updated]
                      (discoveries-by-tag tag limit))))))

(register-sub :get-popular-tags
              (fn [db [_ limit]]
                (let [discoveries-updated (-> (discoveries-updated? @db)
                                            (reaction))]
                  (log/debug  "Getting tags limited: " limit)
                  (reaction
                    (let [_ @discoveries-updated]
                      (get-tag-popular limit))))))

(register-sub :get-current-tag
              (fn [db _]
                (let [current-tag-updated (-> (current-tag-updated? @db)
                                              (reaction))]
                  (reaction
                    (let [_ @current-tag-updated]
                      (current-tag @db))))))

