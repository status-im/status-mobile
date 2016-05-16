(ns syng-im.contacts.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub :get-contacts
  (fn [db _]
    (let [contacts (reaction (:contacts @db))]
      (reaction (vals @contacts)))))
