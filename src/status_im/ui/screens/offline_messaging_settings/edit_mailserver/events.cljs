(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.events
  (:require [re-frame.core :as re-frame]
            [status-im.models.mailserver :as models.mailserver]
            [status-im.utils.handlers :refer [register-handler] :as handlers]))

(handlers/register-handler-fx
 :upsert-mailserver
 [(re-frame/inject-cofx :random-id)]
 (fn [cofx _]
   (models.mailserver/upsert cofx)))

(handlers/register-handler-fx
 :mailserver-set-input
 (fn [cofx [_ input-key value]]
   (models.mailserver/set-input input-key value cofx)))

(handlers/register-handler-fx
 :edit-mailserver
 (fn [cofx [_ mailserver-id]]
   (models.mailserver/edit mailserver-id cofx)))

(handlers/register-handler-fx
 :delete-mailserver
 (fn [cofx [_ mailserver-id]]
   (assoc (models.mailserver/delete mailserver-id cofx)
          :dispatch [:navigate-back])))

(handlers/register-handler-fx
 :set-mailserver-from-qr
 (fn [cofx [_ _ contact-identity]]
   (assoc (models.mailserver/set-input :url contact-identity cofx)
          :dispatch [:navigate-back])))
