(ns status-im.common.device-network
  (:require
    ["@react-native-community/netinfo" :default net-info]
    [native-module.core :as native-module]
    [oops.core :as oops]
    [status-im.feature-flags :as ff]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/reg-fx
 :effects.device-network/listen-to-network-info
 (fn []
   (oops/ocall net-info
               "addEventListener"
               #(rf/dispatch [:device-network/on-state-change
                              (js->clj % :keywordize-keys true)]))))

(rf/reg-event-fx
 :device-network/on-state-change
 (fn [{:keys [db]} [{:keys [isConnected type details]}]]
   (let [old-network-status       (:network/status db)
         old-network-type         (:network/type db)
         connectivity-status      (if isConnected :online :offline)
         status-changed?          (not= connectivity-status old-network-status)
         type-changed?            (not= type old-network-type)
         is-connection-expensive? (:is-connection-expensive details)]
     (log/debug "[net-info]"
                "old-network-status"  old-network-status
                "old-network-type"    old-network-type
                "connectivity-status" connectivity-status
                "type"                type
                "details"             details)
     {:fx [(when status-changed?
             [:dispatch [:device-network/on-network-status-change isConnected]])
           (when type-changed?
             [:dispatch [:device-network/on-network-type-change type is-connection-expensive?]])]})))

(rf/reg-event-fx
 :device-network/on-network-type-change
 (fn [{:keys [db]} [network-type expensive?]]
   {:db (assoc db :network/type network-type)
    :fx [[:effects.device-network/notify-status-go network-type expensive?]
         [:dispatch [:mobile-network/on-network-status-change]]]}))

(rf/reg-event-fx
 :device-network/on-network-status-change
 (fn [{:keys [db]} [is-connected?]]
   (let [network-status (if is-connected? :online :offline)]
     {:db (assoc db :network/status network-status)
      :fx [(when (ff/enabled? ::ff/wallet.wallet-connect)
             [:dispatch [:wallet-connect/reload-on-network-change is-connected?]])]})))

(rf/reg-fx
 :effects.device-network/notify-status-go
 (fn [[network-type expensive?]]
   (native-module/connection-change network-type expensive?)))
