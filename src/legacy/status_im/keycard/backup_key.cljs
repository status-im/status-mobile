(ns legacy.status-im.keycard.backup-key
  (:require
    [legacy.status-im.ethereum.mnemonic :as mnemonic]
    [legacy.status-im.keycard.common :as common]
    [legacy.status-im.multiaccounts.recover.core :as multiaccounts.recover]
    [legacy.status-im.signing.core :as signing.core]
    [legacy.status-im.utils.utils :as utils]
    [re-frame.core :as re-frame]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/defn backup-card-pressed
  {:events [:keycard-settings.ui/backup-card-pressed]}
  [{:keys [db] :as cofx} backup-type]
  (log/debug "[keycard] start backup")
  (rf/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :creating-backup?] backup-type))}
            (when (:profile/profile db)
              (navigation/navigate-to :my-profile nil))
            (navigation/navigate-to :seed-phrase nil)))

(rf/defn recovery-card-pressed
  {:events [:keycard-settings.ui/recovery-card-pressed]}
  [{:keys [db] :as cofx} show-warning]
  (rf/merge cofx
            {:db           (-> db
                               ;setting pin-retry-counter is a workaround for the way the PIN view
                               ;decides if it should accept PUK or PIN
                               (assoc-in [:keycard :application-info :pin-retry-counter] 3)
                               (assoc-in [:keycard :factory-reset-card?] true)
                               (dissoc :popover/popover))
             :hide-popover nil}
            (signing.core/discard)
            (if show-warning
              (utils/show-confirmation
               {:title               (i18n/label :t/keycard-recover-title)
                :content             (i18n/label :t/keycard-recover-text)
                :confirm-button-text (i18n/label :t/yes)
                :cancel-button-text  (i18n/label :t/no)
                :on-accept           #(re-frame/dispatch [:keycard-settings.ui/backup-card-pressed
                                                          :recovery-card])
                :on-cancel           #()})
              (backup-card-pressed :recovery-card))))

(rf/defn start-keycard-backup
  {:events [::start-keycard-backup]}
  [{:keys [db] :as cofx}]
  {::multiaccounts.recover/import-multiaccount {:passphrase    (-> db
                                                                   :profile/key-storage
                                                                   :seed-phrase
                                                                   mnemonic/sanitize-passphrase)
                                                :password      nil
                                                :success-event ::create-backup-card}})

(rf/defn create-backup-card
  {:events [::create-backup-card]}
  [{:keys [db] :as cofx} root-data derived-data]
  (rf/merge cofx
            {:db               (-> db
                                   (update :intro-wizard
                                           assoc
                                           :root-key root-data
                                           :derived derived-data
                                           :recovering? true
                                           :selected-storage-type :advanced)
                                   (assoc-in [:keycard :flow] :recovery)
                                   (update :profile/key-storage dissoc :seed-phrase))
             :dismiss-keyboard nil}
            (common/listen-to-hardware-back-button)
            (navigation/navigate-to :keycard-onboarding-intro nil)))
