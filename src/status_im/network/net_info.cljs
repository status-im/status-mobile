(ns status-im.network.net-info
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.mobile-sync-settings.core :as mobile-network]
            [status-im.utils.fx :as fx]
            [status-im.wallet.core :as wallet]
            ["@react-native-community/netinfo" :default net-info]
            [taoensso.timbre :as log]))

(fx/defn change-network-status
  [{:keys [db] :as cofx} is-connected?]
  (fx/merge cofx
            {:db (assoc db :network-status (if is-connected? :online :offline))}
            (when is-connected?
              (if-not (= (count (get-in db [:wallet :accounts])) (count (get db :multiaccount/accounts)))
                (wallet/update-balances nil nil)))))

(fx/defn change-network-type
  [{:keys [db] :as cofx} old-network-type network-type expensive?]
  (fx/merge cofx
            {:db (assoc db :network/type network-type)
             :network/notify-status-go [network-type expensive?]}
            (mobile-network/on-network-status-change)))

(fx/defn handle-network-info-change
  {:events [::network-info-changed]}
  [{:keys [db] :as cofx} {:keys [isConnected type details] :as state}]
  (let [old-network-status  (:network-status db)
        old-network-type    (:network/type db)
        connectivity-status (if isConnected :online :offline)
        status-changed?     (= connectivity-status old-network-status)
        type-changed?       (= type old-network-type)]
    (log/debug "[net-info]"
               "old-network-status" old-network-status
               "old-network-type" old-network-type
               "connectivity-status" connectivity-status
               "type" type
               "details" details)
    (fx/merge cofx
              (when-not status-changed?
                (change-network-status isConnected))
              (when-not type-changed?
                (change-network-type old-network-type type (:is-connection-expensive details))))))

(defn add-net-info-listener []
  (when net-info
    (.addEventListener ^js net-info
                       #(re-frame/dispatch [::network-info-changed
                                            (js->clj % :keywordize-keys true)]))))

(re-frame/reg-fx
 ::listen-to-network-info
 (fn []
   (add-net-info-listener)))

(re-frame/reg-fx
 :network/notify-status-go
 (fn [[network-type expensive?]]
   (status/connection-change network-type expensive?)))
