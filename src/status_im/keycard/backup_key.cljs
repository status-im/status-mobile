(ns status-im.keycard.backup-key
  (:require [status-im.utils.fx :as fx]
            [status-im.ethereum.mnemonic :as mnemonic]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.navigation :as navigation]
            [taoensso.timbre :as log]))

(fx/defn backup-card-pressed
  {:events [:keycard-settings.ui/backup-card-pressed]}
  [{:keys [db] :as cofx}]
  (log/debug "[keycard] start backup")
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :creating-backup?] true))}
            (navigation/navigate-to-cofx :seed-phrase nil)))

(fx/defn start-keycard-backup
  {:events [::start-keycard-backup]}
  [{:keys [db] :as cofx}]
  {::multiaccounts.recover/import-multiaccount {:passphrase (-> db
                                                                :multiaccounts/key-storage
                                                                :seed-phrase
                                                                mnemonic/sanitize-passphrase)
                                                :password nil
                                                :success-event ::create-backup-card}})
(fx/defn create-backup-card
  {:events [::create-backup-card]}
  [{:keys [db] :as cofx} root-data derived-data]
  (fx/merge cofx
            {:db  (-> db
                      (update :intro-wizard
                              assoc
                              :root-key root-data
                              :derived derived-data
                              :recovering? true
                              :selected-storage-type :advanced)
                      (assoc-in [:keycard :flow] :recovery)
                      (update :multiaccounts/key-storage dissoc :seed-phrase))
             :dismiss-keyboard nil}
            (navigation/navigate-to-cofx :keycard-onboarding-intro nil)))