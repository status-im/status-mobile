(ns status-im.contexts.profile.settings.screens.password.events
  (:require [native-module.core :as native-module]
            [status-im.contexts.profile.settings.screens.password.effects]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx
 :password-settings/verify-old-password
 (fn [_ [entered-password]]
   (let [hashed-password (-> entered-password
                             security/safe-unmask-data
                             native-module/sha3)]
     {:json-rpc/call [{:method     "accounts_verifyPassword"
                       :params     [hashed-password]
                       :on-success (fn [valid?]
                                     (rf/dispatch [:password-settings/password-verify-success valid?
                                                   entered-password]))
                       :on-error   (fn [error]
                                     (log/error "accounts_verifyPassword error"
                                                {:error error
                                                 :event :password-settings/change-password}))}]})))
(rf/reg-event-fx
 :password-settings/password-verify-success
 (fn [{:keys [db]} [valid? old-password]]
   (when valid?
     {:db (if valid?
            (-> db
                (assoc-in [:settings/change-password :old-password] old-password)
                (assoc-in [:settings/change-password :current-step] :new-password))
            (assoc-in db [:settings/change-password :verify-error] true))})))

(rf/reg-event-fx
 :password-settings/change-password-reset-error
 (fn [{:keys [db]}]
   {:db (assoc-in db [:settings/change-password :verify-error] false)}))

(rf/reg-event-fx
 :password-settings/reset-change-password
 (fn [{:keys [db]}]
   {:db (assoc db :settings/change-password {})}))

(rf/reg-event-fx
 :password-settings/change-password-reset-error
 (fn [{:keys [db]}]
   {:db (assoc-in db [:settings/change-password :verify-error] false)}))

(rf/reg-event-fx
 :password-settings/change-password-submit
 (fn [{:keys [db]} [new-password]]
   (let [key-uid      (get-in db [:profile/profile :key-uid])
         old-password (get-in db [:settings/change-password :old-password])]
     {:fx [[:effects.password-settings/change-password
            {:key-uid      key-uid
             :old-password old-password
             :new-password new-password
             :on-success   (fn []
                             (rf/dispatch [:navigate-back]))}]]})))
