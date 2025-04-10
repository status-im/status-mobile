(ns status-im.contexts.settings.advanced.events
  (:require [re-frame.core :as rf]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]))

(rf/reg-event-fx
 :advanced-settings/toggle-waku-backup
 (fn [{:keys [db]}]
   (let [backup-enabled? (-> db :profile/profile :backup-enabled?)]
     {:fx [[:dispatch [:multiaccounts.ui/update :backup-enabled? (not backup-enabled?) {}]]]})))

(rf/reg-event-fx
 :advanced-settings/backup-performed
 (fn [{:keys [db]} [last-backup]]
   {:db (-> db
            (dissoc :backup/performing-backup)
            (assoc-in [:profile/profile :last-backup] last-backup))}))

(rf/reg-event-fx
 :advanced-settings/backup-failed
 (fn [{:keys [db]}]
   {:db (dissoc db :backup/performing-backup)}))

(rf/reg-event-fx
 :advanced-settings/perform-backup
 (fn [{:keys [db]}]
   {:db            (assoc db :backup/performing-backup true)
    :json-rpc/call [{:method   "wakuext_backupData"
                     :params   []
                     :on-error (fn [error]
                                 (log/error "Failed to perform backup" error)
                                 (rf/dispatch
                                  [:toasts/upsert
                                   {:type  :negative
                                    :theme :dark
                                    :text  "Failed to perform backup, please try again later"}])
                                 (rf/dispatch [:advanced-settings/backup-failed error]))}]}))

(rf/reg-event-fx
 :advanced-settings/change-log-level
 (fn [{:keys [db]} [log-level]]
   (let [current-log-level (get-in db [:profile/profile :log-level])]
     (when (not= current-log-level log-level)
       (let [need-set-profile-log-enabled? (or (empty? current-log-level) (empty? log-level))
             profile-log-enabled?          (boolean (seq log-level))]
         {:fx [[:log-level/set-profile-log-level log-level]
               (when need-set-profile-log-enabled?
                 [:log-level/set-profile-log-enabled profile-log-enabled?])
               ;; update log level in taoensso.timbre
               [:logs/set-level log-level]
               [:dispatch [:multiaccounts.ui/update :log-level log-level]]]})))))

(rf/reg-event-fx
 :advanced-settings/change-fleet
 (fn [{db :db} [new-fleet]]
   (let [current-fleet (-> db :profile/profile :fleet keyword)]
     (when (not= current-fleet new-fleet)
       {:ui/show-confirmation
        {:title               (i18n/label :t/close-app-title)
         :content             (i18n/label :t/change-fleet {:fleet new-fleet})
         :confirm-button-text (i18n/label :t/close-app-button)
         :on-accept           #(rf/dispatch [:fleet.ui/save-fleet-confirmed
                                             (some-> new-fleet
                                                     name)])
         :on-cancel           nil}}))))

(rf/reg-event-fx
 :advanced-settings/toggle-light-client
 (fn [{:keys [db]}]
   (let [previously-enabled? (-> db :profile/profile :wakuv2-config :LightClient)
         new-setting         (not previously-enabled?)]
     {:ui/show-confirmation
      {:title               (i18n/label :t/close-app-title)
       :content             (if previously-enabled?
                              (i18n/label :t/disable-light-client)
                              (i18n/label :t/enable-light-client))
       :on-accept           #(rf/dispatch [:advanced-settings/change-light-client new-setting])
       :confirm-button-text (i18n/label :t/close-app-button)
       :on-cancel           nil}})))

(rf/reg-event-fx
 :advanced-settings/change-light-client
 (fn [{:keys [db]} [new-setting]]
   {:db            (assoc-in db [:profile/profile :wakuv2-config :LightClient] new-setting)
    :json-rpc/call [{:method     "wakuext_setLightClient"
                     :params     [{:enabled new-setting}]
                     :on-success (fn []
                                   (log/info "Light client set successfully" new-setting)
                                   (rf/dispatch [:profile/logout]))
                     :on-error   #(log/error "Failed to set light client"
                                             {:error    %
                                              :enabled? new-setting})}]}))

(rf/reg-event-fx
 :advanced-settings/toggle-store-confirmations
 (fn [{:keys [db]}]
   (let [previously-enabled? (-> db
                                 :profile/profile
                                 :wakuv2-config
                                 :EnableStoreConfirmationForMessagesSent)
         new-setting         (not previously-enabled?)]
     {:ui/show-confirmation
      {:title               (i18n/label :t/close-app-title)
       :content             (if previously-enabled?
                              (i18n/label :t/disable-store-confirmations)
                              (i18n/label :t/enable-store-confirmations))
       :on-accept           #(rf/dispatch [:advanced-settings/change-store-confirmations
                                           new-setting])
       :confirm-button-text (i18n/label :t/close-app-button)
       :on-cancel           nil}})))

(rf/reg-event-fx
 :advanced-settings/change-store-confirmations
 (fn [{:keys [db]} [new-setting]]
   {:db            (assoc-in db
                    [:profile/profile :wakuv2-config :EnableStoreConfirmationForMessagesSent]
                    new-setting)
    :json-rpc/call [{:method     "wakuext_setStoreConfirmationForMessagesSent"
                     :params     [{:enabled new-setting}]
                     :on-success (fn []
                                   (log/info "Store confirmation set successfully" new-setting)
                                   (rf/dispatch [:profile/logout]))
                     :on-error   #(log/error "Failed to set store confirmation"
                                             {:error    %
                                              :enabled? new-setting})}]}))
