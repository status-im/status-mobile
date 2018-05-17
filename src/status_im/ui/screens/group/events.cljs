(ns status-im.ui.screens.group.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.group.navigation]))

(handlers/register-handler-db
 :deselect-contact
 (fn [db [_ id]]
   (update db :group/selected-contacts disj id)))

(handlers/register-handler-db
 :select-contact
 (fn [db [_ id]]
   (update db :group/selected-contacts conj id)))

(handlers/register-handler-db
 :deselect-participant
 (fn [db [_ id]]
   (update db :selected-participants disj id)))

(handlers/register-handler-db
 :select-participant
 (fn [db [_ id]]
   (update db :selected-participants conj id)))
