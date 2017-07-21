(ns status-im.new-group.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.subs :as u]))

(reg-sub :is-contact-selected?
  (u/contains-sub :selected-contacts))

(reg-sub :is-participant-selected?
  (u/contains-sub :selected-participants))

(defn filter-selected-contacts [selected-contacts contacts]
  (remove #(true? (:pending? (contacts %))) selected-contacts))

(reg-sub :selected-contacts-count
  :<- [:get :selected-contacts]
  :<- [:get-contacts]
  (fn [[selected-contacts contacts]]
    ;TODO temporary, contact should be deleted from group after contact deletion from contacts
    (count (filter-selected-contacts selected-contacts contacts))))

(reg-sub :selected-participants-count
  :<- [:get :selected-participants]
  (fn [selected-participants]
    (count selected-participants)))

(defn filter-contacts [selected-contacts added-contacts]
  (filter #(selected-contacts (:whisper-identity %)) added-contacts))

(reg-sub :selected-group-contacts
  :<- [:get :selected-contacts]
  :<- [:all-added-contacts]
  (fn [[selected-contacts added-contacts]]
    (filter-contacts selected-contacts added-contacts)))

(reg-sub :get-contact-group
  (fn [db]
    ((:contact-groups db) (:contact-group-id db))))
