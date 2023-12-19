(ns legacy.status-im.keycard.login
  (:require
    [legacy.status-im.bottom-sheet.events :as bottom-sheet]
    [legacy.status-im.keycard.common :as common]
    legacy.status-im.keycard.fx
    [legacy.status-im.keycard.onboarding :as onboarding]
    [legacy.status-im.keycard.recovery :as recovery]
    [legacy.status-im.signing.core :as signing.core]
    [legacy.status-im.utils.deprecated-types :as types]
    [status-im2.navigation.events :as navigation]
    [taoensso.timbre :as log]
    [utils.address :as address]
    [utils.re-frame :as rf]))

(rf/defn login-got-it-pressed
  {:events [:keycard.login.pin.ui/got-it-pressed
            :keycard.login.pin.ui/cancel-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db db}
            (navigation/pop-to-root :profiles)))

(rf/defn login-pin-more-icon-pressed
  {:events [:keycard.login.pin.ui/more-icon-pressed]}
  [cofx]
  (bottom-sheet/hide-bottom-sheet-old cofx))

(rf/defn login-create-key-pressed
  {:events [:keycard.login.ui/create-new-key-pressed]}
  [cofx]
  (rf/merge cofx
            (bottom-sheet/hide-bottom-sheet-old)
            (onboarding/start-onboarding-flow)))

(rf/defn login-add-key-pressed
  {:events [:keycard.login.ui/add-key-pressed]}
  [cofx]
  (recovery/start-import-flow cofx))

(rf/defn login-remember-me-changed
  {:events [:keycard.login.ui/remember-me-changed]}
  [{:keys [db] :as cofx} value]
  (rf/merge cofx
            {:db (assoc-in db [:keycard :remember-me?] value)}))

(rf/defn login-pair-card-pressed
  {:events [:keycard.login.ui/pair-card-pressed]}
  [{:keys [db] :as cofx}]
  (log/debug "[keycard] load-pair-card-pressed")
  (rf/merge cofx
            {:db (assoc-in db [:keycard :flow] :login)}
            (navigation/navigate-to :keycard-recovery-pair nil)))

(rf/defn reset-pin
  {:events [::reset-pin]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   (signing.core/discard)
   (fn [{:keys [db]}]
     {:db           (-> db
                        (dissoc :popover/popover)
                        (update-in [:keycard :pin]
                                   dissoc
                                   :reset
                                   :puk)
                        (update-in [:keycard :pin]
                                   assoc
                                   :enter-step :reset
                                   :error      nil
                                   :status     nil))
      :hide-popover nil})
   (when (:profile/profile db)
     (navigation/navigate-to :my-profile nil))
   (when-not (:profile/login db)
     (if (:popover/popover db)
       (navigation/navigate-replace :keycard-pin nil)
       (navigation/navigate-to :keycard-pin nil)))))

(rf/defn dismiss-frozen-keycard-popover
  {:events [::frozen-keycard-popover-dismissed]}
  [{:keys [db]}]
  {:db           (-> db
                     (dissoc :popover/popover)
                     (update :keycard dissoc :setup-step))
   :hide-popover nil})

(rf/defn login-with-keycard
  {:events [:keycard/login-with-keycard]}
  [{:keys [db] :as cofx}]
  (let [{:keys [:pin-retry-counter :puk-retry-counter]
         :as   application-info}
        (get-in db [:keycard :application-info])

        key-uid (get-in db [:keycard :application-info :key-uid])
        paired? (get-in db [:keycard :application-info :paired?])
        multiaccount (get-in db
                             [:profile/profiles-overview (get-in db [:profile/login :key-uid])])
        multiaccount-key-uid (get multiaccount :key-uid)
        multiaccount-mismatch? (or (nil? multiaccount)
                                   (not= multiaccount-key-uid key-uid))]
    (log/debug "[keycard] login-with-keycard"
               "empty application info" (empty? application-info)
               "no key-uid"             (empty? key-uid)
               "multiaccount-mismatch?" multiaccount-mismatch?
               "no pairing"             paired?)
    (cond
      (empty? application-info)
      (rf/merge cofx
                (common/hide-connection-sheet)
                (navigation/navigate-to :not-keycard nil))

      (empty? key-uid)
      (rf/merge cofx
                (common/hide-connection-sheet)
                (navigation/navigate-to :keycard-blank nil))

      multiaccount-mismatch?
      (rf/merge cofx
                (common/hide-connection-sheet)
                (navigation/navigate-to :keycard-wrong nil))

      (not paired?)
      (rf/merge cofx
                (common/hide-connection-sheet)
                (navigation/navigate-to :keycard-unpaired nil))

      (and (zero? pin-retry-counter)
           (or (nil? puk-retry-counter)
               (pos? puk-retry-counter)))
      nil

      :else
      (common/get-keys-from-keycard cofx))))

(rf/defn proceed-to-login
  {:events [::login-after-reset]}
  [cofx]
  (log/debug "[keycard] proceed-to-login")
  (common/show-connection-sheet
   cofx
   {:sheet-options     {:on-cancel [::common/cancel-sheet-confirm]}
    :on-card-connected :keycard/get-application-info
    :on-card-read      :keycard/login-with-keycard
    :handler           (common/get-application-info :keycard/login-with-keycard)}))

(rf/defn on-keycard-keychain-keys
  {:events [:multiaccounts.login.callback/get-keycard-keys-success]}
  [{:keys [db] :as cofx} key-uid [encryption-public-key whisper-private-key :as creds]]
  (if (nil? creds)
    (navigation/set-stack-root cofx
                               :multiaccounts-stack
                               [:multiaccounts
                                :keycard-login-pin])
    (let [{:keys [name]}    (get-in db [:profile/profiles-overview key-uid])
          multiaccount-data (types/clj->json {:name    name
                                              :key-uid key-uid})
          account-data      {:key-uid               key-uid
                             :encryption-public-key encryption-public-key
                             :whisper-private-key   whisper-private-key}]
      {:db
       (-> db
           (assoc-in [:keycard :pin :status] nil)
           (assoc-in [:keycard :pin :login] [])
           (assoc-in [:keycard :profile/profile]
                     (update account-data :whisper-public-key address/normalized-hex))
           (assoc-in [:keycard :flow] nil)
           (update :profile/login  assoc
                   :password       encryption-public-key
                   :key-uid        key-uid
                   :name           name
                   :save-password? true))
       :keycard/login-with-keycard
       {:multiaccount-data multiaccount-data
        :key-uid           key-uid
        :password          encryption-public-key
        :chat-key          whisper-private-key}})))

(rf/defn on-login-success
  {:events [:keycard.login.callback/login-success]}
  [_ result]
  (log/debug "loginWithKeycard success: " result))
