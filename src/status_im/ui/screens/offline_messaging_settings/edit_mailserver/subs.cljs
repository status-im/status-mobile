(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.subs
  (:require
   [re-frame.core :refer [reg-sub]]
   [status-im.mailserver.core :as mailserver]))

(reg-sub
 :get-manage-mailserver
 :<- [:get :mailservers/manage]
 (fn [manage]
   manage))

(reg-sub
 :get-connected-mailserver
 (fn [db]
   (mailserver/connected? (get-in db [:mailservers/manage :id :value])
                          {:db db})))

(reg-sub
 :manage-mailserver-valid?
 :<- [:get-manage-mailserver]
 (fn [manage]
   (not-any? :error (vals manage))))
