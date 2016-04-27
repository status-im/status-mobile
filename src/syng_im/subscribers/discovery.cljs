(ns syng-im.subscriptions.discovery
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.db :as db]
            [syng-im.utils.debug :refer [log]]
            [syng-im.models.discoveries :refer [discovery-list
                                                get-tag-popular
                                                discoveries-by-tag
                                                signal-discovery-updated
                                                discovery-updated?]]))



(register-sub :get-discoveries
              (fn [db _]
                (let [discovery-updated (-> (discovery-updated? @db)
                                            (reaction))]
                  (reaction
                    (let [_ @discovery-updated]
                      (discovery-list))))))

(register-sub :get-discoveries-by-tag
              (fn [db [_ tag limit]]
                (let [discovery-updated (-> (discovery-updated? @db)
                                            (reaction))
                      _ (log (str "getting discoveries for: " tag))]
                  (reaction
                    (let [_ @discovery-updated]
                      (discoveries-by-tag tag limit))))))

(register-sub :get-popular-tags
              (fn [db [_ limit]]
                (let [discovery-updated (-> (discovery-updated? @db)
                                            (reaction))
                      _ (log (str "getting tags limited: " limit))]
                  (reaction
                    (let [_ @discovery-updated]
                      (get-tag-popular limit))))))