(ns status-im.ui.screens.group.events
  (:require [status-im.utils.handlers :as handlers]
            [status-im.ui.screens.group.navigation]))

(handlers/register-handler-fx
 :deselect-contact
 (fn [{:keys [db]} [_ id]]
   {:db (update db :group/selected-contacts disj id)}))

(handlers/register-handler-fx
 :select-contact
 (fn [{:keys [db]} [_ id]]
   {:db (update db :group/selected-contacts conj id)}))

(handlers/register-handler-fx
 :deselect-participant
 (fn [{:keys [db]} [_ id]]
   {:db (update db :selected-participants disj id)}))

(handlers/register-handler-fx
 :select-participant
 (fn [{:keys [db]} [_ id]]
   {:db (update db :selected-participants conj id)}))
