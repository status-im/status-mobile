(ns legacy.status-im.multiaccounts.logout.core
  (:require
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [status-im.common.keychain.events :as keychain]
    [status-im.db :as db]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(re-frame/reg-fx
 ::logout
 (fn []
   (native-module/logout)))

(rf/defn initialize-app-db
  [{{:keys                [keycard initials-avatar-font-file biometrics]
     :network/keys        [type status expensive?]
     :wallet-connect/keys [web3-wallet]}
    :db}]
  {:db (assoc db/app-db
              :network/type               type
              :network/status             status
              :network/expensive?         expensive?
              :initials-avatar-font-file  initials-avatar-font-file
              :keycard                    (dissoc keycard :secrets :pin :application-info)
              :biometrics                 biometrics
              :syncing                    nil
              :wallet-connect/web3-wallet web3-wallet)})

(rf/defn logout-method
  {:events [::logout-method]}
  [{:keys [db] :as cofx} {:keys [auth-method logout?]}]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    (rf/merge cofx
              {:dispatch                               [:init-root :progress]
               :effects.shell/reset-state              nil
               :hide-popover                           nil
               ::logout                                nil
               :profile.settings/webview-debug-changed false
               :keychain/clear-user-password           key-uid
               :profile/get-profiles-overview          #(rf/dispatch
                                                         [:profile/get-profiles-overview-success %])}
              (keychain/save-auth-method key-uid auth-method)
              (initialize-app-db))))

(rf/defn logout
  {:events [:logout :multiaccounts.logout.ui/logout-confirmed
            :multiaccounts.update.callback/save-settings-success]}
  [_]
  ;; we need to disable notifications before starting the logout process
  {:effects/push-notifications-disable nil
   :dispatch                           [:alert-banners/remove-all]
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
