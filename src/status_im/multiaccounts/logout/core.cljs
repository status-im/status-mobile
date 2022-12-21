(ns status-im.multiaccounts.logout.core
  (:require [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.native-module.core :as status]
            [status-im.notifications.core :as notifications]
            [utils.re-frame :as rf]
            [status-im.utils.keychain.core :as keychain]
            [status-im.wallet.core :as wallet]
            [status-im2.setup.events :as init]))

(rf/defn logout-method
  {:events [::logout-method]}
  [{:keys [db] :as cofx} {:keys [auth-method logout?]}]
  (let [key-uid (get-in db [:multiaccount :key-uid])]
    (rf/merge cofx
              {:init-root-fx                         :progress
               :chat.ui/clear-inputs                 nil
               :chat.ui/clear-inputs-old             nil
               :shell/reset-bottom-tabs              nil
               :hide-popover                         nil
               ::logout                              nil
               ::multiaccounts/webview-debug-changed false
               :keychain/clear-user-password         key-uid
               :setup/open-multiaccounts             #(re-frame/dispatch [:setup/initialize-multiaccounts
                                                                          % {:logout? logout?}])}
              (keychain/save-auth-method key-uid auth-method)
              (wallet/clear-timeouts)
              (init/initialize-app-db))))

(rf/defn logout
  {:events [:logout :multiaccounts.logout.ui/logout-confirmed
            :multiaccounts.update.callback/save-settings-success]}
  [cofx]
  ;; we need to disable notifications before starting the logout process
  (rf/merge cofx
            {:dispatch       [:wallet-connect-legacy/clean-up-sessions]
             :dispatch-later [{:ms       100
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
              {:db (assoc-in db [:multiaccounts/login :save-password?] true)})))

(re-frame/reg-fx
 ::logout
 (fn []
   (status/logout)))
