(ns status-im.multiaccounts.logout.core
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im.wallet.core :as wallet]
    [status-im2.common.keychain.events :as keychain]
    [status-im2.db :as db]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 ::logout
 (fn []
   (native-module/logout)))

(rf/defn initialize-app-db
  [{{:keys           [keycard initials-avatar-font-file]
     :biometric/keys [supported-type]
     :network/keys   [type]}
    :db}]
  {:db (assoc db/app-db
              :network/type              type
              :initials-avatar-font-file initials-avatar-font-file
              :keycard                   (dissoc keycard :secrets :pin :application-info)
              :biometric/supported-type  supported-type
              :syncing                   nil)})

(rf/defn logout-method
  {:events [::logout-method]}
  [{:keys [db] :as cofx} {:keys [auth-method logout?]}]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    (rf/merge cofx
              {:set-root                               :progress
               :chat.ui/clear-inputs                   nil
               :effects.shell/reset-state              nil
               :hide-popover                           nil
               ::logout                                nil
               :profile.settings/webview-debug-changed false
               :keychain/clear-user-password           key-uid
               :profile/get-profiles-overview          #(rf/dispatch
                                                         [:profile/get-profiles-overview-success %])}
              (keychain/save-auth-method key-uid auth-method)
              (wallet/clear-timeouts)
              (initialize-app-db))))

(rf/defn logout
  {:events [:logout :multiaccounts.logout.ui/logout-confirmed
            :multiaccounts.update.callback/save-settings-success]}
  [_]
  ;; we need to disable notifications before starting the logout process
  {:effects/push-notifications-disable nil
   :dispatch-later                     [{:ms       100
                                         :dispatch [::logout-method
                                                    {:auth-method keychain/auth-method-none
                                                     :logout?     true}]}]})

(rf/defn show-logout-confirmation
  {:events [:multiaccounts.logout.ui/logout-pressed]}
  [_]
  {:ui/show-confirmation
   {:title               (i18n/label :t/logout-title)
    :content             (i18n/label :t/logout-are-you-sure)
    :confirm-button-text (i18n/label :t/logout)
    :on-accept           #(re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])
    :on-cancel           nil}})
