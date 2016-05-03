(ns syng-im.models.discoveries
  (:require [cljs.core.async :as async :refer [chan put! <! >!]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.utils.logging :as log]
            [syng-im.persistence.realm :as realm]
            [syng-im.persistence.realm :as r]
            [syng-im.resources :as res]
            [syng-im.db :as db]))

(defn signal-discoveries-updated [db]
  (update-in db db/updated-discoveries-signal-path (fn [current]
                                               (if current
                                                 (inc current)
                                                 0))))

(defn discoveries-updated? [db]
  (get-in db db/updated-discoveries-signal-path))

(defn get-tag [tag]
  (log/debug "Getting tag: " tag)
  (-> (r/get-by-field :tag :name tag)
      (r/single-cljs)))

(defn decrease-tag-counter [tag]
  (let [tag (:name tag)
        tag-object (get-tag tag)]
    (if tag-object
      (let [counter (dec (:count tag-object))]
        (if (= counter 0)
          (realm/delete tag-object)
          (realm/create :tag {:name tag
                              :count counter}
                        true))))))

(defn increase-tag-counter [tag]
  (let [tag (:name tag)
        tag-object (get-tag tag)]
    (if tag-object
      (realm/create :tag {:name tag
                          :count (inc (:count tag-object))}
                    true))))

(defn decrease-tags-counter [tags]
  (doseq [tag tags]
    (decrease-tag-counter tag)))

(defn increase-tags-counter [tags]
  (doseq [tag tags]
    (increase-tag-counter tag)))

(defn get-tags [whisper-id]
  (:tags (-> (r/get-by-field :discoveries :whisper-id whisper-id)
             (r/single-cljs))))

(defn- create-discovery [{:keys [name status whisper-id photo location tags last-updated]}]
  (let [tags (mapv (fn [tag] {:name tag}) tags)
        discovery {:name         name
                   :status       status
                   :whisper-id   whisper-id
                   :photo        photo
                   :location     location
                   :tags         tags
                   :last-updated last-updated}]
    (log/debug "Creating discovery: " discovery tags)
    (realm/create :discoveries discovery true)
    (increase-tags-counter tags)))

(defn- update-discovery [{:keys [name status whisper-id photo location tags last-updated]}]
  (let [old-tags (get-tags whisper-id)
        tags (mapv (fn [tag] {:name tag}) tags)
        discovery {:name         name
                   :status       status
                   :whisper-id   whisper-id
                   :photo        photo
                   :location     location
                   :tags         tags
                   :last-updated last-updated}]
      (decrease-tags-counter old-tags)
      (realm/create :discoveries discovery true)
      (increase-tags-counter tags)))

(defn- discovery-exist? [discoveries discovery]
  (some #(= (:whisper-id discovery) (:whisper-id %)) discoveries))

(defn discovery-list []
  (-> (r/get-all :discoveries)
      (r/sorted :last-updated :desc)))

(defn- add-discoveries [discoveries]
  (realm/write (fn []
                 (let [db-discoveries (.slice (discovery-list) 0)]
                   (dorun (map (fn [discovery]
                                 (if (not (discovery-exist? db-discoveries discovery))
                                   (create-discovery discovery)
                                   (update-discovery discovery)
                                   ))
                               discoveries))))))

(defn save-discoveries [discoveries]
  (add-discoveries discoveries))

(defn discoveries-by-tag [tag limit]
  (log/debug "Discoveries by tag: " tag)
  (-> (r/get-by-filter :discoveries (str "tags.name = '" tag "'"))
      (r/sorted :last-updated :desc)
      (r/page 0 limit)))

(defn get-tag-popular [limit]
  (-> (r/get-all :tag)
      (r/sorted :count :desc)
      (r/page 0 limit)))

