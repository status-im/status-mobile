(ns status-im.contexts.network.events
  (:require
    [status-im.common.data-confirmation-sheet.view :as data-confirmation-sheet]
    [status-im.config :as config]
    [status-im.feature-flags :as ff]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(rf/reg-event-fx :network/set-syncing-on-mobile-network
 (fn [{:keys [db]} [syncing-on-mobile-network?]]
   (when config/mobile-data-syncing-toggle-enabled?
     {:db (update db
                  :profile/profile
                  assoc
                  :syncing-on-mobile-network?
                  syncing-on-mobile-network?
                  :remember-syncing-choice?
                  true)
      :fx [[:json-rpc/call
            [{:method     "wakuext_setSyncingOnMobileNetwork"
              :params     [{:enabled syncing-on-mobile-network?}]
              :on-success #(log/debug "successfully set syncing-on-mobile-network"
                                      syncing-on-mobile-network?)
              :on-error   #(log/error "could not set syncing-on-mobile-network" %)}]]]})))

(rf/reg-event-fx :network/check-expensive-connection
 (fn [{:keys [db]}]
   (when
     (and (:network/expensive? db)
          config/mobile-data-syncing-toggle-enabled?
          (:profile/profile db)
          (not (get-in db [:profile/profile :remember-syncing-choice?])))
     ;; Note here we are only updating :remember-syncing-choice? temporarily to avoid opening
     ;; multiple bottom sheets and the user might again see the sheet on the next login. Unless the
     ;; user makes a syncing choice that will persist this key in the status-go
     {:db (assoc-in db [:profile/profile :remember-syncing-choice?] true)
      :fx [[:dispatch
            [:show-bottom-sheet
             {:content (fn [] [data-confirmation-sheet/view])
              :shell?  true
              :theme   :dark}]]]})))

(rf/reg-event-fx
 :network/on-state-change
 (fn [{:keys [db]} [{:keys [isConnected type details]}]]
   (let [old-network-status  (:network/status db)
         old-network-type    (:network/type db)
         connectivity-status (if isConnected :online :offline)
         status-changed?     (not= connectivity-status old-network-status)
         type-changed?       (not= type old-network-type)
         expensive?          (:isConnectionExpensive details)]
     (log/debug "[net-info]"
                "old-network-status"  old-network-status
                "old-network-type"    old-network-type
                "connectivity-status" connectivity-status
                "type"                type
                "details"             details)
     {:db (assoc db :network/expensive? expensive?)
      :fx [[:dispatch [:network/check-expensive-connection]]
           (when status-changed?
             [:dispatch [:network/on-network-status-change connectivity-status]])
           (when type-changed?
             [:dispatch [:network/on-network-type-change type expensive?]])]})))

(rf/reg-event-fx
 :network/on-network-type-change
 (fn [{:keys [db]} [network-type expensive?]]
   {:db (assoc db :network/type network-type)
    :fx [[:effects.network/notify-status-go network-type expensive?]
         [:dispatch [:network/on-network-status-change]]]}))

(rf/reg-event-fx
 :network/on-network-status-change
 (fn [{:keys [db]} [connectivity-status]]
   {:db (assoc db :network/status connectivity-status)
    :fx [(when (ff/enabled? ::ff/wallet.wallet-connect)
           [:dispatch [:wallet-connect/reload-on-network-change (= :online connectivity-status)]])]}))
