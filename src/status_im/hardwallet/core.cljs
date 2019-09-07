(ns status-im.hardwallet.core
  (:require [status-im.multiaccounts.create.core :as multiaccounts.create]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.datetime :as utils.datetime]
            [status-im.utils.fx :as fx]
            [status-im.hardwallet.login :as login]
            [status-im.hardwallet.sign :as sign]
            [status-im.hardwallet.change-pin :as change-pin]
            [status-im.hardwallet.mnemonic :as mnemonic]
            [status-im.hardwallet.recovery :as recovery]
            [status-im.hardwallet.onboarding :as onboarding]
            [status-im.hardwallet.common :as common]
            status-im.hardwallet.unpair
            status-im.hardwallet.export-key
            status-im.hardwallet.delete-key
            [status-im.hardwallet.wallet :as wallet]
            [taoensso.timbre :as log]
            status-im.hardwallet.fx
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]))

(fx/defn show-keycard-has-multiaccount-alert
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db                      (assoc-in db [:hardwallet :setup-step] nil)
             :utils/show-confirmation {:title               nil
                                       :content             (i18n/label :t/keycard-has-multiaccount-on-it)
                                       :cancel-button-text  ""
                                       :confirm-button-text :t/okay}}))

(fx/defn load-pin-screen
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:hardwallet :setup-step] :pin)
                     (assoc-in [:hardwallet :pin] {:enter-step   :original
                                                   :original     []
                                                   :confirmation []}))}
            (navigation/navigate-to-cofx :keycard-onboarding-pin nil)))

(fx/defn load-recovery-pin-screen
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:hardwallet :pin] {:enter-step          :import-multiaccount
                                                   :import-multiaccount []
                                                   :current             []}))}
            (common/listen-to-hardware-back-button)
            (navigation/navigate-replace-cofx :keycard-recovery-pin nil)))

(fx/defn proceed-setup-with-initialized-card
  [{:keys [db] :as cofx} flow instance-uid]
  (log/debug "[hardwallet] proceed-setup-with-initialized-card"
             "instance-uid" instance-uid)
  (if (= flow :import)
    (navigation/navigate-to-cofx cofx :keycard-recovery-no-key nil)
    (let [pairing-data (get-in db [:hardwallet :pairings instance-uid])]
      (if pairing-data
        (fx/merge cofx
                  {:db (update-in db [:hardwallet :secrets] merge pairing-data)}
                  (common/listen-to-hardware-back-button)
                  (when (= flow :create)
                    (mnemonic/set-mnemonic))
                  (when (= flow :recovery)
                    (onboarding/proceed-with-generating-key)))
        (recovery/load-pair-screen cofx)))))

(fx/defn navigate-to-keycard-settings
  {:events [:profile.ui/keycard-settings-button-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:hardwallet :pin :on-verified] nil)
                     (assoc-in [:hardwallet :setup-step] nil))}
            (common/clear-on-card-connected)
            (navigation/navigate-to-cofx :keycard-settings nil)))

(fx/defn password-option-pressed
  {:eevents [:hardwallet.ui/password-option-pressed]}
  [{:keys [db]}]
  (when (= (get-in db [:hardwallet :flow]) :create)
    #())) ;;TODO with v1 flow

(fx/defn settings-screen-did-load
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:hardwallet :pin :on-verified] nil)
                     (assoc-in [:hardwallet :setup-step] nil))}
            (common/clear-on-card-connected)))

(defn reset-card-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db [:hardwallet :reset-card :disabled?] false)})

(defn enter-pin-screen-did-load
  [{:keys [db]}]
  (let [enter-step (get-in db [:hardwallet :pin :enter-step])]
    {:db (assoc-in db [:hardwallet :pin enter-step] [])}))

(defn multiaccounts-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db [:hardwallet :setup-step] nil)})

(defn authentication-method-screen-did-load
  [{:keys [db]}]
  {:db (assoc-in db [:hardwallet :setup-step] nil)})

(fx/defn set-nfc-supported
  {:events [:hardwallet.callback/check-nfc-support-success]}
  [_ supported?]
  {:hardwallet/set-nfc-supported supported?})

;; TODO: Should be listener and replace the view in bottom sheet to avoid this
(fx/defn on-check-nfc-enabled-success
  {:events [:hardwallet.callback/check-nfc-enabled-success]}
  [{:keys [db]} nfc-enabled?]
  (log/debug "[hardwallet] check-nfc-enabled-success"
             "nfc-enabled?" nfc-enabled?)
  {:db (assoc-in db [:hardwallet :nfc-enabled?] nfc-enabled?)})

(defn- proceed-to-pin-confirmation [fx]
  (assoc-in fx [:db :hardwallet :pin :enter-step] :confirmation))

(fx/defn on-unblock-pin-success
  {:events [:hardwallet.callback/on-unblock-pin-success]}
  [{:keys [db] :as cofx}]
  (let [pairing (common/get-pairing db)]
    (fx/merge cofx
              {:hardwallet/get-application-info {:pairing pairing}
               :db                              (-> db
                                                    (update-in [:hardwallet :pin] merge {:status       nil
                                                                                         :enter-step   :original
                                                                                         :current      [0 0 0 0 0 0]
                                                                                         :confirmation []
                                                                                         :puk          []
                                                                                         :puk-restore? true
                                                                                         :error-label  nil}))}
              (common/hide-connection-sheet)
              (navigation/navigate-to-cofx :enter-pin-settings nil))))

(fx/defn on-unblock-pin-error
  {:events [:hardwallet.callback/on-unblock-pin-error]}
  [{:keys [db] :as cofx} error]
  (let [pairing       (common/get-pairing db)
        tag-was-lost? (common/tag-lost? (:error error))]
    (log/debug "[hardwallet] unblock pin error" error)
    (when-not tag-was-lost?
      (fx/merge cofx
                {:hardwallet/get-application-info {:pairing pairing}
                 :db                              (update-in db [:hardwallet :pin] merge {:status      :error
                                                                                          :error-label :t/puk-mismatch
                                                                                          :enter-step  :puk
                                                                                          :puk         []})}
                (common/hide-connection-sheet)))))

(fx/defn clear-on-verify-handlers
  [{:keys [db]}]
  {:db (update-in db [:hardwallet :pin]
                  dissoc :on-verified-failure :on-verified)})

(fx/defn on-verify-pin-success
  {:events [:hardwallet.callback/on-verify-pin-success]}
  [{:keys [db] :as cofx}]
  (let [on-verified (get-in db [:hardwallet :pin :on-verified])
        pairing     (common/get-pairing db)]
    (log/debug "[hardwaller] success pin verification. on-verified" on-verified)
    (fx/merge cofx
              {:db (update-in db [:hardwallet :pin] merge {:status      nil
                                                           :error-label nil})}
              (common/clear-on-card-connected)
              (common/clear-on-card-read)
              ;; TODO(Ferossgp): Each pin input should handle this event on it's own,
              ;; now for simplicity do not hide bottom sheet when generating key
              ;; but should be refactored.
              (when-not (= on-verified :hardwallet/generate-and-load-key)
                (common/hide-connection-sheet))
              (when-not (contains? #{:hardwallet/unpair
                                     :hardwallet/generate-and-load-key
                                     :hardwallet/remove-key-with-unpair
                                     :hardwallet/unpair-and-delete
                                     :wallet.accounts/generate-new-keycard-account} on-verified)
                (common/get-application-info pairing nil))
              (when on-verified
                (common/dispatch-event on-verified))
              (clear-on-verify-handlers))))

(fx/defn on-verify-pin-error
  {:events [:hardwallet.callback/on-verify-pin-error]}
  [{:keys [db] :as cofx} error]
  (let [tag-was-lost?       (common/tag-lost? (:error error))
        setup?              (boolean (get-in db [:hardwallet :setup-step]))
        on-verified-failure (get-in db [:hardwallet :pin :on-verified-failure])
        exporting?          (get-in db [:hardwallet :on-export-success])]
    (log/debug "[hardwallet] verify pin error" error)
    (when-not tag-was-lost?
      (if (re-matches common/pin-mismatch-error (:error error))
        (fx/merge cofx
                  {:db (update-in db [:hardwallet :pin]
                                  merge
                                  {:status       :error
                                   :enter-step   :current
                                   :puk          []
                                   :current      []
                                   :original     []
                                   :confirmation []
                                   :sign         []
                                   :error-label  :t/pin-mismatch})}
                  (common/hide-connection-sheet)
                  (when (and (not setup?)
                             (not on-verified-failure))
                    (if exporting?
                      (navigation/navigate-back)
                      (navigation/navigate-to-cofx :enter-pin-settings nil)))
                  (common/get-application-info (common/get-pairing db) nil)
                  (when on-verified-failure
                    (fn [_] {:utils/dispatch-later
                             [{:dispatch [on-verified-failure]
                               :ms 200}]}))
                  (clear-on-verify-handlers))

        (fx/merge cofx
                  (common/hide-connection-sheet)
                  (common/show-wrong-keycard-alert true)
                  (clear-on-verify-handlers))))))

(fx/defn unblock-pin
  {:events [:hardwallet/unblock-pin]}
  [cofx]
  (common/show-connection-sheet
   cofx
   {:on-card-connected :hardwallet/unblock-pin
    :handler
    (fn [{:keys [db]}]
      (let [puk     (common/vector->string (get-in db [:hardwallet :pin :puk]))
            key-uid (get-in db [:hardwallet :application-info :key-uid])
            pairing (common/get-pairing db key-uid)]
        {:db (assoc-in db [:hardwallet :pin :status] :verifying)
         :hardwallet/unblock-pin
         {:puk     puk
          :new-pin common/default-pin
          :pairing pairing}}))}))

(def pin-code-length 6)
(def puk-code-length 12)

(fx/defn handle-pin-input
  [{:keys [db]} enter-step]
  (let [numbers-entered (count (get-in db [:hardwallet :pin enter-step]))]
    (when (or (= numbers-entered pin-code-length)
              (= numbers-entered puk-code-length))
      {:dispatch [:hardwallet/process-pin-input]})))

(fx/defn update-pin
  {:events [:hardwallet.ui/pin-numpad-button-pressed]}
  [{:keys [db] :as cofx} number enter-step]
  (let [numbers-entered (count (get-in db [:hardwallet :pin enter-step]))
        need-update? (if (= enter-step :puk)
                       (< numbers-entered puk-code-length)
                       (< numbers-entered pin-code-length))]
    (fx/merge cofx
              {:db (cond-> (assoc-in db [:hardwallet :pin :status] nil)
                     need-update? (update-in [:hardwallet :pin enter-step] (fnil conj []) number))}
              (when need-update?
                (handle-pin-input enter-step)))))

(defn- pin-enter-error [fx error-label]
  (update-in fx [:db :hardwallet :pin] merge {:status       :error
                                              :error-label  error-label
                                              :enter-step   :original
                                              :original     []
                                              :confirmation []}))

; PIN enter steps:
; login - PIN is used to login
; sign - PIN for transaction sign
; current - current PIN to perform actions which require PIN auth
; original - new PIN when user changes it or creates new one
; confirmation - confirmation for new PIN
(fx/defn process-pin-input
  {:events [:hardwallet/process-pin-input]}
  [{:keys [db]}]
  (let [enter-step (get-in db [:hardwallet :pin :enter-step])
        pin (get-in db [:hardwallet :pin enter-step])
        numbers-entered (count pin)]
    (log/debug "[hardwallet] process-pin-input"
               "enter-step" enter-step
               "numbers-entered" numbers-entered)
    (cond-> {:db (assoc-in db [:hardwallet :pin :status] nil)}

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
      (wallet/hide-pin-sheet)

      (and (= enter-step :sign)
           (= pin-code-length numbers-entered))
      (sign/prepare-to-sign)

      (and (= enter-step :puk)
           (= puk-code-length numbers-entered))
      (unblock-pin)

      (and (= enter-step :confirmation)
           (= (get-in db [:hardwallet :pin :original])
              (get-in db [:hardwallet :pin :confirmation])))
      (change-pin/change-pin)

      (and (= enter-step :confirmation)
           (= pin-code-length numbers-entered)
           (not= (get-in db [:hardwallet :pin :original])
                 (get-in db [:hardwallet :pin :confirmation])))
      (pin-enter-error :t/pin-mismatch))))

(fx/defn set-multiaccount-pairing
  [cofx _ pairing paired-on]
  (fx/merge cofx
            (multiaccounts.update/multiaccount-update
             :keycard-pairing pairing {})
            (multiaccounts.update/multiaccount-update
             :keycard-paired-on paired-on {})))

(fx/defn on-retrieve-pairings-success
  {:events [:hardwallet.callback/on-retrieve-pairings-success]}
  [{:keys [db]} pairings]
  {:db (assoc-in db [:hardwallet :pairings] pairings)})

;; When pairing to device has completed, we need to persist pairing data to
;; local storage. That's needed to ensure that during keycard setup
;; keycard won't run out of pairings slots, ie. we don't pair the same
;; card to the same device more than one time. Also, this allows the user to proceed
;; with setup and skip the pairing step if the pairing was already done during a previous
;; unfinished setup.

(fx/defn on-pair-success
  {:events [:hardwallet.callback/on-pair-success]}
  [{:keys [db] :as cofx} pairing]
  (let [setup-step   (get-in db [:hardwallet :setup-step])
        flow         (get-in db [:hardwallet :flow])
        instance-uid (get-in db [:hardwallet :application-info :instance-uid])
        multiaccount (common/find-multiaccount-by-keycard-instance-uid db instance-uid)
        paired-on    (utils.datetime/timestamp)
        pairings     (assoc (get-in db [:hardwallet :pairings]) instance-uid {:pairing   pairing
                                                                              :paired-on paired-on})
        next-step    (if (= setup-step :pair)
                       :begin
                       :card-ready)]
    (fx/merge cofx
              {:hardwallet/persist-pairings pairings
               :db                          (-> db
                                                (assoc-in [:hardwallet :pairings] pairings)
                                                (assoc-in [:hardwallet :application-info :paired?] true)
                                                (assoc-in [:hardwallet :setup-step] next-step)
                                                (assoc-in [:hardwallet :secrets :pairing] pairing)
                                                (assoc-in [:hardwallet :secrets :paired-on] paired-on))}
              (common/hide-connection-sheet)
              (when multiaccount
                (set-multiaccount-pairing multiaccount pairing paired-on))
              (when (= flow :login)
                (navigation/navigate-to-cofx :multiaccounts nil))
              (when (= flow :recovery)
                (onboarding/proceed-with-generating-key))
              (when (= flow :import)
                (load-recovery-pin-screen))
              ;; TODO: If card is already initialized need to confirm pin only then go to mnenmonic
              ;; https://github.com/status-im/status-react/issues/9451
              (when (= flow :create)
                (mnemonic/set-mnemonic)))))

(fx/defn on-pair-error
  {:events [:hardwallet.callback/on-pair-error]}
  [{:keys [db] :as cofx} {:keys [error code]}]
  (log/debug "[hardwallet] pair error: " error)
  (let [setup-step    (get-in db [:hardwallet :setup-step])
        tag-was-lost? (common/tag-lost? error)
        flow          (get-in db [:hardwallet :flow])]
    (log/debug "[hardwallet] on-pair-error" setup-step "flow:" flow)
    (when-not tag-was-lost?
      (fx/merge cofx
                {:db (assoc-in db [:hardwallet :setup-error] (i18n/label :t/invalid-pairing-password))}
                (common/set-on-card-connected (if (= setup-step :pairing)
                                                :hardwallet/load-pairing-screen
                                                :hardwallet/pair))
                (common/hide-connection-sheet)
                (when (= flow :import)
                  (navigation/navigate-to-cofx :keycard-recovery-pair nil))
                (when (not= setup-step :enter-pair-code)
                  (common/process-error code error))))))

(fx/defn set-setup-step
  [{:keys [db]} card-state]
  {:db (assoc-in db [:hardwallet :setup-step]
                 (case card-state
                   :not-paired       :pair
                   :no-pairing-slots :no-slots
                   :init             :card-ready
                   :multiaccount     :import-multiaccount
                   :begin))})

(fx/defn show-no-keycard-applet-alert [_]
  {:utils/show-confirmation {:title               (i18n/label :t/no-keycard-applet-on-card)
                             :content             (i18n/label :t/keycard-applet-install-instructions)
                             :cancel-button-text  ""
                             :confirm-button-text :t/okay}})

;; NOTE: Maybe replaced by multiple events based on on flow to make it easier to maintain.
;; Because there are many execution paths it is harder to follow all possible states.
(fx/defn check-card-state
  {:events [:hardwallet/check-card-state]}
  [{:keys [db] :as cofx}]
  (let [app-info                       (get-in db [:hardwallet :application-info])
        flow                           (get-in db [:hardwallet :flow])
        {:keys [instance-uid key-uid]} app-info
        pairing                        (common/get-pairing db key-uid)
        app-info'                      (if pairing (assoc app-info :paired? true) app-info)
        card-state                     (common/get-card-state app-info')]
    (log/debug "[hardwallet] check-card-state"
               "card-state" card-state
               "flow" flow)
    (fx/merge cofx
              {:db (assoc-in db [:hardwallet :card-state] card-state)}
              (set-setup-step card-state)
              (common/hide-connection-sheet)

              (when (and flow
                         (= card-state :init))
                (proceed-setup-with-initialized-card flow instance-uid))

              (when (= card-state :pre-init)
                (if (= flow :import)
                  (navigation/navigate-to-cofx :keycard-recovery-no-key nil)
                  (fn [cofx]
                    (fx/merge
                     cofx
                     (common/clear-on-card-read)
                     (load-pin-screen)))))

              (when (and (= card-state :multiaccount)
                         (= flow :import))
                (if (common/find-multiaccount-by-key-uid db key-uid)
                  (multiaccounts.recover/show-existing-multiaccount-alert key-uid)
                  (if pairing
                    (load-recovery-pin-screen)
                    (recovery/load-pair-screen))))

              (when (= card-state :blank)
                (if (= flow :import)
                  (navigation/navigate-to-cofx :keycard-recovery-no-key nil)
                  (show-no-keycard-applet-alert)))

              (when (and (= card-state :multiaccount)
                         (#{:create :recovery} flow))
                (show-keycard-has-multiaccount-alert)))))

(fx/defn on-card-connected
  {:events [:hardwallet.callback/on-card-connected]}
  [{:keys [db]} _]
  (log/debug "[hardwallet] card globally connected")
  {:db (assoc-in db [:hardwallet :card-connected?] true)})

(fx/defn on-card-disconnected
  {:events [:hardwallet.callback/on-card-disconnected]}
  [{:keys [db]} _]
  (log/debug "[hardwallet] card disconnected")
  {:db (assoc-in db [:hardwallet :card-connected?] false)})

(fx/defn on-register-card-events
  {:events [:hardwallet.callback/on-register-card-events]}
  [{:keys [db]} listeners]
  {:db (update-in db [:hardwallet :listeners] merge listeners)})
