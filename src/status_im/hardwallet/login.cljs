(ns status-im.hardwallet.login
  (:require [status-im.ethereum.core :as ethereum]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]
            [status-im.hardwallet.common :as common]
            [status-im.hardwallet.recovery :as recovery]
            [status-im.hardwallet.onboarding :as onboarding]
            status-im.hardwallet.fx
            [status-im.ui.components.bottom-sheet.core :as bottom-sheet]))

(fx/defn login-got-it-pressed
  {:events [:keycard.login.pin.ui/got-it-pressed
            :keycard.login.pin.ui/cancel-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db db}
            (navigation/navigate-to-cofx :multiaccounts nil)))

(fx/defn login-pin-more-icon-pressed
  {:events [:keycard.login.pin.ui/more-icon-pressed]}
  [cofx]
  (bottom-sheet/show-bottom-sheet cofx {:view :keycard.login/more}))

(fx/defn login-create-key-pressed
  {:events [:keycard.login.ui/create-new-key-pressed]}
  [cofx]
  (fx/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (onboarding/start-onboarding-flow)))

(fx/defn login-add-key-pressed
  {:events [:keycard.login.ui/add-key-pressed]}
  [cofx]
  (recovery/start-import-flow cofx))

(fx/defn login-remember-me-changed
  {:events [:keycard.login.ui/remember-me-changed]}
  [{:keys [db] :as cofx} value]
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :remember-me?] value)}))

(fx/defn login-pair-card-pressed
  {:events [:keycard.login.ui/pair-card-pressed]}
  [{:keys [db] :as cofx}]
  (log/debug "[hardwallet] load-pair-card-pressed")
  (fx/merge cofx
            {:db (assoc-in db [:hardwallet :flow] :login)}
            (navigation/navigate-to-cofx :keycard-recovery-pair nil)))

(fx/defn login-with-keycard
  {:events [:hardwallet/login-with-keycard]}
  [{:keys [db] :as cofx}]
  (let [application-info       (get-in db [:hardwallet :application-info])
        key-uid                (get-in db [:hardwallet :application-info :key-uid])
        multiaccount           (get-in db [:multiaccounts/multiaccounts (get-in db [:multiaccounts/login :key-uid])])
        multiaccount-key-uid   (get multiaccount :key-uid)
        multiaccount-mismatch? (or (nil? multiaccount)
                                   (not= multiaccount-key-uid key-uid))
        pairing                (:keycard-pairing multiaccount)]
    (cond
      (empty? application-info)
      (fx/merge cofx
                (common/hide-pair-sheet)
                (navigation/navigate-to-cofx :not-keycard nil))

      (empty? key-uid)
      (fx/merge cofx
                (common/hide-pair-sheet)
                (navigation/navigate-to-cofx :keycard-blank nil))

      multiaccount-mismatch?
      (fx/merge cofx
                (common/hide-pair-sheet)
                (navigation/navigate-to-cofx :keycard-wrong nil))

      (empty? pairing)
      (fx/merge cofx
                (common/hide-pair-sheet)
                (navigation/navigate-to-cofx :keycard-unpaired nil))

      :else
      (common/get-keys-from-keycard cofx))))

(fx/defn proceed-to-login
  [{:keys [db] :as cofx}]
  (let [{:keys [card-connected?]} (:hardwallet db)]
    (fx/merge cofx
              (common/set-on-card-connected :hardwallet/get-application-info)
              (common/set-on-card-read :hardwallet/login-with-keycard)
              (if card-connected?
                (login-with-keycard)
                (common/show-pair-sheet {:on-cancel [::common/cancel-sheet-confirm]})))))

(fx/defn on-hardwallet-keychain-keys
  {:events [:multiaccounts.login.callback/get-hardwallet-keys-success]}
  [{:keys [db] :as cofx} key-uid [encryption-public-key whisper-private-key :as creds]]
  (if (nil? creds)
    (navigation/navigate-to-cofx cofx :keycard-login-pin nil)
    (let [{:keys [photo-path name]} (get-in db [:multiaccounts/multiaccounts key-uid])
          multiaccount-data         (types/clj->json {:name       name
                                                      :key-uid    key-uid
                                                      :photo-path photo-path})
          account-data {:key-uid               key-uid
                        :encryption-public-key encryption-public-key
                        :whisper-private-key   whisper-private-key}]
      {:db
       (-> db
           (assoc-in [:hardwallet :pin :status] nil)
           (assoc-in [:hardwallet :pin :login] [])
           (assoc-in [:hardwallet :multiaccount]
                     (update account-data :whisper-public-key ethereum/normalized-hex))
           (assoc-in [:hardwallet :flow] nil)
           (update :multiaccounts/login assoc
                   :password encryption-public-key
                   :key-uid key-uid
                   :photo-path photo-path
                   :name name
                   :save-password? true))
       :hardwallet/login-with-keycard
       {:multiaccount-data multiaccount-data
        :password          encryption-public-key
        :chat-key          whisper-private-key}})))

(fx/defn on-login-success
  {:events [:keycard.login.callback/login-success]}
  [_ result]
  (log/debug "loginWithKeycard success: " result))
