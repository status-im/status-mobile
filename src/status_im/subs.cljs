(ns status-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]
            status-im.chat.subs
            status-im.group-settings.subs
            status-im.discovery.subs
            status-im.contacts.subs
            status-im.new-group.subs))

(register-sub :get
  (fn [db [_ k]]
    (reaction (k @db))))
