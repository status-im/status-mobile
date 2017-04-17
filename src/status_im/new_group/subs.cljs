(ns status-im.new-group.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]
            [status-im.utils.subs :as u]))

(register-sub :is-contact-selected?
  (u/contains-sub :selected-contacts))

(defn filter-selected-contacts [selected-contacts contacts]
  (remove #(true? (:pending? (contacts %))) selected-contacts))

(register-sub :selected-contacts-count
  (fn [_ _]
    (let [selected-contacts (subscribe [:get :selected-contacts])
          contacts (subscribe [:get :contacts])]
      ;TODO temporary, contact should be deleted from group after contact deletion from contacts
      (reaction (count (filter-selected-contacts @selected-contacts @contacts))))))

(register-sub :selected-participants-count
  (fn [_ _]
    (let [selected-participants (subscribe [:get :selected-participants])]
      (reaction (count @selected-participants)))))

(defn filter-contacts [selected-contacts added-contacts]
  (filter #(selected-contacts (:whisper-identity %)) added-contacts))

(register-sub :selected-group-contacts
  (fn [_ _]
    (let [selected-contacts (subscribe [:get :selected-contacts])
          added-contacts (subscribe [:all-added-contacts])]
      (reaction (filter-contacts @selected-contacts @added-contacts)))))

(register-sub :get-contact-group
  (fn [db _]
    (let [contact-groups (reaction (:contact-groups @db))
          contact-group-id (reaction (:contact-group-id @db))]
      (reaction (@contact-groups @contact-group-id)))))
