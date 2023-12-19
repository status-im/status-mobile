(ns legacy.status-im.keycard.onboarding
  (:require
    [clojure.string :as string]
    [legacy.status-im.keycard.common :as common]
    legacy.status-im.keycard.fx
    [legacy.status-im.keycard.mnemonic :as mnemonic]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.utils.utils :as utils]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/defn begin-setup-button-pressed
  {:keys [:keycard.ui/begin-setup-button-pressed]}
  [{:keys [db]}]
  {:db (-> db
           (assoc-in [:keycard :setup-step] :pin)
           (assoc-in [:keycard :pin :enter-step] :original)
           (assoc-in [:keycard :pin :original] [])
           (assoc-in [:keycard :pin :confirmation] []))})

(rf/defn start-installation
  [{:keys [db] :as cofx}]
  (let [card-state (get-in db [:keycard :card-state])
        pin        (common/vector->string (get-in db [:keycard :pin :original]))]
    (log/debug "start-installation: card-state" card-state)
    (case card-state

      :pre-init
      {:keycard/init-card pin}

      (do
        (log/debug (str "Cannot start keycard installation from state: " card-state))
        (rf/merge cofx
                  {:effects.utils/show-popup {:title   (i18n/label :t/error)
                                              :content (i18n/label :t/something-went-wrong)}}
                  (navigation/navigate-to :keycard-authentication-method nil))))))

(rf/defn load-preparing-screen
  {:events [:keycard/load-preparing-screen]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:sheet-options     {:on-cancel [::cancel-pressed]}
    :on-card-connected :keycard/load-preparing-screen
    :handler           start-installation}))

(rf/defn load-pairing-screen
  {:events [:keycard/load-pairing-screen
            :keycard.onboarding.puk-code.ui/confirm-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (assoc-in db [:keycard :setup-step] :pairing)}
   (common/show-connection-sheet
    {:sheet-options     {:on-cancel [::cancel-pressed]}
     :on-card-connected :keycard/load-pairing-screen
     :handler           (common/dispatch-event :keycard/pair)})))

(rf/defn puk-code-next-pressed
  {:events [:keycard.onboarding.puk-code.ui/next-pressed]}
  [_]
  {:ui/show-confirmation {:title               (i18n/label :t/secret-keys-confirmation-title)
                          :content             (i18n/label :t/secret-keys-confirmation-text)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text  (i18n/label :t/cancel)
                          :on-accept           #(re-frame/dispatch
                                                 [:keycard.onboarding.puk-code.ui/confirm-pressed])
                          :on-cancel           #()}})

(rf/defn load-finishing-screen
  {:events [:keycard.onboarding.recovery-phrase-confirm-word2.ui/next-pressed
            :keycard/load-finishing-screen]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (assoc-in db [:keycard :setup-step] :loading-keys)}
   (common/show-connection-sheet
    {:sheet-options     {:on-cancel [::cancel-pressed]}
     :on-card-connected :keycard/load-finishing-screen
     :handler           (common/dispatch-event :keycard/generate-and-load-key)})))

(rf/defn recovery-phrase-learn-more-pressed
  {:events [:keycard.onboarding.recovery-phrase.ui/learn-more-pressed]}
  [_]
  (.openURL ^js react/linking constants/keycard-integration-link))

(rf/defn recovery-phrase-next-pressed
  {:events [:keycard.onboarding.recovery-phrase.ui/next-pressed
            :keycard.ui/recovery-phrase-next-button-pressed]}
  [_]
  {:ui/show-confirmation {:title (i18n/label :t/keycard-recovery-phrase-confirmation-title)
                          :content (i18n/label :t/keycard-recovery-phrase-confirmation-text)
                          :confirm-button-text (i18n/label :t/yes)
                          :cancel-button-text (i18n/label :t/cancel)
                          :on-accept #(re-frame/dispatch
                                       [:keycard.onboarding.recovery-phrase.ui/confirm-pressed])
                          :on-cancel #()}})

(rf/defn recovery-phrase-start-confirmation
  [{:keys [db] :as cofx}]
  (let [mnemonic      (get-in db [:keycard :secrets :mnemonic])
        [word1 word2] (shuffle (map-indexed vector (clojure.string/split mnemonic #" ")))
        word1         (zipmap [:idx :word] word1)
        word2         (zipmap [:idx :word] word2)]
    (rf/merge cofx
              {:db (-> db
                       (assoc-in [:keycard :setup-step] :recovery-phrase-confirm-word1)
                       (assoc-in [:keycard :recovery-phrase :step] :word1)
                       (assoc-in [:keycard :recovery-phrase :confirm-error] nil)
                       (assoc-in [:keycard :recovery-phrase :input-word] nil)
                       (assoc-in [:keycard :recovery-phrase :word1] word1)
                       (assoc-in [:keycard :recovery-phrase :word2] word2))}
              (common/remove-listener-to-hardware-back-button))))

(rf/defn recovery-phrase-confirm-pressed
  {:events [:keycard.onboarding.recovery-phrase.ui/confirm-pressed]}
  [cofx]
  (rf/merge cofx
            (recovery-phrase-start-confirmation)
            (navigation/navigate-to :keycard-onboarding-recovery-phrase-confirm-word1 nil)))

(rf/defn recovery-phrase-next-word
  [{:keys [db]}]
  {:db (-> db
           (assoc-in [:keycard :recovery-phrase :step] :word2)
           (assoc-in [:keycard :recovery-phrase :confirm-error] nil)
           (assoc-in [:keycard :recovery-phrase :input-word] nil)
           (assoc-in [:keycard :setup-step] :recovery-phrase-confirm-word2))})

(rf/defn proceed-with-generating-key
  [{:keys [db] :as cofx}]
  (let [pin (get-in db
                    [:keycard :secrets :pin]
                    (common/vector->string (get-in db [:keycard :pin :current])))]
    (if (empty? pin)
      (rf/merge cofx
                {:db (-> db
                         (assoc-in [:keycard :pin]
                                   {:enter-step  :current
                                    :on-verified :keycard/generate-and-load-key
                                    :current     []})
                         (assoc-in [:keycard :setup-step] :loading-keys))}
                (navigation/navigate-to :keycard-onboarding-pin nil))
      (load-finishing-screen cofx))))

(rf/defn recovery-phrase-confirm-word-next-pressed
  {:events [:keycard.onboarding.recovery-phrase-confirm-word.ui/next-pressed
            :keycard.onboarding.recovery-phrase-confirm-word.ui/input-submitted]}
  [{:keys [db] :as cofx}]
  (let [step           (get-in db [:keycard :recovery-phrase :step])
        input-word     (get-in db [:keycard :recovery-phrase :input-word])
        {:keys [word]} (get-in db [:keycard :recovery-phrase step])]
    (if (= word input-word)
      (if (= (:view-id db) :keycard-onboarding-recovery-phrase-confirm-word1)
        (rf/merge cofx
                  (recovery-phrase-next-word)
                  (navigation/navigate-replace :keycard-onboarding-recovery-phrase-confirm-word2 nil))
        (proceed-with-generating-key cofx))
      {:db (assoc-in db [:keycard :recovery-phrase :confirm-error] (i18n/label :t/wrong-word))})))

(rf/defn recovery-phrase-confirm-word-input-changed
  {:events [:keycard.onboarding.recovery-phrase-confirm-word.ui/input-changed]}
  [{:keys [db]} input]
  {:db (assoc-in db [:keycard :recovery-phrase :input-word] input)})

(rf/defn pair-code-input-changed
  {:events [:keycard.onboarding.pair.ui/input-changed]}
  [{:keys [db]} input]
  {:db (assoc-in db [:keycard :secrets :password] input)})

(rf/defn keycard-option-pressed
  {:events [:onboarding.ui/keycard-option-pressed]}
  [{:keys [db] :as cofx}]
  (let [flow (get-in db [:keycard :flow])]
    (rf/merge cofx
              {:keycard/check-nfc-enabled nil}
              (if (= flow :import)
                (navigation/navigate-to :keycard-recovery-intro nil)
                (do
                  (common/listen-to-hardware-back-button)
                  (navigation/navigate-to :keycard-onboarding-intro nil))))))

(rf/defn start-onboarding-flow
  {:events [:keycard/start-onboarding-flow]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db                        (assoc-in db [:keycard :flow] :create)
             :keycard/check-nfc-enabled nil}
            (common/listen-to-hardware-back-button)
            (navigation/navigate-to :keycard-onboarding-intro nil)))

(rf/defn open-nfc-settings-pressed
  {:events [:keycard.onboarding.nfc-on/open-nfc-settings-pressed]}
  [_]
  {:keycard/open-nfc-settings nil})

(defn- show-recover-confirmation
  []
  {:ui/show-confirmation {:title               (i18n/label :t/are-you-sure?)
                          :content             (i18n/label :t/are-you-sure-description)
                          :confirm-button-text (clojure.string/upper-case (i18n/label :t/yes))
                          :cancel-button-text  (i18n/label :t/see-it-again)
                          :on-accept           #(re-frame/dispatch
                                                 [:keycard.ui/recovery-phrase-confirm-pressed])
                          :on-cancel           #(re-frame/dispatch
                                                 [:keycard.ui/recovery-phrase-cancel-pressed])}})

(rf/defn recovery-phrase-confirm-word
  {:events [:keycard.ui/recovery-phrase-confirm-word-next-button-pressed]}
  [{:keys [db]}]
  (let [step           (get-in db [:keycard :recovery-phrase :step])
        input-word     (get-in db [:keycard :recovery-phrase :input-word])
        {:keys [word]} (get-in db [:keycard :recovery-phrase step])]
    (if (= word input-word)
      (if (= step :word1)
        (recovery-phrase-next-word db)
        (show-recover-confirmation))
      {:db (assoc-in db [:keycard :recovery-phrase :confirm-error] (i18n/label :t/wrong-word))})))

(rf/defn recovery-phrase-next-button-pressed
  [{:keys [db] :as cofx}]
  (if (= (get-in db [:keycard :flow]) :create)
    (recovery-phrase-start-confirmation cofx)
    (let [mnemonic (get-in db [:multiaccounts/recover :passphrase])]
      (rf/merge cofx
                {:db (assoc-in db [:keycard :secrets :mnemonic] mnemonic)}
                (mnemonic/load-loading-keys-screen)))))

(rf/defn on-install-applet-and-init-card-success
  {:events [:keycard.callback/on-install-applet-and-init-card-success
            :keycard.callback/on-init-card-success]}
  [{:keys [db] :as cofx} secrets]
  (let [secrets' (js->clj secrets :keywordize-keys true)]
    (rf/merge cofx
              {:db (-> db
                       (assoc-in [:keycard :card-state] :init)
                       (assoc-in [:keycard :setup-step] :secret-keys)
                       (update-in [:keycard :secrets] merge secrets'))}
              (common/show-connection-sheet
               {:on-card-connected :keycard/get-application-info
                :on-card-read      :keycard/check-card-state
                :handler           (common/get-application-info :keycard/check-card-state)}))))

(rf/defn on-install-applet-and-init-card-error
  {:events [:keycard.callback/on-install-applet-and-init-card-error
            :keycard.callback/on-init-card-error]}
  [{:keys [db] :as cofx} {:keys [code error]}]
  (log/debug "[keycard] install applet and init card error: " error)
  (rf/merge cofx
            {:db (assoc-in db [:keycard :setup-error] error)}
            (common/set-on-card-connected :keycard/load-preparing-screen)
            (common/process-error code error)))

(rf/defn generate-and-load-key
  {:events [:keycard/generate-and-load-key]}
  [{:keys [db] :as cofx}]
  (let [{:keys [pin]}
        (get-in db [:keycard :secrets])

        {:keys [selected-id multiaccounts]}
        (:intro-wizard db)

        multiaccount (or (->> multiaccounts
                              (filter #(= (:id %) selected-id))
                              first)
                         (assoc (get-in db [:intro-wizard :root-key])
                                :derived
                                (get-in db [:intro-wizard :derived])))
        recovery-mnemonic (get-in db [:intro-wizard :passphrase])
        mnemonic (or (:mnemonic multiaccount)
                     recovery-mnemonic)
        pin' (or pin
                 (common/vector->string (get-in db
                                                [:keycard :pin
                                                 :current])))]
    {:keycard/generate-and-load-key
     {:mnemonic             mnemonic
      :pin                  pin'
      :key-uid              (:key-uid multiaccount)
      :delete-multiaccount? (get-in db [:keycard :delete-account?])}}))

(rf/defn factory-reset-card-toggle
  {:events [:keycard.onboarding.intro.ui/factory-reset-card-toggle]}
  [{:keys [db] :as cofx} checked?]
  {:db (assoc-in db [:keycard :factory-reset-card?] checked?)})

(rf/defn begin-setup-pressed
  {:events [:keycard.onboarding.intro.ui/begin-setup-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (-> db
            (update :keycard
                    dissoc
                    :secrets
                    :card-state                      :multiaccount-wallet-address
                    :multiaccount-whisper-public-key :application-info)
            (assoc-in [:keycard :setup-step] :begin)
            (assoc-in [:keycard :pin :on-verified] nil))}
   (if (get-in db [:keycard :factory-reset-card?])
     (utils/show-confirmation {:title               (i18n/label :t/keycard-factory-reset-title)
                               :content             (i18n/label :t/keycard-factory-reset-text)
                               :confirm-button-text (i18n/label :t/yes)
                               :cancel-button-text  (i18n/label :t/no)
                               :on-accept           #(re-frame/dispatch [::factory-reset])
                               :on-cancel           #(re-frame/dispatch [::factory-reset-cancel])})
     (common/show-connection-sheet
      {:on-card-connected :keycard/get-application-info
       :on-card-read      :keycard/check-card-state
       :handler           (common/get-application-info :keycard/check-card-state)}))))

(rf/defn factory-reset
  {:events [::factory-reset]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :keycard/factory-reset
    :on-card-read      :keycard/check-card-state
    :handler           (common/factory-reset :keycard/check-card-state)}))

(rf/defn factory-reset-cancel
  {:events [::factory-reset-cancel]}
  [{:keys [db] :as cofx}]
  {:db (update db :keycard dissoc :factory-reset-card?)})

(rf/defn cancel-pressed
  {:events [::cancel-pressed]}
  [cofx]
  (rf/merge cofx
            (navigation/navigate-back)
            (common/cancel-sheet-confirm)))
