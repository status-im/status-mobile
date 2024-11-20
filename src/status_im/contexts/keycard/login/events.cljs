(ns status-im.contexts.keycard.login.events
  (:require [status-im.constants :as constants]
            [status-im.contexts.profile.config :as profile.config]
            [utils.address :as address]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :keycard.login/on-get-keys-success
 (fn [{:keys [db]} [data]]
   (let [{:keys [key-uid encryption-public-key
                 whisper-private-key]} data
         profile                       (get-in db [:profile/profiles-overview key-uid])]
     {:db
      (-> db
          (dissoc :keycard)
          (update :profile/login assoc
                  :password      encryption-public-key
                  :key-uid       key-uid
                  :name          (:name profile)))
      :fx [[:dispatch [:keycard/disconnect]]
           [:effects.keycard/login-with-keycard
            {:password            encryption-public-key
             :whisper-private-key whisper-private-key
             :key-uid             key-uid}]]})))

(rf/reg-event-fx :keycard.login/on-get-keys-from-keychain-success
 (fn [{:keys [db]} [key-uid [encryption-public-key whisper-private-key]]]
   (when (and encryption-public-key whisper-private-key)
     (let [profile (get-in db [:profile/profiles-overview key-uid])]
       {:db
        (-> db
            (dissoc :keycard)
            (update :profile/login assoc
                    :password      encryption-public-key
                    :key-uid       key-uid
                    :name          (:name profile)))
        :fx [[:dispatch [:keycard/disconnect]]
             [:effects.keycard/login-with-keycard
              {:password            encryption-public-key
               :whisper-private-key whisper-private-key
               :key-uid             key-uid}]]}))))

(rf/reg-event-fx :keycard.login/check-card
 (fn []
   {:fx [[:dispatch
          [:keycard/connect
           {:on-error
            (fn [error]
              (if (= error :keycard/error.keycard-wrong-profile)
                (do
                  (rf/dispatch [:keycard/disconnect])
                  (rf/dispatch [:open-modal :screen/keycard.pin.enter
                                {:on-complete #(rf/dispatch [:keycard.login/prepare-for-profile-recovery
                                                             %])}]))
                (rf/dispatch [:keycard/on-application-info-error error])))}]]]}))

(rf/reg-event-fx :keycard.login/prepare-for-profile-recovery
 (fn [{:keys [db]} [pin]]
   {:fx [[:dispatch
          [:keycard/connect
           {:key-uid    (get-in db [:keycard :application-info :key-uid])
            :on-success (fn []
                          (rf/dispatch
                           [:keycard/get-more-keys
                            {:pin        pin
                             :on-success #(rf/dispatch [:keycard.login/recover-profile-and-login %])
                             :on-failure #(rf/dispatch [:keycard/on-action-with-pin-error %])}]))}]]]}))

(rf/reg-event-fx :keycard.login/recover-profile-and-login
 (fn [{:keys [db]}
      [{:keys [key-uid encryption-public-key whisper-private-key instance-uid whisper-public-key
               whisper-address address wallet-public-key wallet-address wallet-root-address]}]]
   (let [{:keys [pairing]} (get-in db [:keycard :pairings instance-uid])]
     {:db
      (-> db
          (assoc :onboarding/recovered-account? true)
          (assoc-in [:syncing :login-sha3-password] encryption-public-key))

      :fx [[:dispatch [:keycard/disconnect]]
           [:dispatch [:navigate-to :screen/onboarding.preparing-status]]
           [:effects.profile/restore-and-login
            (assoc (profile.config/create)
                   :keycardInstanceUID instance-uid
                   :keycardPairingKey  pairing
                   :keycard            {:keyUID              key-uid
                                        :address             (address/normalized-hex address)
                                        :whisperPrivateKey   (address/normalized-hex whisper-private-key)
                                        :whisperPublicKey    (address/normalized-hex whisper-public-key)
                                        :whisperAddress      (address/normalized-hex whisper-address)
                                        :walletPublicKey     (address/normalized-hex wallet-public-key)
                                        :walletAddress       (address/normalized-hex wallet-address)
                                        :walletRootAddress   (address/normalized-hex wallet-root-address)
                                        :encryptionPublicKey encryption-public-key
                                        :eip1581Address      ""}
                   :displayName        ""
                   :password           encryption-public-key
                   :customizationColor constants/profile-default-color
                   :fetchBackup        true)]]})))
