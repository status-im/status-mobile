(ns syng-im.group-settings.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            [syng-im.models.contacts :refer [contact-by-identity]]))

(register-sub :group-settings-selected-member
  (fn [db [_]]
    (reaction
     (let [identity (get @db :group-settings-selected-member)]
       (contact-by-identity identity)))))

(register-sub :group-settings-show-color-picker
  (fn [db [_]]
    (reaction (get @db :group-settings-show-color-picker))))
