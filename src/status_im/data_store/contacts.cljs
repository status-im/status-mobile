(ns status-im.data-store.contacts
  (:require [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(defn- normalize-contact [contact]
  (-> contact
      (update :tags #(into #{} %))))

(re-frame/reg-cofx
 :data-store/get-all-contacts
 (fn [coeffects _]
   (assoc coeffects :all-contacts (map normalize-contact
                                       (-> @core/account-realm
                                           (core/get-all :contact)
                                           (core/all-clj :contact))))))

(defn save-contact-tx
  "Returns tx function for saving contact"
  [{:keys [whisper-identity] :as contact}]
  (fn [realm]
    (core/create realm
                 :contact
                 (dissoc contact :command :response :subscriptions)
                 true)))

(defn save-contacts-tx
  "Returns tx function for saving contacts"
  [contacts]
  (fn [realm]
    (doseq [contact contacts]
      ((save-contact-tx contact) realm))))

(defn- get-contact-by-id [whisper-identity realm]
  (core/single (core/get-by-field realm :contact :whisper-identity whisper-identity)))

(defn delete-contact-tx
  "Returns tx function for deleting contact"
  [whisper-identity]
  (fn [realm]
    (core/delete realm (get-contact-by-id whisper-identity realm))))

(defn add-contact-tag-tx
  "Returns tx function for adding chat contacts"
  [whisper-identity tag]
  (fn [realm]
    (let [contact       (get-contact-by-id whisper-identity realm)
          existing-tags (object/get contact "tags")]
      (aset contact "tags"
            (clj->js (into #{} (concat tag
                                       (core/list->clj existing-tags))))))))

(defn remove-contact-tag-tx
  "Returns tx function for removing chat contacts"
  [whisper-identity tag]
  (fn [realm]
    (let [contact       (get-contact-by-id whisper-identity realm)
          existing-tags (object/get contact "tags")]
      (aset contact "tags"
            (clj->js (remove (into #{} tag)
                             (core/list->clj existing-tags)))))))
