(ns status-im.discovery.model
  ;status-im.models.discoveries
  (:require [status-im.utils.logging :as log]
            [status-im.persistence.realm.core :as r]))

(defn get-tag [tag]
  (log/debug "Getting tag: " tag)
  (-> (r/get-by-field :account :tag :name tag)
      (r/single-cljs)))

(defn update-tag-counter [func tag]
  (let [tag        (:name tag)
        tag-object (get-tag tag)]
    (if tag-object
      (r/create :account :tag
                {:name  tag
                 :count (func (:count tag-object))}
                true))))

(defn update-tags-counter [func tags]
  (doseq [tag (distinct tags)]
    (update-tag-counter func tag)))

(defn get-tags [msg-id]
  (-> (r/get-by-field :account :discovery :msg-id msg-id)
      (r/single-cljs)
      (:tags)
      (vals)))

(defn- upsert-discovery [{:keys [msg-id tags] :as discovery}]
  (log/debug "Creating/updating discovery with tags: " tags)
  (let [prev-tags (get-tags msg-id)]
    (if prev-tags
      (update-tags-counter dec prev-tags))
    (r/create :account :discovery discovery true)
    (update-tags-counter inc tags)))

(defn discovery-list []
  (->> (-> (r/get-all :account :discovery)
           (r/sorted :priority :desc)
           (r/collection->map))
       (mapv #(update % :tags vals))))

(defn- add-discoveries [discoveries]
  (r/write :account
           (fn []
             (doseq [discovery discoveries]
               (upsert-discovery discovery)))))

(defn save-discoveries [discoveries]
  (add-discoveries discoveries))

(defn remove-discoveries! [by ordering critical-count to-delete-count]
  (let [objs  (r/get-all :account :discovery)
        count (r/get-count objs)]
    (if (> count critical-count)
      (let [to-delete (-> (r/sorted objs by ordering)
                          (r/page 0 to-delete-count))]
        (r/write :account
                 (fn []
                   (log/debug (str "Deleting " (r/get-count to-delete) " discoveries"))
                   (r/delete :account to-delete)))))))

(defn discoveries-by-tag [tag limit]
  (let [discoveries (-> (r/get-by-filter :account :discovery (str "tags.name = '" tag "'"))
                        (r/sorted :priority :desc))]
    (log/debug "Discoveries by tag: " tag)
    (if (pos? limit)
      (r/page discoveries 0 limit)
      discoveries)))

(defn all-tags []
  (-> (r/get-all :account :tag)
      (r/sorted :count :desc)
      r/collection->map))

