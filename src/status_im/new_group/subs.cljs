(ns status-im.new-group.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]
            [status-im.utils.subs :as u]))

(register-sub :is-contact-selected?
  (u/contains-sub :selected-contacts))

(register-sub :selected-contacts-count
  (fn [_ _]
    (let [contacts (subscribe [:get :selected-contacts])]
      (reaction (count @contacts)))))

(defn filter-contacts [selected-contacts added-contacts]
  (filter #(selected-contacts (:whisper-identity %)) added-contacts))

(register-sub :selected-group-contacts
  (fn [_ _]
    (let [selected-contacts (subscribe [:get :selected-contacts])
          added-contacts (subscribe [:all-added-contacts])]
      (reaction (filter-contacts @selected-contacts @added-contacts)))))
