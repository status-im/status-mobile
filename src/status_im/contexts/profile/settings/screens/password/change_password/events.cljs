(ns status-im.contexts.profile.settings.screens.password.change-password.events
  (:require [status-im.contexts.profile.settings.screens.password.change-password.effects]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/reg-event-fx :change-password/verify-old-password
 (fn [_ [entered-password]]
   (let [hashed-password (-> entered-password
                             security/hash-masked-password
                             security/safe-unmask-data)]
     {:json-rpc/call [{:method     "accounts_verifyPassword"
                       :params     [hashed-password]
                       :on-success (fn [valid?]
                                     (rf/dispatch [:change-password/password-verify-success valid?
                                                   entered-password]))
                       :on-error   (fn [error]
                                     (log/error "accounts_verifyPassword error"
                                                {:error error
                                                 :event :password-settings/change-password}))}]})))
(rf/reg-event-fx :change-password/password-verify-success
 (fn [{:keys [db]} [valid? old-password]]
   {:db (if valid?
          (-> db
              (assoc-in [:settings/change-password :old-password] old-password)
              (assoc-in [:settings/change-password :current-step] :new-password))
          (assoc-in db [:settings/change-password :verify-error] true))}))

(rf/reg-event-fx :change-password/reset-error
 (fn [{:keys [db]}]
   {:db (assoc-in db [:settings/change-password :verify-error] false)}))

(rf/reg-event-fx :change-password/reset
 (fn [{:keys [db]}]
   {:db (assoc db :settings/change-password {})}))

(rf/reg-event-fx :change-password/confirm-new-password
 (fn [{:keys [db]} [new-password]]
   {:db (assoc-in db [:settings/change-password :new-password] new-password)
    :fx [[:dispatch [:change-password/submit]]]}))

(rf/reg-event-fx :change-password/submit
 (fn [{:keys [db]}]
   (let [key-uid                             (get-in db [:profile/profile :key-uid])
         {:keys [new-password old-password]} (get db :settings/change-password)]
     {:db (assoc-in db [:settings/change-password :loading?] true)
      :fx [[:dispatch [:dismiss-keyboard]]
           [:dispatch [:open-modal :screen/change-password-loading]]
           [:effects.change-password/change-password
            {:key-uid      key-uid
             :old-password old-password
             :new-password new-password
             :on-success   (fn []
                             (rf/dispatch [:change-password/submit-success]))
             :on-fail      (fn [error]
                             (rf/dispatch [:change-password/submit-fail error]))}]]})))

(rf/reg-event-fx :change-password/submit-fail
 (fn [_ [error]]
   (log/error "failed to change the password"
              {:error error
               :event :change-password/submit})
   {:fx [[:dispatch [:change-password/reset]]
         [:dispatch [:navigate-back]]]}))

(rf/reg-event-fx :change-password/submit-success
 (fn [{:keys [db]}]
   {:db (assoc-in db [:settings/change-password :loading?] false)}))
