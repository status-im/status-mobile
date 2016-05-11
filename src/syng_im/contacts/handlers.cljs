(ns syng-im.contacts.handlers
  (:require [re-frame.core :refer [register-handler after]]
            [syng-im.models.contacts :as contacts]))

(defn save-contact
  [_ [_ contact]]
  (contacts/save-syng-contacts [contact]))

(register-handler :add-contact
  (-> (fn [db [_ contact]]
        (update db :contacts conj contact))
      ((after save-contact))))


