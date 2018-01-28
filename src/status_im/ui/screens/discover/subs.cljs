(ns status-im.ui.screens.discover.subs
  (:require [re-frame.core :refer [reg-sub]]
            [status-im.utils.datetime :as time]))

(defn- calculate-priority [now-ms chats current-public-key contacts
                           {:keys [whisper-id created-at]}]
  (let [contact               (get contacts whisper-id)
        chat                  (get chats whisper-id)
        seen-online-recently? (< (- now-ms (get contact :last-online))
                                 time/hour)
        me?                   (= current-public-key whisper-id)]
    (+ created-at                                         ; message is newer => priority is higher
       (if (or me? contact) time/day 0)                   ; user exists in contact list => increase priority
       (if (or me? chat) time/day 0)                      ; chat with this user exists => increase priority
       (if (or me? seen-online-recently?) time/hour 0)))) ; the user was online recently => increase priority


(defn- get-discoveries-by-tags [discoveries-by-tags search-tags]
  (reduce (fn [acc search-tag]
            (concat acc (get discoveries-by-tags search-tag [])))
          []
          search-tags))

(reg-sub :discover/discoveries :discoveries)

(reg-sub :discover/discoveries-with-priority
  :<- [:discover/discoveries]
  :<- [:get-active-chats]
  :<- [:get-contacts]
  :<- [:get :current-public-key]
  (fn [[discoveries chats contacts current-public-key]]
    (let [now-ms (time/now-ms)]
      (map #(assoc % :priority (calculate-priority now-ms chats current-public-key contacts %)) (vals discoveries)))))

(reg-sub :discover/search-tags :discover-search-tags)

(reg-sub :discover/tags
  :<- [:discover/discoveries]
  (fn [discoveries]
    (reduce (fn [acc {:keys [tags]}]
              (into acc tags))
            #{}
            (vals discoveries))))


;; TODO(yenda) this is not really the most recent discoveries
;; it's just all off them
(reg-sub :discover/recent-discoveries
  :<- [:discover/discoveries]
  (fn [discoveries]
    (sort-by :created-at > (vals discoveries))))

(reg-sub :discover/discoveries-by-tags
  :<- [:discover/discoveries-with-priority]
  (fn [discoveries]
    (reduce (fn [discoveries-by-tags {:keys [tags] :as discovery}]
              (reduce (fn [discoveries-by-tags tag]
                        (update discoveries-by-tags tag conj discovery))
                      discoveries-by-tags
                      tags))
            {}
            discoveries)))

(reg-sub :discover/most-popular-hashtags
  :<- [:discover/discoveries-by-tags]
  (fn [discoveries]
    (->> discoveries
         (sort-by (comp count val) >)
         (take 10))))

(reg-sub :discover/popular-hashtags-preview
  :<- [:discover/most-popular-hashtags]
  (fn [most-popular-hashtags]
    (->> most-popular-hashtags
         (map (fn [[tag discoveries]] {:tag       tag
                                       :total     (count discoveries)
                                       :discovery (first (sort-by :priority > discoveries))})))))

(reg-sub :discover/all-popular-hashtags
  :<- [:discover/most-popular-hashtags]
  (fn [most-popular-hashtags]
    (let [tags        (map first most-popular-hashtags)
          discoveries (apply concat (map second most-popular-hashtags))]
      {:tags        tags
       :discoveries (sort-by :priority > (distinct discoveries))})))


(reg-sub :discover/search-results
  :<- [:discover/discoveries-by-tags]
  :<- [:discover/search-tags]
  :<- [:get-active-chats]
  :<- [:get-contacts]
  :<- [:get :current-public-key]
  (fn [[discoveries search-tags chats contacts current-public-key] [_ limit]]
    (let [discoveries (->> (get-discoveries-by-tags discoveries search-tags)
                           (sort-by :priority >))]
      {:discoveries (take limit discoveries)
       :tags        search-tags
       :total       (count discoveries)})))

(reg-sub :discover/all-dapps
  (fn [db]
    (let [dapp? (->> (get-in db [:group/contact-groups "dapps" :contacts])
                     (map :identity)
                     set)]
      (->> (:contacts/contacts db)
           (filter #(-> % key dapp?))
           (into {})))))
