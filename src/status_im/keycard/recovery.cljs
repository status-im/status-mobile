(ns status-im.keycard.recovery
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.bottom-sheet.core :as bottom-sheet]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [i18n.i18n :as i18n]
            [status-im.keycard.common :as common]
            status-im.keycard.fx
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.native-module.core :as status]
            [status-im.popover.core :as popover]
            [utils.re-frame :as rf]
            [utils.datetime :as datetime]
            [status-im.utils.keychain.core :as keychain]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [status-im2.navigation.events :as navigation]
            [taoensso.timbre :as log]
            [utils.security.core :as security]))

(rf/defn pair*
  [_ password]
  {:keycard/pair {:password password}})

(rf/defn pair
  {:events [:keycard/pair]}
  [cofx]
  (let [{:keys [password]} (get-in cofx [:db :keycard :secrets])]
    (common/show-connection-sheet
     cofx
     {:on-card-connected :keycard/pair
      :handler           (pair* password)})))

(rf/defn pair-code-next-button-pressed
  {:events [:keycard.onboarding.pair.ui/input-submitted
            :keycard.ui/pair-code-next-button-pressed
            :keycard.onboarding.pair.ui/next-pressed]}
  [{:keys [db] :as cofx}]
  (let [pairing   (get-in db [:keycard :secrets :pairing])
        paired-on (get-in db [:keycard :secrets :paired-on] (datetime/timestamp))]
    (rf/merge cofx
              (if pairing
                {:db (-> db
                         (assoc-in [:keycard :setup-step] :import-multiaccount)
                         (assoc-in [:keycard :secrets :paired-on] paired-on))}
                (pair)))))

(rf/defn load-pair-screen
  [{:keys [db] :as cofx}]
  (log/debug "[keycard] load-pair-screen")
  (rf/merge cofx
            {:db       (-> db
                           (assoc-in [:keycard :setup-step] :pair))
             :dispatch [:bottom-sheet/hide]}
            (common/listen-to-hardware-back-button)
            (navigation/navigate-to-cofx :keycard-recovery-pair nil)))

(rf/defn keycard-storage-selected-for-recovery
  {:events [:recovery.ui/keycard-storage-selected]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc-in db [:keycard :flow] :recovery)}
            (navigation/navigate-to-cofx :keycard-recovery-enter-mnemonic nil)))

(rf/defn start-import-flow
  {:events [::recover-with-keycard-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db
             (-> db
                 (assoc-in [:keycard :flow] :import)
                 (assoc :recovered-account? true))
             :keycard/check-nfc-enabled nil}
            (bottom-sheet/hide-bottom-sheet)
            (navigation/navigate-to-cofx :keycard-recovery-intro nil)))

(rf/defn access-key-pressed
  {:events [:multiaccounts.recover.ui/recover-multiaccount-button-pressed]}
  [_]
  {:dispatch [:bottom-sheet/show-sheet :recover-sheet]})

(rf/defn recovery-keycard-selected
  {:events [:recovery.ui/keycard-option-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db                        (assoc-in db [:keycard :flow] :recovery)
             :keycard/check-nfc-enabled nil}
            (common/listen-to-hardware-back-button)
            (navigation/navigate-to-cofx :keycard-onboarding-intro nil)))

(rf/defn cancel-pressed
  {:events [::cancel-pressed]}
  [cofx]
  (rf/merge cofx
            (common/cancel-sheet-confirm)
            (navigation/navigate-back)))

(rf/defn begin-setup-pressed
  {:events [:keycard.recovery.intro.ui/begin-recovery-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (-> db
            (update :keycard
                    dissoc
                    :secrets
                    :card-state                      :multiaccount-wallet-address
                    :multiaccount-whisper-public-key
                    :application-info)
            (assoc-in [:keycard :setup-step] :begin)
            (assoc-in [:keycard :pin :on-verified] nil))}
   (common/show-connection-sheet
    {:on-card-connected :keycard/get-application-info
     :on-card-read      :keycard/check-card-state
     :sheet-options     {:on-cancel [::cancel-pressed]}
     :handler           (common/get-application-info :keycard/check-card-state)})))

(rf/defn recovery-success-finish-pressed
  {:events [:keycard.recovery.success/finish-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (update db
                         :keycard                     dissoc
                         :multiaccount-wallet-address
                         :multiaccount-whisper-public-key)}
            (navigation/navigate-to-cofx (if platform/android?
                                           :notifications-settings
                                           :welcome)
                                         nil)))

(rf/defn intro-wizard
  {:events [:multiaccounts.create.ui/intro-wizard]}
  [{:keys [db] :as cofx}]
  (let [accs (get db :multiaccounts/multiaccounts)]
    (rf/merge cofx
              {:db (-> db
                       (update :keycard dissoc :flow)
                       (dissoc :restored-account?))}
              (multiaccounts.create/prepare-intro-wizard)
              (if (pos? (count accs))
                (navigation/navigate-to-cofx :get-your-keys nil)
                (navigation/set-stack-root :onboarding [:get-your-keys])))))

(rf/defn recovery-no-key
  {:events [:keycard.recovery.no-key.ui/generate-key-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db                        (assoc-in db [:keycard :flow] :create)
             :keycard/check-nfc-enabled nil}
            (intro-wizard)))

(rf/defn create-keycard-multiaccount
  {:events       [::create-keycard-multiaccount]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [{:keys [db] :as cofx}]
  (let [{{:keys [multiaccount secrets flow]} :keycard} db
        {:keys [address
                name
                identicon
                public-key
                whisper-public-key
                wallet-public-key
                wallet-root-public-key
                whisper-address
                wallet-address
                wallet-root-address
                whisper-private-key
                encryption-public-key
                instance-uid
                key-uid
                recovered]}
        multiaccount
        {:keys [pairing paired-on]} secrets
        {:keys [name identicon]}
        (if (nil? name)
          ;; name might have been generated during recovery via passphrase
          (get-in db [:intro-wizard :derived constants/path-whisper-keyword])
          {:name      name
           :identicon identicon})]
    ;; if a name is still `nil` we have to generate it before multiaccount's
    ;; creation otherwise spec validation will fail
    (if (nil? name)
      {:keycard/generate-name-and-photo
       {:public-key whisper-public-key
        :on-success ::on-name-and-photo-generated}}
      (rf/merge cofx
                {:db (-> db
                         (assoc-in [:keycard :setup-step] nil)
                         (dissoc :intro-wizard))}
                (multiaccounts.create/on-multiaccount-created
                 {:recovered            (or recovered (get-in db [:intro-wizard :recovering?]))
                  :derived              {constants/path-wallet-root-keyword
                                         {:public-key wallet-root-public-key
                                          :address    (eip55/address->checksum wallet-root-address)}
                                         constants/path-whisper-keyword
                                         {:public-key whisper-public-key
                                          :address    (eip55/address->checksum whisper-address)
                                          :name       name
                                          :identicon  identicon}
                                         constants/path-default-wallet-keyword
                                         {:public-key wallet-public-key
                                          :address    (eip55/address->checksum wallet-address)}}
                  :address              address
                  :public-key           public-key
                  :keycard-instance-uid instance-uid
                  :key-uid              (ethereum/normalized-hex key-uid)
                  :keycard-pairing      pairing
                  :keycard-paired-on    paired-on
                  :chat-key             whisper-private-key}
                 encryption-public-key
                 {})))))

(rf/defn return-to-keycard-login
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (-> db
                     (update-in [:keycard :pin]
                                assoc
                                :enter-step :login
                                :status     nil
                                :login      [])
                     (update :keycard dissoc :application-info))}
            (navigation/set-stack-root :multiaccounts-stack
                                       [:multiaccounts
                                        :keycard-login-pin])))

(rf/defn on-backup-success
  [{:keys [db] :as cofx} backup-type]
  (rf/merge cofx
            {:utils/show-popup {:title   (i18n/label (if (= backup-type :recovery-card)
                                                       :t/keycard-access-reset
                                                       :t/keycard-backup-success-title))
                                :content (i18n/label (if (= backup-type :recovery-card)
                                                       :t/keycard-can-use-with-new-passcode
                                                       :t/keycard-backup-success-body))}}
            (cond
              (multiaccounts.model/logged-in? cofx)
              (navigation/set-stack-root :profile-stack [:my-profile :keycard-settings])

              (:multiaccounts/login db)
              (return-to-keycard-login)

              :else
              (navigation/set-stack-root :onboarding [:get-your-keys]))))

(re-frame/reg-fx
 ::finish-migration
 (fn [[account settings password encryption-pass login-params]]
   (status/convert-to-keycard-account
    account
    settings
    password
    encryption-pass
    #(let [{:keys [error]} (types/json->clj %)]
       (if (string/blank? error)
         (status/login-with-keycard login-params)
         (throw
          (js/Error.
           "Please shake the phone to report this error and restart the app. Migration failed unexpectedly.")))))))

(rf/defn migrate-account
  [{:keys [db] :as cofx}]
  (let [pairing         (get-in db [:keycard :secrets :pairing])
        paired-on       (get-in db [:keycard :secrets :paired-on])
        instance-uid    (get-in db [:keycard :multiaccount :instance-uid])
        account         (-> db
                            :multiaccounts/login
                            (assoc :keycard-pairing pairing)
                            (assoc :save-password? false))
        key-uid         (-> account :key-uid)
        settings        {:keycard-instance-uid instance-uid
                         :keycard-paired-on    paired-on
                         :keycard-pairing      pairing}
        password        (ethereum/sha3 (security/safe-unmask-data (get-in db
                                                                          [:keycard
                                                                           :migration-password])))
        encryption-pass (get-in db [:keycard :multiaccount :encryption-public-key])
        login-params    {:key-uid           key-uid
                         :multiaccount-data (types/clj->json account)
                         :password          encryption-pass
                         :chat-key          (get-in db [:keycard :multiaccount :whisper-private-key])}]
    {:db                (-> db
                            (assoc-in [:multiaccounts/multiaccounts key-uid :keycard-pairing] pairing)
                            (assoc :multiaccounts/login account)
                            (assoc :auth-method keychain/auth-method-none)
                            (update :keycard dissoc :flow :migration-password)
                            (dissoc :recovered-account?))
     ::finish-migration [account settings password encryption-pass login-params]}))

(rf/defn delete-multiaccount
  [{:keys [db]}]
  (let [key-uid (get-in db [:multiaccounts/login :key-uid])]
    {:keycard/delete-multiaccount-before-migration
     {:key-uid    key-uid
      :on-error   #(re-frame/dispatch [::delete-multiaccount-error %])
      :on-success #(re-frame/dispatch [::create-keycard-multiaccount])}}))

(rf/defn handle-delete-multiaccount-error
  {:events [::delete-multiaccount-error]}
  [cofx _]
  (popover/show-popover cofx {:view :transfer-multiaccount-unknown-error}))

(rf/defn on-generate-and-load-key-success
  {:events       [:keycard.callback/on-generate-and-load-key-success]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [{:keys [db random-guid-generator] :as cofx} data]
  (let [account-data (js->clj data :keywordize-keys true)
        backup?      (get-in db [:keycard :creating-backup?])
        migration?   (get-in db [:keycard :converting-account?])]
    (rf/merge
     cofx
     {:db (-> db
              (assoc-in
               [:keycard :multiaccount]
               (-> account-data
                   (update :address ethereum/normalized-hex)
                   (update :whisper-address ethereum/normalized-hex)
                   (update :wallet-address ethereum/normalized-hex)
                   (update :wallet-root-address ethereum/normalized-hex)
                   (update :public-key ethereum/normalized-hex)
                   (update :whisper-public-key ethereum/normalized-hex)
                   (update :wallet-public-key ethereum/normalized-hex)
                   (update :wallet-root-public-key ethereum/normalized-hex)
                   (update :instance-uid #(get-in db [:keycard :multiaccount :instance-uid] %))))
              (assoc-in [:keycard :multiaccount-wallet-address] (:wallet-address account-data))
              (assoc-in [:keycard :multiaccount-whisper-public-key] (:whisper-public-key account-data))
              (assoc-in [:keycard :pin :status] nil)
              (assoc-in [:keycard :application-info :key-uid]
                        (ethereum/normalized-hex (:key-uid account-data)))
              (update :keycard dissoc :recovery-phrase :creating-backup? :converting-account?)
              (update-in [:keycard :secrets] dissoc :pin :puk :password :mnemonic)
              (assoc :multiaccounts/new-installation-id (random-guid-generator)))}
     (common/remove-listener-to-hardware-back-button)
     (common/hide-connection-sheet)
     (cond backup?                                 (on-backup-success backup?)
           migration?                              (migrate-account)

           (get-in db [:keycard :delete-account?])
           (delete-multiaccount)

           :else                                   (create-keycard-multiaccount)))))

(rf/defn on-generate-and-load-key-error
  {:events [:keycard.callback/on-generate-and-load-key-error]}
  [{:keys [db] :as cofx} {:keys [error code]}]
  (log/debug "[keycard] generate and load key error: " error)
  (when-not (common/tag-lost? error)
    (rf/merge cofx
              {:db (assoc-in db [:keycard :setup-error] error)}
              (common/set-on-card-connected :keycard/load-loading-keys-screen)
              (common/process-error code error))))

(rf/defn import-multiaccount
  {:events [:keycard/import-multiaccount]}
  [{:keys [db] :as cofx}]
  (let [{:keys [pairing]} (get-in db [:keycard :secrets])
        instance-uid      (get-in db [:keycard :application-info :instance-uid])
        key-uid           (get-in db [:keycard :application-info :key-uid])
        pairing'          (or pairing (common/get-pairing db key-uid))
        pin               (common/vector->string (get-in db [:keycard :pin :import-multiaccount]))]
    (rf/merge cofx
              {:db                  (-> db
                                        (assoc-in [:keycard :multiaccount :instance-uid] instance-uid)
                                        (assoc-in [:keycard :pin :status] :verifying)
                                        (assoc-in [:keycard :secrets]
                                                  {:pairing   pairing'
                                                   :paired-on (datetime/timestamp)}))
               :keycard/import-keys
               {:pin        pin
                :on-success :keycard.callback/on-generate-and-load-key-success}})))

(rf/defn load-recovering-key-screen
  {:events [:keycard/load-recovering-key-screen]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :keycard/load-recovering-key-screen
    :handler           (common/dispatch-event :keycard/import-multiaccount)}))

(rf/defn on-name-and-photo-generated
  {:events       [::on-name-and-photo-generated]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [{:keys [db] :as cofx} whisper-name identicon]
  (rf/merge
   cofx
   {:db (update-in db
                   [:keycard :multiaccount]
                   (fn [multiacc]
                     (assoc multiacc
                            :recovered (get db :recovered-account?)
                            :name      whisper-name
                            :identicon identicon)))}
   (create-keycard-multiaccount)))
