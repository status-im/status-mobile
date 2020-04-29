(ns status-im.hardwallet.recovery
  (:require [status-im.navigation :as navigation]
            [status-im.utils.datetime :as utils.datetime]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [taoensso.timbre :as log]
            [status-im.hardwallet.common :as common]
            status-im.hardwallet.fx
            [status-im.constants :as constants]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.core :as ethereum]
            [status-im.ui.components.bottom-sheet.core :as bottom-sheet]))

(fx/defn pair* [_ password]
  {:hardwallet/pair {:password password}})

(fx/defn pair
  {:events [:hardwallet/pair]}
  [cofx]
  (let [{:keys [password]} (get-in cofx [:db :hardwallet :secrets])]
    (common/show-connection-sheet
     cofx
     {:on-card-connected :hardwallet/pair
      :handler           (pair* password)})))

(fx/defn pair-code-next-button-pressed
  {:events [:keycard.onboarding.pair.ui/input-submitted
            :hardwallet.ui/pair-code-next-button-pressed
            :keycard.onboarding.pair.ui/next-pressed]}
  [{:keys [db] :as cofx}]
  (let [pairing (get-in db [:hardwallet :secrets :pairing])
        paired-on (get-in db [:hardwallet :secrets :paired-on] (utils.datetime/timestamp))]
    (fx/merge cofx
              (if pairing
                {:db (-> db
                         (assoc-in [:hardwallet :setup-step] :import-multiaccount)
                         (assoc-in [:hardwallet :secrets :paired-on] paired-on))}
                (pair)))))

(fx/defn load-pair-screen
  [{:keys [db] :as cofx}]
  (log/debug "[hardwallet] load-pair-screen")
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:hardwallet :setup-step] :pair))
             :dispatch [:bottom-sheet/hide-sheet]}
            (common/listen-to-hardware-back-button)
            (navigation/navigate-to-cofx :keycard-recovery-pair nil)))

(fx/defn keycard-storage-selected-for-recovery
  {:events [:recovery.ui/keycard-storage-selected]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :flow] :recovery)}
            (navigation/navigate-to-cofx :keycard-recovery-enter-mnemonic nil)))

(fx/defn start-import-flow
  {:events [::recover-with-keycard-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db                           (assoc-in db [:hardwallet :flow] :import)
             :hardwallet/check-nfc-enabled nil}
            (bottom-sheet/hide-bottom-sheet)
            (navigation/navigate-to-cofx :keycard-recovery-intro nil)))

(fx/defn access-key-pressed
  {:events [:multiaccounts.recover.ui/recover-multiaccount-button-pressed]}
  [_]
  {:dispatch [:bottom-sheet/show-sheet :recover-sheet]})

(fx/defn recovery-keycard-selected
  {:events [:recovery.ui/keycard-option-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db                           (assoc-in db [:hardwallet :flow] :recovery)
             :hardwallet/check-nfc-enabled nil}
            (navigation/navigate-to-cofx :keycard-onboarding-intro nil)))

(fx/defn cancel-confirm
  {:events [::cancel-confirm]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            (common/cancel-sheet-confirm)
            (navigation/navigate-back)))

(fx/defn cancel-pressed
  {:events [::cancel-pressed]}
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/keycard-cancel-setup-title)
                          :content             (i18n/label :t/keycard-cancel-setup-text)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/no)
                          :on-accept           #(re-frame/dispatch [::cancel-confirm])
                          :on-cancel           #()}})

(fx/defn begin-setup-pressed
  {:events [:keycard.recovery.intro.ui/begin-recovery-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge
   cofx
   {:db (-> db
            (update :hardwallet
                    dissoc :secrets :card-state :multiaccount-wallet-address
                    :multiaccount-whisper-public-key
                    :application-info)
            (assoc-in [:hardwallet :setup-step] :begin)
            (assoc-in [:hardwallet :pin :on-verified] nil))}
   (common/show-connection-sheet
    {:on-card-connected :hardwallet/get-application-info
     :on-card-read      :hardwallet/check-card-state
     :sheet-options     {:on-cancel [::cancel-pressed]}
     :handler           (common/get-application-info nil :hardwallet/check-card-state)})))

(fx/defn recovery-success-finish-pressed
  {:events [:keycard.recovery.success/finish-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update db :hardwallet dissoc
                         :multiaccount-wallet-address
                         :multiaccount-whisper-public-key)}
            (navigation/navigate-to-cofx :welcome nil)))

(fx/defn recovery-no-key
  {:events [:keycard.recovery.no-key.ui/generate-key-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db                           (assoc-in db [:hardwallet :flow] :create)
             :hardwallet/check-nfc-enabled nil}
            (multiaccounts.create/intro-wizard)))

(fx/defn create-keycard-multiaccount
  [{:keys [db] :as cofx}]
  (let [{{:keys [multiaccount secrets flow]} :hardwallet} db
        {:keys [address
                name
                photo-path
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
                key-uid]} multiaccount
        {:keys [pairing paired-on]} secrets
        {:keys [name photo-path]}
        (if (nil? name)
          ;; name might have been generated during recovery via passphrase
          (get-in db [:intro-wizard :derived constants/path-whisper-keyword])
          {:name       name
           :photo-path photo-path})]
    ;; if a name is still `nil` we have to generate it before multiaccount's
    ;; creation otherwise spec validation will fail
    (if (nil? name)
      {:hardwallet/generate-name-and-photo
       {:public-key whisper-public-key
        :on-success ::on-name-and-photo-generated}}
      (fx/merge cofx
                {:db (-> db
                         (assoc-in [:hardwallet :setup-step] nil)
                         (dissoc :intro-wizard))}
                (multiaccounts.create/on-multiaccount-created
                 {:derived              {constants/path-wallet-root-keyword
                                         {:public-key wallet-root-public-key
                                          :address    (eip55/address->checksum wallet-root-address)}
                                         constants/path-whisper-keyword
                                         {:public-key whisper-public-key
                                          :address    (eip55/address->checksum whisper-address)
                                          :name       name
                                          :photo-path photo-path}
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
                 {})
                (if (= flow :import)
                  (navigation/navigate-replace :keycard-recovery-success nil)
                  (navigation/navigate-to-cofx :welcome nil))))))

(fx/defn on-generate-and-load-key-success
  {:events       [:hardwallet.callback/on-generate-and-load-key-success]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [{:keys [db random-guid-generator] :as cofx} data]
  (let [account-data (js->clj data :keywordize-keys true)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:hardwallet :multiaccount]
                                 (-> account-data
                                     (update :address ethereum/normalized-hex)
                                     (update :whisper-address ethereum/normalized-hex)
                                     (update :wallet-address ethereum/normalized-hex)
                                     (update :wallet-root-address ethereum/normalized-hex)
                                     (update :public-key ethereum/normalized-hex)
                                     (update :whisper-public-key ethereum/normalized-hex)
                                     (update :wallet-public-key ethereum/normalized-hex)
                                     (update :wallet-root-public-key ethereum/normalized-hex)
                                     (update :instance-uid #(get-in db [:hardwallet :multiaccount :instance-uid] %))))
                       (assoc-in [:hardwallet :multiaccount-wallet-address] (:wallet-address account-data))
                       (assoc-in [:hardwallet :multiaccount-whisper-public-key] (:whisper-public-key account-data))
                       (assoc-in [:hardwallet :application-info :key-uid]
                                 (ethereum/normalized-hex (:key-uid account-data)))
                       (update :hardwallet dissoc :recovery-phrase)
                       (update-in [:hardwallet :secrets] dissoc :pin :puk :password)
                       (assoc :multiaccounts/new-installation-id (random-guid-generator))
                       (update-in [:hardwallet :secrets] dissoc :mnemonic))}
              (common/remove-listener-to-hardware-back-button)
              (common/hide-connection-sheet)
              (create-keycard-multiaccount))))

(fx/defn on-generate-and-load-key-error
  {:events [:hardwallet.callback/on-generate-and-load-key-error]}
  [{:keys [db] :as cofx} {:keys [error code]}]
  (log/debug "[hardwallet] generate and load key error: " error)
  (when-not (common/tag-lost? error)
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :setup-error] error)}
              (common/set-on-card-connected :hardwallet/load-loading-keys-screen)
              (common/process-error code error))))

(fx/defn import-multiaccount
  {:events [:hardwallet/import-multiaccount]}
  [{:keys [db] :as cofx}]
  (let [{:keys [pairing]} (get-in db [:hardwallet :secrets])
        instance-uid      (get-in db [:hardwallet :application-info :instance-uid])
        key-uid           (get-in db [:hardwallet :application-info :key-uid])
        pairing'          (or pairing (common/get-pairing db key-uid))
        pin               (common/vector->string (get-in db [:hardwallet :pin :import-multiaccount]))]
    (fx/merge cofx
              {:db                  (-> db
                                        (assoc-in [:hardwallet :multiaccount :instance-uid] instance-uid)
                                        (assoc-in [:hardwallet :secrets] {:pairing   pairing'
                                                                          :paired-on (utils.datetime/timestamp)}))
               :hardwallet/get-keys {:pairing    pairing'
                                     :pin        pin
                                     :on-success :hardwallet.callback/on-generate-and-load-key-success}})))

(fx/defn load-recovering-key-screen
  {:events [:hardwallet/load-recovering-key-screen]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :hardwallet/load-recovering-key-screen
    :handler           (common/dispatch-event :hardwallet/import-multiaccount)}))

(fx/defn on-name-and-photo-generated
  {:events [::on-name-and-photo-generated]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [{:keys [db] :as cofx} whisper-name photo-path]
  (fx/merge
   cofx
   {:db (update-in db [:hardwallet :multiaccount]
                   (fn [multiacc]
                     (assoc multiacc
                            :name whisper-name
                            :photo-path photo-path)))}
   (create-keycard-multiaccount)))
