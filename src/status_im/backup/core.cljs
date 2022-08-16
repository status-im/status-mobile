(ns status-im.backup.core
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]))

(fx/defn handle-backup-failed
  {:events [::backup-failed]}
  [{:keys [db]}]
  {:db (dissoc db :backup/performing-backup)})

(fx/defn handle-backup-perfomed
  {:events [::backup-performed]}
  [{:keys [db]}]
  {:db (dissoc db :backup/performing-backup)})

(fx/defn handle-perform-backup-pressed
  {:events [:multiaccounts.ui/perform-backup-pressed]}
  [{:keys [db]}]
  {:db (assoc db :backup/performing-backup true)
   ::json-rpc/call [{:method     "wakuext_backupData"
                     :params     []
                     :on-error   #(do
                                    (log/error "failed to perfom backup" %)
                                    (re-frame/dispatch [::backup-failed %]))
                     :on-success #(re-frame/dispatch [::backup-performed %])}]})
