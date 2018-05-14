(ns status-im.data-store.contact-groups
  (:require [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
 :data-store/get-all-contact-groups
 (fn [cofx _]
   (assoc cofx :all-contact-groups (into {}
                                         (map (juxt :group-id identity))
                                         (-> @core/account-realm
                                             (core/get-all :contact-group)
                                             (core/all-clj :contact-group))))))

(defn save-contact-group-tx
  "Returns tx function for saving contact group"
  [{:keys [group-id] :as group}]
  (fn [realm]
    (core/create realm :contact-group group (core/exists? realm :contact-group :group-id group-id))))

(defn save-contact-groups-tx
  "Returns tx function for saving contact groups"
  [groups]
  (fn [realm]
    (doseq [group groups]
      ((save-contact-group-tx group) realm))))

(defn add-contacts-to-contact-group-tx
  "Returns tx function for adding contacts to contact group"
  [group-id contacts]
  (fn [realm]
    (let [group             (core/single (core/get-by-field realm :contact-group :group-id group-id))
          existing-contacts (object/get group "contacts")]
      (aset group "contacts" (clj->js (into #{} (concat contacts
                                                        (core/list->clj existing-contacts))))))))
