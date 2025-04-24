(ns status-im.contexts.profile.logout.events
  (:require [status-im.config :as config]
            [status-im.contexts.profile.logout.effects]
            [status-im.db :as db]
            [utils.re-frame :as rf]))

(rf/reg-event-fx
 :profile.logout/disable-notifications
 (fn [{:keys [db]}]
   {:fx [[:effects/push-notifications-disable
          (let [{:keys [notifications-enabled?
                        news-notifications-enabled?
                        messenger-notifications-enabled?]} (:profile/profile db)]
            (when notifications-enabled?
              (cond-> #{:disable-chat-notifications?}
                news-notifications-enabled?      (conj :disable-news-notifications?)
                messenger-notifications-enabled? (conj :disable-chat-notifications?))))]
         [:dispatch [:alert-banners/remove-all]]]}))

(defn- restart-app-db
  [{:keys        [initials-avatar-font-file keycard biometrics
                  network/status network/expensive?
                  centralized-metrics/user-confirmed?]
    network-type :network/network-type
    :as          db}]
  (assoc db/app-db
         ;; We must carry over the current `:selected-stack-id`, otherwise the
         ;; app will start with `:view-id` as nil.
         :shell/selected-stack-id             (:shell/selected-stack-id db)
         :profile/logging-out?                true
         :centralized-metrics/user-confirmed? user-confirmed?
         :network/type                        network-type
         :network/status                      status
         :network/expensive?                  expensive?
         :initials-avatar-font-file           initials-avatar-font-file
         :keycard                             (dissoc keycard :secrets :pin :application-info)
         :biometrics                          biometrics
         :syncing                             nil
         :log-level/pre-login-log-level       (config/log-level)))

(rf/reg-event-fx
 :profile.logout/reset-state
 (fn [{db :db}]
   {:db (restart-app-db db)
    :fx [[:hide-popover nil]
         [:effects.profile/logout nil]
         [:profile.settings/webview-debug-changed false]
         [:profile/get-profiles-overview
          #(rf/dispatch [:profile/get-profiles-overview-success % {:logout-phase? true}])]]}))

(rf/reg-event-fx
 :profile/logout
 (fn [{db :db}]
   {:db (assoc db :profile/logging-out? true)
    ;; We need to disable notifications before starting the logout process
    :fx [[:dispatch [:profile.logout/disable-notifications]]
         [:dispatch [:wallet-connect/unregister-event-listeners]]
         [:dispatch-later
          {:ms       100
           :dispatch [:profile.logout/reset-state]}]]}))
