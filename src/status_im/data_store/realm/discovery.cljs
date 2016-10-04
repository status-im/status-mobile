(ns status-im.data-store.realm.discovery
  (:require [status-im.data-store.realm.core :as realm]
            [taoensso.timbre :as log]))

(defn get-all
  []
  (-> (realm/get-all @realm/account-realm :discovery)
      (realm/sorted :priority :desc)))

(defn get-all-as-list
  []
  (-> (get-all)
      realm/realm-collection->list))

(defn get-tag-by-name [tag]
  (log/debug "Getting tag: " tag)
  (realm/get-one-by-field-clj @realm/account-realm :tag :name tag))

(defn- update-tag-counter [func tag]
  (let [tag        (:name tag)
        tag-object (get-tag-by-name tag)]
    (if tag-object
      (realm/create @realm/account-realm :tag
                    {:name  tag
                     :count (func (:count tag-object))}
                    true))))

(defn- update-tags-counter [func tags]
  (doseq [tag (distinct tags)]
    (update-tag-counter func tag)))

(defn- get-tags
  [message-id]
  (-> (realm/get-one-by-field-clj @realm/account-realm :discovery :message-id message-id)
      (:tags)
      (vals)))

(defn- upsert-discovery [{:keys [message-id tags] :as discovery}]
  (log/debug "Creating/updating discovery with tags: " tags)
  (let [prev-tags (get-tags message-id)]
    (if prev-tags
      (update-tags-counter dec prev-tags))
    (realm/create @realm/account-realm :discovery discovery true)
    (update-tags-counter inc tags)))

(defn save
  [discovery]
  (realm/write @realm/account-realm
               #(upsert-discovery discovery)))

(defn save-all
  [discoveries]
  (realm/write @realm/account-realm
               (fn []
                 (doseq [discovery discoveries]
                   (upsert-discovery discovery)))))

(defn delete
  [by ordering critical-count to-delete-count]
  (let [discoveries  (realm/get-all @realm/account-realm :discovery)
        count (realm/get-count discoveries)]
    (if (> count critical-count)
      (let [to-delete (-> (realm/sorted discoveries by ordering)
                          (realm/page 0 to-delete-count))]
        (realm/write @realm/account-realm
                     (fn []
                       (log/debug (str "Deleting " (realm/get-count to-delete) " discoveries"))
                       (realm/delete @realm/account-realm to-delete)))))))

(defn get-all-tags []
  (-> (realm/get-all @realm/account-realm :tag)
      (realm/sorted :count :desc)
      realm/realm-collection->list))
