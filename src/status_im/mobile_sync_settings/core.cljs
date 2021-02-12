(ns status-im.mobile-sync-settings.core
  (:require
   [status-im.multiaccounts.update.core :as multiaccounts.update]
   [status-im.utils.fx :as fx]
   [status-im.wallet.core :as wallet]
   [status-im.bottom-sheet.core :as bottom-sheet]
   [status-im.multiaccounts.model :as multiaccounts.model]
   [status-im.navigation :as navigation]
   [status-im.mailserver.core :as mailserver]
   [status-im.utils.mobile-sync :as utils]
   [taoensso.timbre :as log]))

(fx/defn sheet-defaults
  [{:keys [db]}]
  (let [remember-choice? (get-in db [:multiaccount :remember-syncing-choice?])]
    {:db (assoc db :mobile-network/remember-choice? (or (nil? remember-choice?)
                                                        remember-choice?))}))

(fx/defn on-network-status-change
  [{:keys [db] :as cofx}]
  (let [logged-in? (multiaccounts.model/logged-in? cofx)
        {:keys [remember-syncing-choice?]} (:multiaccount db)]
    (apply
     fx/merge
     cofx
     (cond
       (and logged-in?
            (utils/cellular? (:network/type db))
            (not remember-syncing-choice?)
            (not= :create-multiaccount (:view-id db)))

       [(bottom-sheet/show-bottom-sheet
         {:view :mobile-network})
        (sheet-defaults)]

       logged-in?
       [(mailserver/process-next-messages-request)
        (bottom-sheet/hide-bottom-sheet)
        (wallet/restart-wallet-service-default)]))))

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
                 "sunc?" sync?
                 "remember?" remember?
                 "cellular?" cellular?)
       (fx/merge
        cofx
        (multiaccounts.update/multiaccount-update
         :syncing-on-mobile-network? (boolean sync?) {})
        (multiaccounts.update/multiaccount-update
         :remember-syncing-choice? (boolean remember-choice?) {})
        (when (and cellular? sync?)
          (mailserver/process-next-messages-request))
        (wallet/restart-wallet-service-default))))))

(fx/defn mobile-network-continue-syncing
  {:events [:mobile-network/continue-syncing]}
  [cofx]
  ((apply-settings true) cofx))

(fx/defn mobile-network-stop-syncing
  {:events [:mobile-network/stop-syncing]}
  [cofx]
  ((apply-settings false) cofx))

(fx/defn mobile-network-set-syncing
  {:events [:mobile-network/set-syncing]}
  [{:keys [db] :as cofx} syncing?]
  (let [{:keys [remember-syncing-choice?]} (:multiaccount db)]
    ((apply-settings syncing? remember-syncing-choice?) cofx)))

(fx/defn mobile-network-ask-on-mobile-network?
  {:events [:mobile-network/ask-on-mobile-network?]}
  [{:keys [db] :as cofx} ask?]
  (let [{:keys [syncing-on-mobile-network?]} (:multiaccount db)]
    ((apply-settings syncing-on-mobile-network? (not ask?)) cofx)))

(fx/defn mobile-network-restore-defaults
  {:events [:mobile-network/restore-defaults]}
  [cofx]
  ((apply-settings false false) cofx))

(fx/defn mobile-network-remember-choice?
  {:events [:mobile-network/remember-choice?]}
  [{:keys [db]} remember-choice?]
  {:db (assoc db :mobile-network/remember-choice? remember-choice?)})

(fx/defn mobile-network-navigate-to-settings
  {:events [:mobile-network/navigate-to-settings]}
  [cofx]
  (fx/merge
   cofx
   (bottom-sheet/hide-bottom-sheet)
   (navigation/navigate-to-cofx :profile-stack {:screen  :mobile-network-settings
                                                :initial false})))

(fx/defn mobile-network-show-offline-sheet
  {:events [:mobile-network/show-offline-sheet]}
  [cofx]
  (bottom-sheet/show-bottom-sheet
   cofx
   {:view :mobile-network-offline}))
