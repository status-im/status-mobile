(ns status-im.keycard.onboarding
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.keycard.common :as common]
            [status-im.keycard.mnemonic :as mnemonic]
            [taoensso.timbre :as log]
            status-im.keycard.fx
            [status-im.ui.components.react :as react]
            [status-im.constants :as constants]))

(fx/defn begin-setup-button-pressed
  {:keys [:keycard.ui/begin-setup-button-pressed]}
  [{:keys [db]}]
  {:db (-> db
           (assoc-in [:keycard :setup-step] :pin)
           (assoc-in [:keycard :pin :enter-step] :original)
           (assoc-in [:keycard :pin :original] [])
           (assoc-in [:keycard :pin :confirmation] []))})

(fx/defn start-installation
  [{:keys [db] :as cofx}]
  (let [card-state (get-in db [:keycard :card-state])
        pin        (common/vector->string (get-in db [:keycard :pin :original]))]
    (log/debug "start-installation: card-state" card-state)
    (case card-state

      :pre-init
      {:keycard/init-card pin}

      (do
        (log/debug (str "Cannot start keycard installation from state: " card-state))
        (fx/merge cofx
                  {:utils/show-popup {:title   (i18n/label :t/error)
                                      :content (i18n/label :t/something-went-wrong)}}
                  (navigation/navigate-to-cofx :keycard-authentication-method nil))))))

(fx/defn load-preparing-screen
  {:events [:keycard/load-preparing-screen]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:sheet-options     {:on-cancel [::cancel-pressed]}
    :on-card-connected :keycard/load-preparing-screen
    :handler           start-installation}))

(fx/defn load-pairing-screen
  {:events [:keycard/load-pairing-screen
            :keycard.onboarding.puk-code.ui/confirm-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge
   cofx
   {:db (assoc-in db [:keycard :setup-step] :pairing)}
   (common/show-connection-sheet
    {:sheet-options     {:on-cancel [::cancel-pressed]}
     :on-card-connected :keycard/load-pairing-screen
     :handler           (common/dispatch-event :keycard/pair)})))

(fx/defn puk-code-next-pressed
  {:events [:keycard.onboarding.puk-code.ui/next-pressed]}
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/secret-keys-confirmation-title)
                          :content             (i18n/label :t/secret-keys-confirmation-text)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/cancel)
                          :on-accept           #(re-frame/dispatch [:keycard.onboarding.puk-code.ui/confirm-pressed])
                          :on-cancel           #()}})

(fx/defn load-finishing-screen
  {:events [:keycard.onboarding.recovery-phrase-confirm-word2.ui/next-pressed
            :keycard/load-finishing-screen]}
  [{:keys [db] :as cofx}]
  (fx/merge
   cofx
   {:db (assoc-in db [:keycard :setup-step] :loading-keys)}
   (common/show-connection-sheet
    {:sheet-options     {:on-cancel [::cancel-pressed]}
     :on-card-connected :keycard/load-finishing-screen
     :handler           (common/dispatch-event :keycard/generate-and-load-key)})))

(fx/defn recovery-phrase-learn-more-pressed
  {:events [:keycard.onboarding.recovery-phrase.ui/learn-more-pressed]}
  [_]
  (.openURL ^js react/linking constants/keycard-integration-link))

(fx/defn recovery-phrase-next-pressed
  {:events [:keycard.onboarding.recovery-phrase.ui/next-pressed
            :keycard.ui/recovery-phrase-next-button-pressed]}
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/keycard-recovery-phrase-confirmation-title)
                          :content             (i18n/label :t/keycard-recovery-phrase-confirmation-text)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/cancel)
                          :on-accept           #(re-frame/dispatch [:keycard.onboarding.recovery-phrase.ui/confirm-pressed])
                          :on-cancel           #()}})

(fx/defn recovery-phrase-start-confirmation
  [{:keys [db] :as cofx}]
  (let [mnemonic      (get-in db [:keycard :secrets :mnemonic])
        [word1 word2] (shuffle (map-indexed vector (clojure.string/split mnemonic #" ")))
        word1         (zipmap [:idx :word] word1)
        word2         (zipmap [:idx :word] word2)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:keycard :setup-step] :recovery-phrase-confirm-word1)
                       (assoc-in [:keycard :recovery-phrase :step] :word1)
                       (assoc-in [:keycard :recovery-phrase :confirm-error] nil)
                       (assoc-in [:keycard :recovery-phrase :input-word] nil)
                       (assoc-in [:keycard :recovery-phrase :word1] word1)
                       (assoc-in [:keycard :recovery-phrase :word2] word2))}
              (common/remove-listener-to-hardware-back-button))))

(fx/defn recovery-phrase-confirm-pressed
  {:events [:keycard.onboarding.recovery-phrase.ui/confirm-pressed]}
  [cofx]
  (fx/merge cofx
            (recovery-phrase-start-confirmation)
            (navigation/navigate-to-cofx :keycard-onboarding-recovery-phrase-confirm-word1 nil)))

(fx/defn recovery-phrase-next-word
  [{:keys [db]}]
  {:db (-> db
           (assoc-in [:keycard :recovery-phrase :step] :word2)
           (assoc-in [:keycard :recovery-phrase :confirm-error] nil)
           (assoc-in [:keycard :recovery-phrase :input-word] nil)
           (assoc-in [:keycard :setup-step] :recovery-phrase-confirm-word2))})

(fx/defn recovery-phrase-confirm-word-back-pressed
  {:events [:keycard.onboarding.recovery-phrase-confirm-word.ui/back-pressed]}
  [{:keys [db] :as cofx}]
  (if (= (:view-id db) :keycard-onboarding-recovery-phrase-confirm-word1)
    (navigation/navigate-to-cofx cofx :keycard-onboarding-recovery-phrase nil)
    (navigation/navigate-to-cofx cofx :keycard-onboarding-recovery-phrase-confirm-word1 nil)))

(fx/defn proceed-with-generating-key
  [{:keys [db] :as cofx}]
  (let [pin (get-in db [:keycard :secrets :pin]
                    (common/vector->string (get-in db [:keycard :pin :current])))]
    (if (empty? pin)
      (fx/merge cofx
                {:db (-> db
                         (assoc-in [:keycard :pin] {:enter-step  :current
                                                    :on-verified :keycard/generate-and-load-key
                                                    :current     []})
                         (assoc-in [:keycard :setup-step] :loading-keys))}
                (navigation/navigate-to-cofx :keycard-onboarding-pin nil))
      (load-finishing-screen cofx))))

(fx/defn recovery-phrase-confirm-word-next-pressed
  {:events [:keycard.onboarding.recovery-phrase-confirm-word.ui/next-pressed
            :keycard.onboarding.recovery-phrase-confirm-word.ui/input-submitted]}
  [{:keys [db] :as cofx}]
  (let [step (get-in db [:keycard :recovery-phrase :step])
        input-word (get-in db [:keycard :recovery-phrase :input-word])
        {:keys [word]} (get-in db [:keycard :recovery-phrase step])]
    (if (= word input-word)
      (if (= (:view-id db) :keycard-onboarding-recovery-phrase-confirm-word1)
        (fx/merge cofx
                  (recovery-phrase-next-word)
                  (navigation/navigate-replace :keycard-onboarding-recovery-phrase-confirm-word2 nil))
        (proceed-with-generating-key cofx))
      {:db (assoc-in db [:keycard :recovery-phrase :confirm-error] (i18n/label :t/wrong-word))})))

(fx/defn recovery-phrase-confirm-word-input-changed
  {:events [:keycard.onboarding.recovery-phrase-confirm-word.ui/input-changed]}
  [{:keys [db]} input]
  {:db (assoc-in db [:keycard :recovery-phrase :input-word] input)})

(fx/defn pair-code-input-changed
  {:events [:keycard.onboarding.pair.ui/input-changed]}
  [{:keys [db]} input]
  {:db (assoc-in db [:keycard :secrets :password] input)})

(fx/defn keycard-option-pressed
  {:events [:onboarding.ui/keycard-option-pressed]}
  [{:keys [db] :as cofx}]
  (let [flow (get-in db [:keycard :flow])]
    (fx/merge cofx
              {:keycard/check-nfc-enabled nil}
              (if (= flow :import)
                (navigation/navigate-to-cofx :keycard-recovery-intro nil)
                (navigation/navigate-to-cofx :keycard-onboarding-intro nil)))))

(fx/defn start-onboarding-flow
  {:events [:keycard/start-onboarding-flow]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db                           (assoc-in db [:keycard :flow] :create)
             :keycard/check-nfc-enabled nil}
            (navigation/navigate-to-cofx :keycard-onboarding-intro nil)))

(fx/defn open-nfc-settings-pressed
  {:events [:keycard.onboarding.nfc-on/open-nfc-settings-pressed]}
  [_]
  {:keycard/open-nfc-settings nil})

(defn- show-recover-confirmation []
  {:ui/show-confirmation {:title               (i18n/label :t/are-you-sure?)
                          :content             (i18n/label :t/are-you-sure-description)
                          :confirm-button-text (clojure.string/upper-case (i18n/label :t/yes))
                          :cancel-button-text  (i18n/label :t/see-it-again)
                          :on-accept           #(re-frame/dispatch [:keycard.ui/recovery-phrase-confirm-pressed])
                          :on-cancel           #(re-frame/dispatch [:keycard.ui/recovery-phrase-cancel-pressed])}})

(fx/defn recovery-phrase-confirm-word
  {:events [:keycard.ui/recovery-phrase-confirm-word-next-button-pressed]}
  [{:keys [db]}]
  (let [step (get-in db [:keycard :recovery-phrase :step])
        input-word (get-in db [:keycard :recovery-phrase :input-word])
        {:keys [word]} (get-in db [:keycard :recovery-phrase step])]
    (if (= word input-word)
      (if (= step :word1)
        (recovery-phrase-next-word db)
        (show-recover-confirmation))
      {:db (assoc-in db [:keycard :recovery-phrase :confirm-error] (i18n/label :t/wrong-word))})))

(fx/defn recovery-phrase-next-button-pressed
  [{:keys [db] :as cofx}]
  (if (= (get-in db [:keycard :flow]) :create)
    (recovery-phrase-start-confirmation cofx)
    (let [mnemonic (get-in db [:multiaccounts/recover :passphrase])]
      (fx/merge cofx
                {:db (assoc-in db [:keycard :secrets :mnemonic] mnemonic)}
                (mnemonic/load-loading-keys-screen)))))

(fx/defn on-install-applet-and-init-card-success
  {:events [:keycard.callback/on-install-applet-and-init-card-success
            :keycard.callback/on-init-card-success]}
  [{:keys [db] :as cofx} secrets]
  (let [secrets' (js->clj secrets :keywordize-keys true)]
    (fx/merge cofx
              {:keycard/get-application-info nil
               :db                              (-> db
                                                    (assoc-in [:keycard :card-state] :init)
                                                    (assoc-in [:keycard :setup-step] :secret-keys)
                                                    (update-in [:keycard :secrets] merge secrets'))}
              (common/hide-connection-sheet)
              (common/listen-to-hardware-back-button)
              (navigation/navigate-replace :keycard-onboarding-puk-code nil))))

(fx/defn on-install-applet-and-init-card-error
  {:events [:keycard.callback/on-install-applet-and-init-card-error
            :keycard.callback/on-init-card-error]}
  [{:keys [db] :as cofx} {:keys [code error]}]
  (log/debug "[keycard] install applet and init card error: " error)
  (fx/merge cofx
            {:db (assoc-in db [:keycard :setup-error] error)}
            (common/set-on-card-connected :keycard/load-preparing-screen)
            (common/process-error code error)))

(fx/defn generate-and-load-key
  {:events [:keycard/generate-and-load-key]}
  [{:keys [db] :as cofx}]
  (let [{:keys [pairing pin]}
        (get-in db [:keycard :secrets])

        {:keys [selected-id multiaccounts]}
        (:intro-wizard db)

        multiaccount      (or (->> multiaccounts
                                   (filter #(= (:id %) selected-id))
                                   first)
                              (assoc (get-in db [:intro-wizard :root-key])
                                     :derived
                                     (get-in db [:intro-wizard :derived])))
        recovery-mnemonic (get-in db [:intro-wizard :passphrase])
        mnemonic          (or (:mnemonic multiaccount)
                              recovery-mnemonic)
        pin'              (or pin (common/vector->string (get-in db [:keycard :pin :current])))]
    (fx/merge cofx
              {:keycard/generate-and-load-key
               {:mnemonic     mnemonic
                :pairing      pairing
                :pin          pin'}})))

(fx/defn begin-setup-pressed
  {:events [:keycard.onboarding.intro.ui/begin-setup-pressed]}
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
     :handler           (common/get-application-info nil :keycard/check-card-state)})))

(fx/defn cancel-confirm
  {:events [::cancel-confirm]}
  [cofx]
  (fx/merge cofx
            (navigation/navigate-back)
            (common/cancel-sheet-confirm)))

(fx/defn cancel-pressed
  {:events [::cancel-pressed]}
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/keycard-cancel-setup-title)
                          :content             (i18n/label :t/keycard-cancel-setup-text)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/no)
                          :on-accept           #(re-frame/dispatch [::cancel-confirm])
                          :on-cancel           #()}})
