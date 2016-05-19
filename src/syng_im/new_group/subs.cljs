(ns syng-im.new-group.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub :is-contact-selected?
  (fn [db [_ id]]
    (-> (:selected-contacts @db)
        (contains? id)
        (reaction))))
