(ns status-im.multiaccounts.logout.core
  (:require [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [native-module.core :as native-module]
            [status-im.notifications.core :as notifications]
            [utils.re-frame :as rf]
            [status-im.utils.keychain.core :as keychain]
            [status-im.wallet.core :as wallet]
            [status-im2.events :as init]))

(rf/defn logout-method
  {:events [::logout-method]}
  [{:keys [db] :as cofx} {:keys [auth-method logout?]}]
  (let [key-uid (get-in db [:profile/profile :key-uid])]
    (rf/merge cofx
              {:set-root                             :progress
               :chat.ui/clear-inputs                 nil
               :shell/reset-state                    nil
               :hide-popover                         nil
               ::logout                              nil
               ::multiaccounts/webview-debug-changed false
               :keychain/clear-user-password         key-uid
               :profile/get-profiles-overview        #(re-frame/dispatch
                                                       [:profile/get-profiles-overview-success
                                                        %])}
              (keychain/save-auth-method key-uid auth-method)
              (wallet/clear-timeouts)
              (init/initialize-app-db))))

(rf/defn logout
  {:events [:logout :multiaccounts.logout.ui/logout-confirmed
            :multiaccounts.update.callback/save-settings-success]}
  [cofx]
  ;; we need to disable notifications before starting the logout process
  (rf/merge cofx
            {:dispatch-later [{:ms       100
                               :dispatch [::logout-method
                                          {:auth-method keychain/auth-method-none
                                           :logout?     true}]}]}
            (notifications/logout-disable)))

(rf/defn show-logout-confirmation
  {:events [:multiaccounts.logout.ui/logout-pressed]}
  [_]
  {:ui/show-confirmation
   {:title               (i18n/label :t/logout-title)
    :content             (i18n/label :t/logout-are-you-sure)
    :confirm-button-text (i18n/label :t/logout)
    :on-accept           #(re-frame/dispatch [:multiaccounts.logout.ui/logout-confirmed])
    :on-cancel           nil}})

(rf/defn biometric-logout
  {:events [:biometric-logout]}
  [cofx]
  (rf/merge cofx
            (logout-method {:auth-method keychain/auth-method-biometric-prepare
                            :logout?     false})
            (fn [{:keys [db]}]
              {:db (assoc-in db [:profile/login :save-password?] true)})))

(re-frame/reg-fx
 ::logout
 (fn []
   (native-module/logout)))
