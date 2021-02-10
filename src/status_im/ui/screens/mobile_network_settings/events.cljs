(ns status-im.ui.screens.mobile-network-settings.events
  (:require
   [status-im.utils.handlers :as handlers]
   [status-im.multiaccounts.update.core :as multiaccounts.update]
   [status-im.utils.fx :as fx]
   [status-im.wallet.core :as wallet]
   [status-im.ui.components.bottom-sheet.core :as bottom-sheet]
   [status-im.multiaccounts.model :as multiaccounts.model]
   [status-im.navigation :as navigation]
   [status-im.mailserver.core :as mailserver]
   [status-im.ui.screens.mobile-network-settings.utils :as utils]
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

(handlers/register-handler-fx
 :mobile-network/continue-syncing
 (apply-settings true))

(handlers/register-handler-fx
 :mobile-network/stop-syncing
 (apply-settings false))

(handlers/register-handler-fx
 :mobile-network/set-syncing
 (fn [{:keys [db] :as cofx} [_ syncing?]]
   (let [{:keys [remember-syncing-choice?]} (:multiaccount db)]
     ((apply-settings syncing? remember-syncing-choice?) cofx))))

(handlers/register-handler-fx
 :mobile-network/ask-on-mobile-network?
 (fn [{:keys [db] :as cofx} [_ ask?]]
   (let [{:keys [syncing-on-mobile-network?]} (:multiaccount db)]
     ((apply-settings syncing-on-mobile-network? (not ask?)) cofx))))

(handlers/register-handler-fx
 :mobile-network/restore-defaults
 (apply-settings false false))

(handlers/register-handler-fx
 :mobile-network/remember-choice?
 (fn [{:keys [db]} [_ remember-choice?]]
   {:db (assoc db :mobile-network/remember-choice? remember-choice?)}))

(handlers/register-handler-fx
 :mobile-network/navigate-to-settings
 (fn [cofx]
   (fx/merge
    cofx
    (bottom-sheet/hide-bottom-sheet)
    (navigation/navigate-to-cofx :profile-stack {:screen  :mobile-network-settings
                                                 :initial false}))))

;;:mobile-network-offline

(handlers/register-handler-fx
 :mobile-network/show-offline-sheet
 (fn [cofx]
   (bottom-sheet/show-bottom-sheet
    cofx
    {:view :mobile-network-offline})))
