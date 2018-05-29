(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
 :get-manage-mailserver
 :<- [:get :mailservers/manage]
 (fn [manage]
   manage))

(reg-sub
 :manage-mailserver-valid?
 :<- [:get-manage-mailserver]
 (fn [manage]
   (not-any? :error (vals manage))))
