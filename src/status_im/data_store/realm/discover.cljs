(ns status-im.data-store.realm.discover
  (:require [status-im.data-store.realm.core :as realm]
            [taoensso.timbre :as log])
  (:refer-clojure :exclude [exists?]))

(defn get-all
  [ordering]
  (-> (realm/get-all @realm/account-realm :discover)
      (realm/sorted :created-at ordering)))

(defn get-all-as-list
  [ordering]
  (-> (get-all ordering)
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
  (-> (realm/get-one-by-field-clj @realm/account-realm :discover :message-id message-id)
      (:tags)
      (vals)))

(defn- upsert-discover [{:keys [message-id tags] :as discover}]
  (log/debug "Creating/updating discover with tags: " tags)
  (let [prev-tags (get-tags message-id)]
    (when prev-tags
      (update-tags-counter dec prev-tags))
    (realm/create @realm/account-realm :discover discover true)
    (update-tags-counter inc tags)))

(defn exists?
  [message-id]
  (realm/exists? @realm/account-realm :discover {:message-id message-id}))

(defn save
  [discover]
  (realm/write @realm/account-realm
               #(upsert-discover discover)))

(defn save-all
  [discoveries]
  (realm/write @realm/account-realm
               (fn []
                 (doseq [discover discoveries]
                   (upsert-discover discover)))))

(defn delete
  [by ordering critical-count to-delete-count]
  (let [discoveries  (realm/get-all @realm/account-realm :discover)
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
