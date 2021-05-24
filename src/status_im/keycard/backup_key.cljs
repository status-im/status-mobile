(ns status-im.keycard.backup-key
  (:require [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            [status-im.utils.utils :as utils]
            [status-im.i18n.i18n :as i18n]
            [status-im.ethereum.mnemonic :as mnemonic]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.navigation :as navigation]
            [status-im.signing.core :as signing.core]
            [taoensso.timbre :as log]))

(fx/defn backup-card-pressed
  {:events [:keycard-settings.ui/backup-card-pressed]}
  [{:keys [db] :as cofx} backup-type]
  (log/debug "[keycard] start backup")
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :creating-backup?] backup-type))}
            (navigation/navigate-to-cofx :seed-phrase nil)))

(fx/defn recovery-card-pressed
  {:events [:keycard-settings.ui/recovery-card-pressed]}
  [{:keys [db] :as cofx} show-warning]
  (fx/merge cofx
            {:db (-> db
                     ;setting pin-retry-counter is a workaround for the way the PIN view decides if it should accept PUK or PIN
                     (assoc-in [:keycard :application-info :pin-retry-counter] 3)
                     (assoc-in [:keycard :factory-reset-card?] true)
                     (dissoc :popover/popover))}
            (signing.core/discard)
            (if show-warning
              (utils/show-confirmation {:title               (i18n/label :t/keycard-recover-title)
                                        :content             (i18n/label :t/keycard-recover-text)
                                        :confirm-button-text (i18n/label :t/yes)
                                        :cancel-button-text  (i18n/label :t/no)
                                        :on-accept           #(re-frame/dispatch [:keycard-settings.ui/backup-card-pressed :recovery-card])
                                        :on-cancel           #()})
              (backup-card-pressed :recovery-card))))

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
