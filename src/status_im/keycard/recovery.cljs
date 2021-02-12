(ns status-im.keycard.recovery
  (:require [status-im.navigation :as navigation]
            [status-im.utils.datetime :as utils.datetime]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [taoensso.timbre :as log]
            [status-im.keycard.common :as common]
            status-im.keycard.fx
            [status-im.constants :as constants]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.core :as ethereum]
            [status-im.bottom-sheet.core :as bottom-sheet]
            [status-im.utils.platform :as platform]))

(fx/defn pair* [_ password]
  {:keycard/pair {:password password}})

(fx/defn pair
  {:events [:keycard/pair]}
  [cofx]
  (let [{:keys [password]} (get-in cofx [:db :keycard :secrets])]
    (common/show-connection-sheet
     cofx
     {:on-card-connected :keycard/pair
      :handler           (pair* password)})))

(fx/defn pair-code-next-button-pressed
  {:events [:keycard.onboarding.pair.ui/input-submitted
            :keycard.ui/pair-code-next-button-pressed
            :keycard.onboarding.pair.ui/next-pressed]}
  [{:keys [db] :as cofx}]
  (let [pairing (get-in db [:keycard :secrets :pairing])
        paired-on (get-in db [:keycard :secrets :paired-on] (utils.datetime/timestamp))]
    (fx/merge cofx
              (if pairing
                {:db (-> db
                         (assoc-in [:keycard :setup-step] :import-multiaccount)
                         (assoc-in [:keycard :secrets :paired-on] paired-on))}
                (pair)))))

(fx/defn load-pair-screen
  [{:keys [db] :as cofx}]
  (log/debug "[keycard] load-pair-screen")
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :setup-step] :pair))
             :dispatch [:bottom-sheet/hide]}
            (common/listen-to-hardware-back-button)
            (navigation/navigate-to-cofx :keycard-recovery-pair nil)))

(fx/defn keycard-storage-selected-for-recovery
  {:events [:recovery.ui/keycard-storage-selected]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:keycard :flow] :recovery)}
            (navigation/navigate-to-cofx :keycard-recovery-enter-mnemonic nil)))

(fx/defn start-import-flow
  {:events [::recover-with-keycard-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db                           (assoc-in db [:keycard :flow] :import)
             :keycard/check-nfc-enabled nil}
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
            {:db                           (assoc-in db [:keycard :flow] :recovery)
             :keycard/check-nfc-enabled nil}
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
            (update :keycard
                    dissoc :secrets :card-state :multiaccount-wallet-address
                    :multiaccount-whisper-public-key
                    :application-info)
            (assoc-in [:keycard :setup-step] :begin)
            (assoc-in [:keycard :pin :on-verified] nil))}
   (common/show-connection-sheet
    {:on-card-connected :keycard/get-application-info
     :on-card-read      :keycard/check-card-state
     :sheet-options     {:on-cancel [::cancel-pressed]}
     :handler           (common/get-application-info nil :keycard/check-card-state)})))

(fx/defn recovery-success-finish-pressed
  {:events [:keycard.recovery.success/finish-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update db :keycard dissoc
                         :multiaccount-wallet-address
                         :multiaccount-whisper-public-key)}
            (navigation/navigate-to-cofx (if platform/android?
                                           :notifications-settings :welcome) nil)))

(fx/defn recovery-no-key
  {:events [:keycard.recovery.no-key.ui/generate-key-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db                           (assoc-in db [:keycard :flow] :create)
             :keycard/check-nfc-enabled nil}
            (multiaccounts.create/intro-wizard)))

(fx/defn create-keycard-multiaccount
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
                recovered]}                            multiaccount
        {:keys [pairing paired-on]}                    secrets
        {:keys [name identicon]}
        (if (nil? name)
          ;; name might have been generated during recovery via passphrase
          (get-in db [:intro-wizard :derived constants/path-whisper-keyword])
          {:name       name
           :identicon identicon})]
    ;; if a name is still `nil` we have to generate it before multiaccount's
    ;; creation otherwise spec validation will fail
    (if (nil? name)
      {:keycard/generate-name-and-photo
       {:public-key whisper-public-key
        :on-success ::on-name-and-photo-generated}}
      (fx/merge cofx
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
                                          :identicon identicon}
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
                  (navigation/navigate-to-cofx (if platform/android?
                                                 :notifications-settings :welcome) nil))))))

(fx/defn on-generate-and-load-key-success
  {:events       [:keycard.callback/on-generate-and-load-key-success]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [{:keys [db random-guid-generator] :as cofx} data]
  (let [account-data (js->clj data :keywordize-keys true)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:keycard :multiaccount]
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
                       (assoc-in [:keycard :application-info :key-uid]
                                 (ethereum/normalized-hex (:key-uid account-data)))
                       (update :keycard dissoc :recovery-phrase)
                       (update-in [:keycard :secrets] dissoc :pin :puk :password)
                       (assoc :multiaccounts/new-installation-id (random-guid-generator))
                       (update-in [:keycard :secrets] dissoc :mnemonic))}
              (common/remove-listener-to-hardware-back-button)
              (common/hide-connection-sheet)
              (create-keycard-multiaccount))))

(fx/defn on-generate-and-load-key-error
  {:events [:keycard.callback/on-generate-and-load-key-error]}
  [{:keys [db] :as cofx} {:keys [error code]}]
  (log/debug "[keycard] generate and load key error: " error)
  (when-not (common/tag-lost? error)
    (fx/merge cofx
              {:db (assoc-in db [:keycard :setup-error] error)}
              (common/set-on-card-connected :keycard/load-loading-keys-screen)
              (common/process-error code error))))

(fx/defn import-multiaccount
  {:events [:keycard/import-multiaccount]}
  [{:keys [db] :as cofx}]
  (let [{:keys [pairing]} (get-in db [:keycard :secrets])
        instance-uid      (get-in db [:keycard :application-info :instance-uid])
        key-uid           (get-in db [:keycard :application-info :key-uid])
        pairing'          (or pairing (common/get-pairing db key-uid))
        pin               (common/vector->string (get-in db [:keycard :pin :import-multiaccount]))]
    (fx/merge cofx
              {:db                  (-> db
                                        (assoc-in [:keycard :multiaccount :instance-uid] instance-uid)
                                        (assoc-in [:keycard :secrets] {:pairing   pairing'
                                                                       :paired-on (utils.datetime/timestamp)}))
               :keycard/get-keys {:pairing    pairing'
                                  :pin        pin
                                  :on-success :keycard.callback/on-generate-and-load-key-success}})))

(fx/defn load-recovering-key-screen
  {:events [:keycard/load-recovering-key-screen]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :keycard/load-recovering-key-screen
    :handler           (common/dispatch-event :keycard/import-multiaccount)}))

(fx/defn on-name-and-photo-generated
  {:events [::on-name-and-photo-generated]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [{:keys [db] :as cofx} whisper-name identicon]
  (fx/merge
   cofx
   {:db (update-in db [:keycard :multiaccount]
                   (fn [multiacc]
                     (assoc multiacc
                            :recovered true
                            :name whisper-name
                            :identicon identicon)))}
   (create-keycard-multiaccount)))
