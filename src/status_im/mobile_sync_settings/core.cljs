(ns status-im.mobile-sync-settings.core
  (:require [status-im.bottom-sheet.core :as bottom-sheet]
            [status-im.mailserver.core :as mailserver]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [utils.re-frame :as rf]
            [status-im.utils.mobile-sync :as utils]
            [status-im.wallet.core :as wallet]
            [status-im2.navigation.events :as navigation]
            [taoensso.timbre :as log]))

(rf/defn sheet-defaults
  [{:keys [db]}]
  (let [remember-choice? (get-in db [:multiaccount :remember-syncing-choice?])]
    {:db (assoc db
                :mobile-network/remember-choice?
                (or (nil? remember-choice?)
                    remember-choice?))}))

(rf/defn on-network-status-change
  [{:keys [db] :as cofx}]
  (let [initialized?                       (get db :network-status/initialized?)
        logged-in?                         (multiaccounts.model/logged-in? cofx)
        {:keys [remember-syncing-choice?]} (:multiaccount db)]
    (apply
     rf/merge
     cofx
     {:db (assoc db :network-status/initialized? true)}
     (cond
       (and logged-in?
            (utils/cellular? (:network/type db))
            (not remember-syncing-choice?)
            (not= :create-multiaccount (:view-id db)))

       [(bottom-sheet/show-bottom-sheet
         {:view :mobile-network})
        (sheet-defaults)]

       ;; NOTE(rasom): When we log into account on-network-status-change is
       ;; dispatched, but that doesn't mean there was a status change, thus
       ;; no reason to restart wallet.
       (and logged-in? initialized?)
       [(mailserver/process-next-messages-request)
        (bottom-sheet/hide-bottom-sheet nil)
        (wallet/restart-wallet-service nil)]

       logged-in?
       [(mailserver/process-next-messages-request)
        (bottom-sheet/hide-bottom-sheet nil)]))))

(defn apply-settings
  ([sync?] (apply-settings sync? :default))
  ([sync? remember?]
   (fn [{:keys [db] :as cofx}]
     (let [network          (:network/type db)
           remember-choice?
           (if (not= :default remember?)
             remember?
             (:mobile-network/remember-choice? db))
           cellular?        (utils/cellular? network)]
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
  (let [{:keys [remember-syncing-choice?]} (:multiaccount db)]
    ((apply-settings syncing? remember-syncing-choice?) cofx)))

(rf/defn mobile-network-ask-on-mobile-network?
  {:events [:mobile-network/ask-on-mobile-network?]}
  [{:keys [db] :as cofx} ask?]
  (let [{:keys [syncing-on-mobile-network?]} (:multiaccount db)]
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
   (bottom-sheet/hide-bottom-sheet nil)
   (navigation/navigate-to-cofx :mobile-network-settings nil)))

(rf/defn mobile-network-show-offline-sheet
  {:events [:mobile-network/show-offline-sheet]}
  [cofx]
  (bottom-sheet/show-bottom-sheet
   cofx
   {:view :mobile-network-offline}))
