(ns legacy.status-im.network.net-info
  (:require
    ["@react-native-community/netinfo" :default net-info]
    [legacy.status-im.mobile-sync-settings.core :as mobile-network]
    [legacy.status-im.wallet.core :as wallet]
    [native-module.core :as native-module]
    [re-frame.core :as re-frame]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/defn change-network-status
  [{:keys [db] :as cofx} is-connected?]
  (rf/merge cofx
            {:db (assoc db :network-status (if is-connected? :online :offline))}
            (when (and is-connected?
                       (or (not= (count (get-in db [:wallet-legacy :accounts]))
                                 (count (get db :profile/wallet-accounts)))
                           (wallet/has-empty-balances? db)))
              (wallet/update-balances nil nil))))

(rf/defn change-network-type
  [{:keys [db] :as cofx} old-network-type network-type expensive?]
  (rf/merge cofx
            {:db                       (assoc db :network/type network-type)
             :network/notify-status-go [network-type expensive?]}
            (mobile-network/on-network-status-change)))

(rf/defn handle-network-info-change
  {:events [::network-info-changed]}
  [{:keys [db] :as cofx} {:keys [isConnected type details] :as state}]
  (let [old-network-status  (:network-status db)
        old-network-type    (:network/type db)
        connectivity-status (if isConnected :online :offline)
        status-changed?     (= connectivity-status old-network-status)
        type-changed?       (= type old-network-type)]
    (log/debug "[net-info]"
               "old-network-status"  old-network-status
               "old-network-type"    old-network-type
               "connectivity-status" connectivity-status
               "type"                type
               "details"             details)
    (rf/merge cofx
              (when-not status-changed?
                (change-network-status isConnected))
              (when-not type-changed?
                (change-network-type old-network-type type (:is-connection-expensive details))))))

(defn add-net-info-listener
  []
  (when net-info
    (.addEventListener ^js net-info
                       #(re-frame/dispatch [::network-info-changed
                                            (js->clj % :keywordize-keys true)]))))

(re-frame/reg-fx
 :network/listen-to-network-info
 (fn []
   (add-net-info-listener)))

(re-frame/reg-fx
 :network/notify-status-go
 (fn [[network-type expensive?]]
   (native-module/connection-change network-type expensive?)))
