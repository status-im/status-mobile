(ns status-im.ui.screens.discover.subs
  (:require [re-frame.core :refer [reg-sub]]
            [status-im.utils.datetime :as time]))

(reg-sub :get-discoveries :discoveries)

(reg-sub :get-current-tag :current-tag)

(reg-sub :get-discover-search-tags :discover-search-tags)

(reg-sub :get-tags :tags)

(defn- calculate-priority [chats current-public-key contacts
                           {:keys [whisper-id created-at]}]
  (let [contact               (get contacts whisper-id)
        chat                  (get chats whisper-id)
        seen-online-recently? (< (- (time/now-ms) (get contact :last-online))
                                 time/hour)
        me?                   (= current-public-key whisper-id)]
    (+ created-at                                         ; message is newer => priority is higher
       (if (or me? contact) time/day 0)                   ; user exists in contact list => increase priority
       (if (or me? chat) time/day 0)                      ; chat with this user exists => increase priority
       (if (or me? seen-online-recently?) time/hour 0)))) ; the user was online recently => increase priority


(defn- get-discoveries-by-tags [discoveries current-tag tags]
  (let [tags' (or tags [current-tag])]
    (filter #(some (->> (:tags %)
                        (map :name)
                        (into (hash-set)))
                   tags')
            (vals discoveries))))

(reg-sub
  :get-popular-discoveries
  :<- [:get-discoveries]
  :<- [:get-current-tag]
  :<- [:get-discover-search-tags]
  :<- [:chats]
  :<- [:get-contacts]
  :<- [:get-current-public-key]
  (fn [[discoveries current-tag discover-search-tags chats contacts public-key]
       [_ limit tags]]
    (let [discoveries (->> (get-discoveries-by-tags discoveries
                                                    current-tag
                                                    (or tags discover-search-tags))
                           (map #(assoc % :priority (calculate-priority chats public-key contacts %)))
                           (sort-by :priority >))]
      {:discoveries (take limit discoveries)
       :total       (count discoveries)})))

(reg-sub
  :get-top-discovery-per-tag
  :<- [:get-discoveries]
  :<- [:get-tags]
  (fn [[discoveries tags] [_ limit]]
    (let [tag-names (map :name (take limit tags))]
      (for [tag tag-names]
        (let [results (get-discoveries-by-tags discoveries tag nil)]
          [tag {:discovery (first results)
                :total     (count results)}])))))

(reg-sub
  :get-recent-discoveries
  :<- [:get-discoveries]
  (fn [discoveries]
    (sort-by :created-at > (vals discoveries))))

(reg-sub
  :get-popular-tags
  :<- [:get-tags]
  (fn [tags [_ limit]]
    (take limit tags)))

(reg-sub
  :get-discover-search-results
  :<- [:get-discoveries]
  :<- [:get-current-tag]
  :<- [:get-discover-search-tags]
  (fn [[discoveries current-tag discover-search-tags]]
    (get-discoveries-by-tags discoveries current-tag discover-search-tags)))

(reg-sub
  :get-all-dapps
  :<- [:get-contact-groups]
  :<- [:get-contacts]
  (fn [[groups contacts]]
    (let [dapp-ids (into #{} (map :identity) (get-in groups ["dapps" :contacts]))]
      (select-keys contacts dapp-ids))))
