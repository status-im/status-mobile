(ns status-im.data-store.discover
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.discover :as data-store]
            [status-im.utils.handlers :as handlers]))

;; stores a collection of discover messages
;; removes the tags from the discovers because there is no
;; need to store them and realm doesn't support lists
;; of string
;; also deletes the oldest queries if the number of discovers stored is
;; above maximum-number-of-discoveries
(re-frame/reg-fx
  :data-store/save-all-discoveries
  (fn [[discovers maximum-number-of-discoveries]]
    (data-store/save-all (mapv #(dissoc % :tags) discovers))
    (data-store/delete :created-at :asc maximum-number-of-discoveries)))

(defn get-all
  ;; extracts the hashtags from the status and put them into a set
  ;; for each discover
  ;; returns a map of discovers that can be used as is in the app-db
  []
  (reduce (fn [acc {:keys [message-id status] :as discover}]
            (let [tags     (handlers/get-hashtags status)
                  discover (assoc discover :tags tags)]
              (assoc acc message-id discover)))
          {}
          (data-store/get-all-as-list :asc)))

(re-frame/reg-cofx
  :data-store/discoveries
  (fn [cofx _]
    (assoc cofx :data-store/discoveries (get-all))))
