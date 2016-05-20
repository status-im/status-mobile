(ns syng-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            syng-im.chat.subs
            syng-im.group-settings.subs
            syng-im.discovery.subs
            syng-im.contacts.subs
            syng-im.new-group.subs
            syng-im.participants.subs))

(register-sub :get
  (fn [db [_ k]]
    (reaction (k @db))))

(register-sub :get-in
  (fn [db [_ path]]
    (reaction (get-in @db path))))
