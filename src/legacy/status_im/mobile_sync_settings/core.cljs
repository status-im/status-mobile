(ns legacy.status-im.mobile-sync-settings.core
  (:require
    [legacy.status-im.bottom-sheet.events :as bottom-sheet]
    [legacy.status-im.mailserver.core :as mailserver]
    [legacy.status-im.multiaccounts.model :as multiaccounts.model]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [legacy.status-im.utils.mobile-sync :as utils]
    [legacy.status-im.wallet.core :as wallet]
    [status-im.contexts.chat.home.add-new-contact.events :as add-new-contact]
    [status-im.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/defn sheet-defaults
  [{:keys [db]}]
  (let [remember-choice? (get-in db [:profile/profile :remember-syncing-choice?])]
    {:db (assoc db
                :mobile-network/remember-choice?
                (or (nil? remember-choice?)
                    remember-choice?))}))

(rf/defn on-network-status-change
  [{:keys [db] :as cofx}]
  (let [initialized?                       (get db :network-status/initialized?)
        logged-in?                         (multiaccounts.model/logged-in? db)
        {:keys [remember-syncing-choice?]} (:profile/profile db)]
    (apply
     rf/merge
     cofx
     {:db (assoc db :network-status/initialized? true)}
     (cond
       ;; NOTE(rasom): When we log into account on-network-status-change is
       ;; dispatched, but that doesn't mean there was a status change, thus
       ;; no reason to restart wallet.
       (and logged-in? initialized?)
       [(mailserver/process-next-messages-request)
        (bottom-sheet/hide-bottom-sheet-old)
        (wallet/restart-wallet-service nil)
        (add-new-contact/set-new-identity-reconnected)]

       logged-in?
       [(mailserver/process-next-messages-request)
        (bottom-sheet/hide-bottom-sheet-old)]))))

(defn apply-settings
  ([sync?] (apply-settings sync? :default))
  ([sync? remember?]
   (fn [{:keys [db] :as cofx}]
     (let [network (:network/type db)
           remember-choice?
           (if (not= :default remember?)
             remember?
             (:mobile-network/remember-choice? db))
           cellular? (utils/cellular? network)]
       (log/info "apply mobile network settings"
                 "sunc?"     sync?
                 "remember?" remember?
                 "cellular?" cellular?)
       (rf/merge
        cofx
        (multiaccounts.update/multiaccount-update
         :syncing-on-mobile-network?
         (boolean sync?)
         {})
        (multiaccounts.update/multiaccount-update
         :remember-syncing-choice?
         (boolean remember-choice?)
         {})
        (when (and cellular? sync?)
          (mailserver/process-next-messages-request))
        (wallet/restart-wallet-service nil))))))

(rf/defn mobile-network-continue-syncing
  {:events [:mobile-network/continue-syncing]}
  [cofx]
  ((apply-settings true) cofx))

(rf/defn mobile-network-stop-syncing
  {:events [:mobile-network/stop-syncing]}
  [cofx]
  ((apply-settings false) cofx))

(rf/defn mobile-network-set-syncing
  {:events [:mobile-network/set-syncing]}
  [{:keys [db] :as cofx} syncing?]
  (let [{:keys [remember-syncing-choice?]} (:profile/profile db)]
    ((apply-settings syncing? remember-syncing-choice?) cofx)))

(rf/defn mobile-network-ask-on-mobile-network?
  {:events [:mobile-network/ask-on-mobile-network?]}
  [{:keys [db] :as cofx} ask?]
  (let [{:keys [syncing-on-mobile-network?]} (:profile/profile db)]
    ((apply-settings syncing-on-mobile-network? (not ask?)) cofx)))

(rf/defn mobile-network-restore-defaults
  {:events [:mobile-network/restore-defaults]}
  [cofx]
  ((apply-settings false false) cofx))

(rf/defn mobile-network-remember-choice?
  {:events [:mobile-network/remember-choice?]}
  [{:keys [db]} remember-choice?]
  {:db (assoc db :mobile-network/remember-choice? remember-choice?)})

(rf/defn mobile-network-navigate-to-settings
  {:events [:mobile-network/navigate-to-settings]}
  [cofx]
  (rf/merge
   cofx
   (bottom-sheet/hide-bottom-sheet-old)
   (navigation/navigate-to :mobile-network-settings nil)))

(rf/defn mobile-network-show-offline-sheet
  {:events [:mobile-network/show-offline-sheet]}
  [cofx]
  (bottom-sheet/hide-bottom-sheet-old cofx))
