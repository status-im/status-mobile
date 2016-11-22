(ns status-im.discover.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [status-im.utils.datetime :as time]))

(defn- calculate-priority [{:keys [chats contacts current-public-key]}
                           {:keys [whisper-id created-at]}]
  (let [contact               (get contacts whisper-id)
        chat                  (get chats whisper-id)
        seen-online-recently? (< (- (time/now-ms) (get contact :last-online))
                                 time/hour)
        me?                   (= current-public-key whisper-id)]
    (+ created-at                                           ;; message is newer => priority is higher
       (if (or me? contact) time/day 0)                     ;; user exists in contact list => increase priority
       (if (or me? chat) time/day 0)                        ;; chat with this user exists => increase priority
       (if (or me? seen-online-recently?) time/hour 0)      ;; the user was online recently => increase priority
       )))

(defn- get-discoveries-by-tags [discoveries current-tag tags]
  (let [tags' (or tags [current-tag])]
    (filter #(every? (->> (map :name (:tags %))
                          (into (hash-set)))
                     tags')
            (vals discoveries))))

(register-sub :get-popular-discoveries
  (fn [db [_ limit tags]]
    (let [discoveries (reaction (:discoveries @db))
          current-tag (reaction (:current-tag @db))
          search-tags (reaction (:discover-search-tags @db))
          discoveries (->> (get-discoveries-by-tags @discoveries @current-tag (or tags @search-tags))
                           (map #(assoc % :priority (calculate-priority db %)))
                           (sort-by :priority >))]
      (reaction {:discoveries (take limit discoveries)
                 :total       (count discoveries)}))))

(register-sub :get-recent-discoveries
  (fn [db]
    (->> (:discoveries @db)
         (vals)
         (reaction))))

(register-sub :get-popular-tags
  (fn [db [_ limit]]
    (-> (take limit (:tags @db))
        (reaction))))

(register-sub :get-discover-search-results
  (fn [db _]
    (let [discoveries (reaction (:discoveries @db))
          current-tag (reaction (:current-tag @db))
          tags        (reaction (:discover-search-tags @db))]
      (-> (get-discoveries-by-tags @discoveries @current-tag @tags)
          (reaction)))))
