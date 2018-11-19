(ns status-im.ui.screens.group.subs
  (:require [re-frame.core :refer [reg-sub]]
            [status-im.utils.subs :as utils]))

(reg-sub
 :is-contact-selected?
 (utils/contains-sub :group/selected-contacts))

(reg-sub
 :is-participant-selected?
 (utils/contains-sub :selected-participants))

(defn filter-selected-contacts [selected-contacts contacts]
  (remove #(true? (:pending? (contacts %))) selected-contacts))

(reg-sub
 :selected-contacts-count
 :<- [:get :group/selected-contacts]
 :<- [:contacts/contacts]
 (fn [[selected-contacts contacts]]
   (count (filter-selected-contacts selected-contacts contacts))))

(reg-sub
 :selected-participants-count
 :<- [:get :selected-participants]
 (fn [selected-participants]
   (count selected-participants)))

(defn filter-contacts [selected-contacts added-contacts]
  (filter #(selected-contacts (:public-key %)) added-contacts))

(reg-sub
 :selected-group-contacts
 :<- [:get :group/selected-contacts]
 :<- [:all-added-contacts]
 (fn [[selected-contacts added-contacts]]
   (filter-contacts selected-contacts added-contacts)))
