(ns legacy.status-im.keycard.core
  (:require
    legacy.status-im.keycard.backup-key
    [legacy.status-im.keycard.card :as card]
    [legacy.status-im.keycard.change-pin :as change-pin]
    [legacy.status-im.keycard.common :as common]
    legacy.status-im.keycard.delete-key
    legacy.status-im.keycard.export-key
    [legacy.status-im.keycard.login :as login]
    [legacy.status-im.keycard.mnemonic :as mnemonic]
    [legacy.status-im.keycard.onboarding :as onboarding]
    [legacy.status-im.keycard.recovery :as recovery]
    [legacy.status-im.keycard.sign :as sign]
    legacy.status-im.keycard.unpair
    [legacy.status-im.keycard.wallet :as wallet]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [re-frame.db]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/defn show-keycard-has-multiaccount-alert
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db                              (assoc-in db [:keycard :setup-step] nil)
             :effects.utils/show-confirmation {:title               nil
                                               :content             (i18n/label
                                                                     :t/keycard-has-multiaccount-on-it)
                                               :cancel-button-text  ""
                                               :confirm-button-text :t/okay}}))

(rf/defn load-pin-screen
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :setup-step] :pin)
                     (assoc-in [:keycard :pin]
                               {:enter-step   :original
                                :original     []
                                :confirmation []}))}
            (navigation/navigate-to :keycard-onboarding-pin nil)))

(rf/defn load-recovery-pin-screen
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :pin]
                               {:enter-step          :import-multiaccount
                                :import-multiaccount []
                                :current             []}))}
            (common/listen-to-hardware-back-button)
            (navigation/navigate-replace :keycard-recovery-pin nil)))

(rf/defn load-pairing
  [{:keys [db]}]
  (let [instance-uid (get-in db [:keycard :application-info :instance-uid])
        pairing-data (or (get-in db [:keycard :pairings (keyword instance-uid)])
                         (get-in db [:keycard :pairings instance-uid]))]
    {:db (update-in db [:keycard :secrets] merge pairing-data)}))

(rf/defn proceed-setup-with-initialized-card
  [{:keys [db] :as cofx} flow instance-uid paired?]
  (log/debug "[keycard] proceed-setup-with-initialized-card"
             "instance-uid"
             instance-uid)
  (if (= flow :import)
    (navigation/navigate-to cofx :keycard-recovery-no-key nil)
    (if paired?
      (rf/merge cofx
                (common/listen-to-hardware-back-button)
                (when (= flow :create)
                  (mnemonic/set-mnemonic))
                (when (= flow :recovery)
                  (onboarding/proceed-with-generating-key)))
      (if (get-in db [:keycard :secrets :password])
        (onboarding/load-pairing-screen cofx)
        (recovery/load-pair-screen cofx)))))

(rf/defn navigate-to-keycard-settings
  {:events [:profile.ui/keycard-settings-button-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :pin :on-verified] nil)
                     (assoc-in [:keycard :setup-step] nil))}
            (common/clear-on-card-connected)
            (navigation/navigate-to :keycard-settings nil)))

(rf/defn password-option-pressed
  {:eevents [:keycard.ui/password-option-pressed]}
  [{:keys [db]}]
  (when (= (get-in db [:keycard :flow]) :create)
    #())) ;;TODO with v1 flow

(rf/defn settings-screen-did-load
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (-> db
                     (assoc-in [:keycard :pin :on-verified] nil)
                     (assoc-in [:keycard :setup-step] nil))}
            (common/clear-on-card-connected)))

(defn reset-card-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db [:keycard :reset-card :disabled?] false)})

(defn enter-pin-screen-did-load
  [{:keys [db]}]
  (let [enter-step (get-in db [:keycard :pin :enter-step])]
    {:db (assoc-in db [:keycard :pin enter-step] [])}))

(defn login-pin-screen-did-load
  [{:keys [db]}]
  (let [enter-step (get-in db [:keycard :pin :enter-step])]
    {:db (-> db
             (assoc-in [:keycard :pin enter-step] [])
             (dissoc :intro-wizard :recovered-account?))}))

(defn multiaccounts-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db [:keycard :setup-step] nil)})

(defn authentication-method-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db [:keycard :setup-step] nil)})

(rf/defn set-nfc-supported
  {:events [:keycard.callback/check-nfc-support-success]}
  [_ supported?]
  {:keycard/set-nfc-supported supported?})

;; TODO: Should be listener and replace the view in bottom sheet to avoid this
(rf/defn on-check-nfc-enabled-success
  {:events [:keycard.callback/check-nfc-enabled-success]}
  [{:keys [db]} nfc-enabled?]
  (log/debug "[keycard] check-nfc-enabled-success"
             "nfc-enabled?"
             nfc-enabled?)
  {:db (assoc-in db [:keycard :nfc-enabled?] nfc-enabled?)})

(defn- proceed-to-pin-confirmation
  [fx]
  (assoc-in fx [:db :keycard :pin :enter-step] :confirmation))

(defn- proceed-to-change-puk-confirmation
  [fx]
  (assoc-in fx [:db :keycard :pin :enter-step] :puk-confirmation))

(defn- proceed-to-pin-reset-confirmation
  [fx]
  (-> fx
      (update-in [:db :keycard :pin] dissoc :reset-confirmation)
      (assoc-in [:db :keycard :pin :enter-step] :reset-confirmation)))

(defn- proceed-to-puk-confirmation
  [fx]
  (assoc-in fx [:db :keycard :pin :enter-step] :puk))

(rf/defn on-unblock-pin-success
  {:events [:keycard.callback/on-unblock-pin-success]}
  [{:keys [db] :as cofx}]
  (let [reset-pin (get-in db [:keycard :pin :reset])]
    (rf/merge cofx
              {:db
               (-> db
                   (update-in [:keycard :application-info]
                              assoc
                              :puk-retry-counter 5
                              :pin-retry-counter 3)
                   (update-in [:keycard :pin]
                              assoc
                              :status       :after-unblocking
                              :enter-step   :login
                              :login        reset-pin
                              :confirmation []
                              :puk          []
                              :puk-restore? true
                              :error-label  nil))}
              (common/hide-connection-sheet)
              (common/clear-on-card-connected)
              (common/clear-on-card-read))))

(rf/defn on-unblock-pin-error
  {:events [:keycard.callback/on-unblock-pin-error]}
  [{:keys [db] :as cofx} error]
  (let [tag-was-lost? (common/tag-lost? (:error error))
        puk-retries   (common/pin-retries (:error error))]
    (log/debug "[keycard] unblock pin error" error)
    (when-not tag-was-lost?
      (rf/merge cofx
                {:db
                 (-> db
                     (assoc-in [:keycard :application-info :puk-retry-counter] puk-retries)
                     (update-in [:keycard :pin]
                                merge
                                {:status      (if (zero? puk-retries) :blocked-card :error)
                                 :error-label :t/puk-mismatch
                                 :enter-step  :puk
                                 :puk         []}))}

                (common/hide-connection-sheet)))))

(rf/defn clear-on-verify-handlers
  [{:keys [db]}]
  {:db (update-in db
                  [:keycard :pin]
                  dissoc
                  :on-verified-failure
                  :on-verified)})

(rf/defn on-verify-pin-success
  {:events [:keycard.callback/on-verify-pin-success]}
  [{:keys [db] :as cofx}]
  (let [on-verified (get-in db [:keycard :pin :on-verified])]
    (log/debug "[hardwaller] success pin verification. on-verified" on-verified)
    (rf/merge cofx
              {:db (update-in db
                              [:keycard :pin]
                              merge
                              {:status      nil
                               :error-label nil})}
              (common/clear-on-card-connected)
              (common/clear-on-card-read)
              ;; TODO(Ferossgp): Each pin input should handle this event on it's own,
              ;; now for simplicity do not hide bottom sheet when generating key
              ;; and exporting key but should be refactored.
              (when-not (contains? #{:keycard/generate-and-load-key
                                     :wallet-legacy.accounts/generate-new-keycard-account
                                     :keycard/remove-key-with-unpair
                                     :keycard/unpair-and-delete}
                                   on-verified)
                (common/hide-connection-sheet))
              (when-not (contains? #{:keycard/unpair
                                     :keycard/generate-and-load-key
                                     :keycard/remove-key-with-unpair
                                     :keycard/unpair-and-delete
                                     :wallet-legacy.accounts/generate-new-keycard-account}
                                   on-verified)
                (common/get-application-info nil))
              (when on-verified
                (common/dispatch-event on-verified))
              (clear-on-verify-handlers))))

(rf/defn on-verify-pin-error
  {:events [:keycard.callback/on-verify-pin-error]}
  [{:keys [db] :as cofx} error]
  (let [tag-was-lost?       (common/tag-lost? (:error error))
        setup?              (boolean (get-in db [:keycard :setup-step]))
        on-verified-failure (get-in db [:keycard :pin :on-verified-failure])
        exporting?          (get-in db [:keycard :on-export-success])
        pin-retries         (common/pin-retries (:error error))]
    (log/debug "[keycard] verify pin error" error)
    (when-not tag-was-lost?
      (if-not (nil? pin-retries)
        (rf/merge cofx
                  {:db (-> db
                           (assoc-in [:keycard :application-info :pin-retry-counter] pin-retries)
                           (update-in [:keycard :pin]
                                      assoc
                                      :status       :error
                                      :enter-step   :current
                                      :puk          []
                                      :current      []
                                      :original     []
                                      :confirmation []
                                      :sign         []
                                      :error-label  :t/pin-mismatch))}
                  (common/hide-connection-sheet)
                  (when (and (not setup?)
                             (not on-verified-failure))
                    (when exporting?
                      (navigation/navigate-back)))
                  ;(navigation/navigate-to :enter-pin-settings nil)))
                  (when (zero? pin-retries) (common/frozen-keycard-popup))
                  (when on-verified-failure
                    (fn [_]
                      {:utils/dispatch-later
                       [{:dispatch [on-verified-failure]
                         :ms       200}]}))
                  #_(clear-on-verify-handlers))

        (rf/merge cofx
                  (common/hide-connection-sheet)
                  (common/show-wrong-keycard-alert)
                  (clear-on-verify-handlers))))))

(rf/defn unblock-pin
  {:events [:keycard/unblock-pin]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :keycard/unblock-pin
    :handler
    (fn [{:keys [db]}]
      (let [puk     (common/vector->string (get-in db [:keycard :pin :puk]))
            pin     (common/vector->string (get-in db [:keycard :pin :reset]))
            key-uid (get-in db [:keycard :application-info :key-uid])]
        {:db (assoc-in db [:keycard :pin :status] :verifying)
         :keycard/unblock-pin
         {:puk     puk
          :new-pin pin}}))}))

(def pin-code-length 6)
(def puk-code-length 12)

(rf/defn handle-pin-input
  [{:keys [db]} enter-step]
  (let [numbers-entered (count (get-in db [:keycard :pin enter-step]))]
    (when (or (= numbers-entered pin-code-length)
              (= numbers-entered puk-code-length))
      {:dispatch [:keycard/process-pin-input]})))

(rf/defn update-pin
  {:events [:keycard.ui/pin-numpad-button-pressed]}
  [{:keys [db] :as cofx} number enter-step]
  (log/debug "update-pin" enter-step)
  (let [numbers-entered (count (get-in db [:keycard :pin enter-step]))
        need-update?    (if (or (= enter-step :puk)
                                (= enter-step :puk-original)
                                (= enter-step :puk-confirmation))
                          (< numbers-entered puk-code-length)
                          (< numbers-entered pin-code-length))]
    (rf/merge cofx
              {:db (cond-> (-> db
                               (assoc-in [:keycard :pin :enter-step] enter-step)
                               (assoc-in [:keycard :pin :status] nil))
                     need-update? (update-in [:keycard :pin enter-step] (fnil conj []) number))}
              (when need-update?
                (handle-pin-input enter-step)))))

(defn- pin-enter-error
  [fx error-label]
  (update-in fx
             [:db :keycard :pin]
             merge
             {:status       :error
              :error-label  error-label
              :enter-step   :original
              :original     []
              :confirmation []}))

(defn- puk-enter-error
  [fx error-label]
  (update-in fx
             [:db :keycard :pin]
             merge
             {:status           :error
              :error-label      error-label
              :enter-step       :puk-original
              :puk-original     []
              :puk-confirmation []}))

(defn- pin-reset-error
  [fx error-label]
  (update-in fx
             [:db :keycard :pin]
             merge
             {:status             :error
              :error-label        error-label
              :enter-step         :reset
              :reset              []
              :reset-confirmation []}))

; PIN enter steps:
; login - PIN is used to login
; sign - PIN for transaction sign
; current - current PIN to perform actions which require PIN auth
; original - new PIN when user changes it or creates new one
; confirmation - confirmation for new PIN
(rf/defn process-pin-input
  {:events [:keycard/process-pin-input]}
  [{:keys [db]}]
  (let [enter-step      (get-in db [:keycard :pin :enter-step])
        pin             (get-in db [:keycard :pin enter-step])
        numbers-entered (count pin)]
    (log/debug "[keycard] process-pin-input"
               "enter-step"      enter-step
               "numbers-entered" numbers-entered)
    (cond-> {:db (assoc-in db [:keycard :pin :status] nil)}

      (and (= enter-step :login)
           (= 6 numbers-entered))
      (login/proceed-to-login)

      (and (= enter-step :original)
           (= pin-code-length numbers-entered))
      (proceed-to-pin-confirmation)

      (and (= enter-step :original)
           (= pin-code-length numbers-entered)
           (= common/default-pin (common/vector->string pin)))
      (pin-enter-error :t/cannot-use-default-pin)

      (and (= enter-step :import-multiaccount)
           (= pin-code-length numbers-entered))
      (recovery/load-recovering-key-screen)

      (and (= enter-step :current)
           (= pin-code-length numbers-entered))
      (common/verify-pin {:pin-step :current})

      (and (= enter-step :export-key)
           (= pin-code-length numbers-entered))
      (wallet/verify-pin-with-delay)

      (and (= enter-step :sign)
           (= pin-code-length numbers-entered))
      (sign/prepare-to-sign)

      (and (= enter-step :puk)
           (= puk-code-length numbers-entered))
      (unblock-pin)

      (and (= enter-step :confirmation)
           (= (get-in db [:keycard :pin :original])
              (get-in db [:keycard :pin :confirmation])))
      (change-pin/change-pin)

      (and (= enter-step :confirmation)
           (= pin-code-length numbers-entered)
           (not= (get-in db [:keycard :pin :original])
                 (get-in db [:keycard :pin :confirmation])))
      (pin-enter-error :t/pin-mismatch)

      (and (= enter-step :puk-original)
           (= puk-code-length numbers-entered))
      (proceed-to-change-puk-confirmation)

      (and (= enter-step :puk-confirmation)
           (= (get-in db [:keycard :pin :puk-original])
              (get-in db [:keycard :pin :puk-confirmation])))
      (change-pin/change-puk)

      (and (= enter-step :puk-confirmation)
           (= puk-code-length numbers-entered)
           (not= (get-in db [:keycard :pin :puk-original])
                 (get-in db [:keycard :pin :puk-confirmation])))
      (puk-enter-error :t/puk-mismatch)

      (= enter-step :reset)
      (proceed-to-pin-reset-confirmation)

      (and (= enter-step :reset-confirmation)
           (= (get-in db [:keycard :pin :reset])
              (get-in db [:keycard :pin :reset-confirmation])))
      (proceed-to-puk-confirmation)

      (and (= enter-step :reset-confirmation)
           (= pin-code-length numbers-entered)
           (not= (get-in db [:keycard :pin :reset])
                 (get-in db [:keycard :pin :reset-confirmation])))
      (pin-reset-error :t/pin-mismatch))))

(rf/defn set-multiaccount-pairing
  [cofx _ pairing paired-on]
  (rf/merge cofx
            (multiaccounts.update/multiaccount-update
             :keycard-pairing
             pairing
             {})
            (multiaccounts.update/multiaccount-update
             :keycard-paired-on
             paired-on
             {})))

(rf/defn on-retrieve-pairings-success
  {:events [:keycard.callback/on-retrieve-pairings-success]}
  [{:keys [db]} pairings]
  (card/set-pairings pairings)
  {:db (assoc-in db [:keycard :pairings] pairings)})

;; When pairing to device has completed, we need to persist pairing data to
;; local storage. That's needed to ensure that during keycard setup
;; keycard won't run out of pairings slots, ie. we don't pair the same
;; card to the same device more than one time. Also, this allows the user to proceed
;; with setup and skip the pairing step if the pairing was already done during a previous
;; unfinished setup.

(rf/defn on-pair-success
  {:events [:keycard.callback/on-pair-success]}
  [{:keys [db] :as cofx} pairing]
  (let [setup-step   (get-in db [:keycard :setup-step])
        flow         (get-in db [:keycard :flow])
        instance-uid (get-in db [:keycard :application-info :instance-uid])
        multiaccount (common/find-multiaccount-by-keycard-instance-uid db instance-uid)
        paired-on    (datetime/timestamp)
        pairings     (-> (get-in db [:keycard :pairings])
                         (dissoc (keyword instance-uid))
                         (assoc instance-uid {:pairing pairing :paired-on paired-on}))
        next-step    (if (= setup-step :pair)
                       :begin
                       :card-ready)]
    (rf/merge cofx
              {:keycard/persist-pairings pairings
               :db                       (-> db
                                             (assoc-in [:keycard :pairings] pairings)
                                             (assoc-in [:keycard :application-info :paired?] true)
                                             (assoc-in [:keycard :setup-step] next-step)
                                             (assoc-in [:keycard :secrets :pairing] pairing)
                                             (assoc-in [:keycard :secrets :paired-on] paired-on))}
              (when-not (and (= flow :recovery) (= next-step :card-ready))
                (common/hide-connection-sheet))
              (when multiaccount
                (set-multiaccount-pairing multiaccount pairing paired-on))
              (when (= flow :login)
                (navigation/navigate-to :multiaccounts nil))
              (when (= flow :recovery)
                (onboarding/proceed-with-generating-key))
              (when (= flow :import)
                (load-recovery-pin-screen))
              ;; TODO: If card is already initialized need to confirm pin only then go to mnenmonic
              ;; https://github.com/status-im/status-mobile/issues/9451
              (when (= flow :create)
                (mnemonic/set-mnemonic)))))

(rf/defn on-pair-error
  {:events [:keycard.callback/on-pair-error]}
  [{:keys [db] :as cofx} {:keys [error code]}]
  (log/debug "[keycard] pair error: " error)
  (let [setup-step    (get-in db [:keycard :setup-step])
        tag-was-lost? (common/tag-lost? error)
        flow          (get-in db [:keycard :flow])]
    (log/debug "[keycard] on-pair-error" setup-step "flow:" flow)
    (when-not tag-was-lost?
      (rf/merge cofx
                {:db (assoc-in db [:keycard :setup-error] (i18n/label :t/invalid-pairing-password))}
                (common/set-on-card-connected (if (= setup-step :pairing)
                                                :keycard/load-pairing-screen
                                                :keycard/pair))
                (common/hide-connection-sheet)
                (when (= flow :import)
                  (navigation/navigate-to :keycard-recovery-pair nil))
                (when (not= setup-step :enter-pair-code)
                  (common/process-error code error))))))

(rf/defn set-setup-step
  [{:keys [db]} card-state]
  {:db (assoc-in db
        [:keycard :setup-step]
        (case card-state
          :not-paired       :pair
          :no-pairing-slots :no-slots
          :init             :card-ready
          :profile/profile  :import-multiaccount
          :begin))})

(rf/defn show-no-keycard-applet-alert
  [_]
  {:effects.utils/show-confirmation {:title               (i18n/label :t/no-keycard-applet-on-card)
                                     :content             (i18n/label
                                                           :t/keycard-applet-install-instructions)
                                     :cancel-button-text  ""
                                     :confirm-button-text :t/okay}})

;; NOTE: Maybe replaced by multiple events based on on flow to make it easier to maintain.
;; Because there are many execution paths it is harder to follow all possible states.
(rf/defn check-card-state
  {:events [:keycard/check-card-state]}
  [{:keys [db] :as cofx}]
  (let [app-info                               (get-in db [:keycard :application-info])
        flow                                   (get-in db [:keycard :flow])
        {:keys [instance-uid key-uid paired?]} app-info
        card-state                             (common/get-card-state app-info)]
    (log/debug "[keycard] check-card-state"
               "card-state" card-state
               "flow"       flow)
    (rf/merge cofx
              {:db (assoc-in db [:keycard :card-state] card-state)}
              (set-setup-step card-state)

              (when paired?
                (load-pairing))

              (if (and flow
                       (= card-state :init))
                (proceed-setup-with-initialized-card flow instance-uid paired?)
                (common/hide-connection-sheet))

              (when (= card-state :pre-init)
                (if (= flow :import)
                  (navigation/navigate-to :keycard-recovery-no-key nil)
                  (fn [cofx]
                    (rf/merge
                     cofx
                     (common/clear-on-card-read)
                     (load-pin-screen)))))

              (when (and (= card-state :profile/profile)
                         (= flow :import))
                (if (common/find-multiaccount-by-key-uid db key-uid)
                  ;; reimplement
                  ;;(multiaccounts.recover/show-existing-multiaccount-alert key-uid)
                  (if paired?
                    (load-recovery-pin-screen)
                    (recovery/load-pair-screen))))

              (when (= card-state :blank)
                (if (= flow :import)
                  (navigation/navigate-to :keycard-recovery-no-key nil)
                  (show-no-keycard-applet-alert)))

              (when (and (= card-state :profile/profile)
                         (#{:create :recovery} flow))
                (show-keycard-has-multiaccount-alert)))))

(rf/defn on-card-connected
  {:events [:keycard.callback/on-card-connected]}
  [{:keys [db]} _]
  (log/debug "[keycard] card globally connected")
  {:db (assoc-in db [:keycard :card-connected?] true)})

(rf/defn on-card-disconnected
  {:events [:keycard.callback/on-card-disconnected]}
  [{:keys [db]} _]
  (log/debug "[keycard] card disconnected")
  {:db (assoc-in db [:keycard :card-connected?] false)})

(rf/defn on-nfc-user-cancelled
  {:events [:keycard.callback/on-nfc-user-cancelled]}
  [{:keys [db]} _]
  (log/debug "[keycard] nfc user cancelled")
  {:dispatch [:signing.ui/cancel-is-pressed]})

(rf/defn on-nfc-timeout
  {:events [:keycard.callback/on-nfc-timeout]}
  [{:keys [db]} _]
  (log/debug "[keycard] nfc timeout")
  {:db             (-> db
                       (assoc-in [:keycard :nfc-running?] false)
                       (assoc-in [:keycard :card-connected?] false))
   :dispatch-later [{:ms 500 :dispatch [:keycard.ui/start-nfc]}]})

(rf/defn on-register-card-events
  {:events [:keycard.callback/on-register-card-events]}
  [{:keys [db]} listeners]
  {:db (update-in db [:keycard :listeners] merge listeners)})

(rf/defn ui-recovery-phrase-cancel-pressed
  {:events [:keycard.ui/recovery-phrase-cancel-pressed]}
  [{:keys [db]}]
  {:db (assoc-in db [:keycard :setup-step] :recovery-phrase)})

(rf/defn ui-pin-numpad-delete-button-pressed
  {:events [:keycard.ui/pin-numpad-delete-button-pressed]}
  [{:keys [db]} step]
  (when-not (empty? (get-in db [:keycard :pin step]))
    {:db (update-in db [:keycard :pin step] pop)}))

(rf/defn start-nfc
  {:events [:keycard.ui/start-nfc]}
  [cofx]
  {:keycard/start-nfc nil})

(rf/defn stop-nfc
  {:events [:keycard.ui/stop-nfc]}
  [cofx]
  {:keycard/stop-nfc                      nil
   :keycard.callback/on-card-disconnected nil})

(rf/defn start-nfc-success
  {:events [:keycard.callback/start-nfc-success]}
  [{:keys [db]} _]
  (log/debug "[keycard] nfc started success")
  {:db (assoc-in db [:keycard :nfc-running?] true)})

(rf/defn start-nfc-failure
  {:events [:keycard.callback/start-nfc-failure]}
  [{:keys [db]} _]
  (log/debug "[keycard] nfc failed starting")) ;; leave current value on :nfc-running

(rf/defn stop-nfc-success
  {:events [:keycard.callback/stop-nfc-success]}
  [{:keys [db]} _]
  (log/debug "[keycard] nfc stopped success")
  (log/debug "[keycard] setting card-connected? and nfc-running? to false")
  {:db (-> db
           (assoc-in [:keycard :nfc-running?] false)
           (assoc-in [:keycard :card-connected?] false))})

(rf/defn stop-nfc-failure
  {:events [:keycard.callback/stop-nfc-failure]}
  [{:keys [db]} _]
  (log/debug "[keycard] nfc failed stopping")) ;; leave current value on :nfc-running

(rf/defn init
  {:events [:keycard/init]}
  [_]
  {:keycard/register-card-events nil
   :keycard/check-nfc-support    nil
   :keycard/check-nfc-enabled    nil
   :keycard/retrieve-pairings    nil})
