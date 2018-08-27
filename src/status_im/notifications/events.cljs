(ns status-im.notifications.events
  (:require [re-frame.core :as re-frame]
            [status-im.notifications.core :as notifications]
            [status-im.ui.screens.accounts.models :as accounts]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.platform :as platform]))

(re-frame/reg-fx
 :notifications/display-notification
 notifications/display-notification)

(re-frame/reg-fx
 :notifications/handle-initial-push-notification
 notifications/handle-initial-push-notification)

(re-frame/reg-fx
 :notifications/get-fcm-token
 (fn [_]
   (when platform/mobile?
     (notifications/get-fcm-token))))

(re-frame/reg-fx
 :notifications/request-notifications
 (fn [_]
   (notifications/request-permissions)))

(handlers/register-handler-fx
 :notifications/handle-push-notification
 (fn [cofx [_ event]]
   (notifications/handle-push-notification event cofx)))

(handlers/register-handler-db
 :notifications/update-fcm-token
 (fn [db [_ fcm-token]]
   (assoc-in db [:notifications :fcm-token] fcm-token)))

(handlers/register-handler-fx
 :notifications/request-notifications-granted
 (fn [cofx _]
   (accounts/show-mainnet-is-default-alert cofx)))

(handlers/register-handler-fx
 :notifications/request-notifications-denied
 (fn [cofx _]
   (accounts/show-mainnet-is-default-alert cofx)))
