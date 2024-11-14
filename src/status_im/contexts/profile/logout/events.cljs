(ns status-im.contexts.profile.logout.events
  (:require [native-module.core :as native-module]
            [status-im.db :as db]
            [utils.re-frame :as rf]))

(rf/reg-fx :profile.logout/native-module native-module/logout)

(rf/reg-event-fx
 :profile.logout/disable-notifications
 (fn [_]
   {:fx [[:effects/push-notifications-disable nil]
         [:dispatch [:alert-banners/remove-all]]]}))

(defn- restart-app-db
  [{:keys        [initials-avatar-font-file keycard biometrics
                  network/status network/expensive?
                  centralized-metrics/user-confirmed?]
    network-type :network/network-type
    :as          _db}]
  (assoc db/app-db
         :centralized-metrics/user-confirmed? user-confirmed?
         :network/type                        network-type
         :network/status                      status
         :network/expensive?                  expensive?
         :initials-avatar-font-file           initials-avatar-font-file
         :keycard                             (dissoc keycard :secrets :pin :application-info)
         :biometrics                          biometrics
         :syncing                             nil))

(rf/reg-event-fx
 :profile.logout/reset-state
 (fn [{db :db}]
   {:db (restart-app-db db)
    :fx [[:dispatch [:update-theme-and-init-root :progress]]
         [:effects.shell/reset-state nil]
         [:hide-popover nil]
         [:profile.logout/native-module nil]
         [:profile.settings/webview-debug-changed false]
         [:profile/get-profiles-overview #(rf/dispatch [:profile/get-profiles-overview-success %])]]}))

(rf/reg-event-fx
 :profile.logout/logout
 (fn [_]
   ;; We need to disable notifications before starting the logout process
   {:fx [[:dispatch [:profile.logout/disable-notifications]]
         [:dispatch-later
          {:ms       100
           :dispatch [:profile.logout/reset-state]}]]}))

