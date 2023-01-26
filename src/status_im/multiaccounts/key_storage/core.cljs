(ns status-im.multiaccounts.key-storage.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.bottom-sheet.core :as bottom-sheet]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.mnemonic :as mnemonic]
            [utils.i18n :as i18n]
            [status-im.keycard.backup-key :as keycard.backup]
            [status-im.keycard.common :as common]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.multiaccounts.logout.core :as multiaccounts.logout]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.native-module.core :as native-module]
            [status-im.popover.core :as popover]
            [utils.re-frame :as rf]
            [status-im.utils.types :as types]
            [status-im2.navigation.events :as navigation]
            [utils.security.core :as security]))

(rf/defn key-and-storage-management-pressed
  "This event can be dispatched before login and from profile and needs to redirect accordingly"
  {:events [::key-and-storage-management-pressed]}
  [cofx]
  (navigation/navigate-to-cofx
   cofx
   (if (multiaccounts.model/logged-in? cofx)
     :actions-logged-in
     :actions-not-logged-in)
   nil))

(rf/defn move-keystore-checked
  {:events [::move-keystore-checked]}
  [{:keys [db] :as cofx} checked?]
  {:db (assoc-in db [:multiaccounts/key-storage :move-keystore-checked?] checked?)})

(rf/defn reset-db-checked
  {:events [::reset-db-checked]}
  [{:keys [db] :as cofx} checked?]
  {:db (assoc-in db [:multiaccounts/key-storage :reset-db-checked?] checked?)})

(rf/defn navigate-back
  {:events [::navigate-back]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (-> db
            (dissoc :recovered-account?)
            (update :keycard
                    dissoc
                    :from-key-storage-and-migration?
                    :creating-backup?
                    :factory-reset-card?))}
   (navigation/navigate-back)))

(rf/defn enter-seed-pressed
  "User is logged out and probably wants to move multiaccount to Keycard. Navigate to enter seed phrase screen"
  {:events [::enter-seed-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (assoc db :recovered-account? true)}
   (navigation/navigate-to-cofx :seed-phrase nil)))

(rf/defn seed-phrase-input-changed
  {:events [::seed-phrase-input-changed]}
  [{:keys [db] :as cofx} masked-seed-phrase]
  (let [seed-phrase (security/safe-unmask-data masked-seed-phrase)]
    {:db (update db
                 :multiaccounts/key-storage assoc
                 :seed-phrase               (when seed-phrase
                                              (string/lower-case seed-phrase))
                 :seed-shape-invalid?       (or (empty? seed-phrase)
                                                (not (mnemonic/valid-length? seed-phrase)))
                 :seed-word-count           (mnemonic/words-count seed-phrase))}))

(rf/defn key-uid-seed-mismatch
  {:events [::show-seed-key-uid-mismatch-error-popup]}
  [cofx _]
  (popover/show-popover cofx {:view :seed-key-uid-mismatch}))

(rf/defn key-uid-matches
  {:events [::key-uid-matches]}
  [{:keys [db] :as cofx} _]
  (let [backup? (get-in db [:keycard :creating-backup?])]
    (if backup?
      (keycard.backup/start-keycard-backup cofx)
      (navigation/navigate-to-cofx cofx :storage nil))))

(defn validate-seed-against-key-uid
  "Check if the key-uid was generated with the given seed-phrase"
  [{:keys [import-mnemonic-fn on-success on-error]} {:keys [seed-phrase key-uid]}]
  (import-mnemonic-fn
   (mnemonic/sanitize-passphrase seed-phrase)
   nil
   (fn [result]
     (let [{:keys [keyUid]} (types/json->clj result)]
       ;; if the key-uid from app-db is same as the one returned by multiaccount import,
       ;; it means that this seed was used to generate this multiaccount
       (if (= key-uid keyUid)
         (on-success)
         (on-error))))))

(re-frame/reg-fx
 ::validate-seed-against-key-uid
 (partial validate-seed-against-key-uid
          {:import-mnemonic-fn native-module/multiaccount-import-mnemonic
           :on-success         #(re-frame/dispatch [::key-uid-matches])
           :on-error           #(re-frame/dispatch [::show-seed-key-uid-mismatch-error-popup])}))

(rf/defn seed-phrase-validated
  {:events [::seed-phrase-validated]}
  [{:keys [db] :as cofx} validation-error]
  (let [error?      (-> validation-error
                        types/json->clj
                        :error
                        string/blank?
                        not)
        onboarding? (not (or (:multiaccounts/login db) (:multiaccount db)))]
    (if error?
      (popover/show-popover cofx {:view :custom-seed-phrase})
      {::validate-seed-against-key-uid {:seed-phrase (-> db :multiaccounts/key-storage :seed-phrase)
                                        ;; Unique key-uid of the account for which we are going to move
                                        ;; keys
                                        :key-uid     (or (-> db :multiaccounts/login :key-uid)
                                                         (-> db :multiaccount :key-uid)
                                                         (and onboarding?
                                                              (-> db
                                                                  :keycard
                                                                  :application-info
                                                                  :key-uid)))}})))

(rf/defn choose-storage-pressed
  {:events [::choose-storage-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [seed-phrase]} (:multiaccounts/key-storage db)]
    {::multiaccounts/validate-mnemonic
     [(mnemonic/sanitize-passphrase seed-phrase)
      #(re-frame/dispatch [::seed-phrase-validated %])]}))

(rf/defn keycard-storage-pressed
  {:events [::keycard-storage-pressed]}
  [{:keys [db]} selected?]
  {:db (assoc-in db [:multiaccounts/key-storage :keycard-storage-selected?] selected?)})

(re-frame/reg-fx
 ::delete-imported-key
 (fn [{:keys [key-uid address password on-success on-error]}]
   (let [hashed-pass (ethereum/sha3 (security/safe-unmask-data password))]
     (native-module/delete-imported-key
      key-uid
      (string/lower-case (subs address 2))
      hashed-pass
      (fn [result]
        (let [{:keys [error]} (types/json->clj result)]
          (if-not (string/blank? error)
            (on-error error)
            (on-success))))))))

#_"Multiaccount has been deleted from device. We now need to emulate the restore seed phrase process, and make the user land on Keycard setup screen.
To ensure that keycard setup works, we need to:
1. Import multiaccount, derive required keys and save them at the correct location in app-db
2. Take the user to :keycard-onboarding-intro screen in :intro-login-stack

The exact events dispatched for this flow if consumed from the UI are:
:m.r/enter-phrase-input-changed
:m.r/enter-phrase-next-pressed
:m.r/re-encrypt-pressed
:i/on-key-storage-selected ([:intro-wizard :selected-storage-type] is set to :advanced)
:m.r/select-storage-next-pressed

We don't need to take the exact steps, just set the required state and redirect to correct screen
"
(rf/defn import-multiaccount
  [{:keys [db] :as cofx}]
  {:dispatch                                   [:bottom-sheet/hide]
   ::multiaccounts.recover/import-multiaccount
   {:passphrase    (get-in db [:multiaccounts/key-storage :seed-phrase])
    :password      nil
    :success-event ::import-multiaccount-success}})

(rf/defn delete-multiaccount-and-init-keycard-onboarding
  {:events [::delete-multiaccount-and-init-keycard-onboarding]}
  [{:keys [db] :as cofx}]
  (rf/merge
   {:dispatch [:bottom-sheet/hide]
    :db       (assoc-in db [:multiaccounts/key-storage :reset-db-checked?] true)}
   (import-multiaccount)))

(rf/defn storage-selected
  {:events [::storage-selected]}
  [{:keys [db] :as cofx}]
  (if (get-in db [:multiaccounts/key-storage :reset-db-checked?])
    (popover/show-popover cofx {:view :transfer-multiaccount-to-keycard-warning})
    (bottom-sheet/show-bottom-sheet cofx {:view :migrate-account-password})))

(rf/defn skip-password-pressed
  {:events [::skip-password-pressed]}
  [cofx]
  (popover/show-popover cofx {:view :transfer-multiaccount-to-keycard-warning}))

(rf/defn password-changed
  {:events [::password-changed]}
  [{db :db} password]
  (let [unmasked-pass (security/safe-unmask-data password)]
    {:db (update db
                 :keycard                   assoc
                 :migration-password        password
                 :migration-password-error  nil
                 :migration-password-valid? (and unmasked-pass (> (count unmasked-pass) 5)))}))

(rf/defn verify-password-result
  {:events [::verify-password-result]}
  [{:keys [db] :as cofx} result]
  (let [{:keys [error]} (types/json->clj result)]
    (if (string/blank? error)
      (rf/merge
       cofx
       {:db (update db :keycard dissoc :migration-password-error :migration-password-valid?)}
       (import-multiaccount))
      {:db (assoc-in db [:keycard :migration-password-error] (i18n/label :t/wrong-password))})))

(rf/defn verify-password
  {:events [::verify-password]}
  [{:keys [db] :as cofx}]
  (native-module/verify-database-password
   (get-in db [:multiaccounts/login :key-uid])
   (ethereum/sha3 (security/safe-unmask-data (get-in db [:keycard :migration-password])))
   #(re-frame/dispatch [::verify-password-result %])))

(rf/defn handle-multiaccount-import
  {:events [::import-multiaccount-success]}
  [{:keys [db] :as cofx} root-data derived-data]
  (rf/merge cofx
            {:db (-> db
                     (update :intro-wizard
                             assoc
                             :root-key              root-data
                             :derived               derived-data
                             :recovering?           true
                             :selected-storage-type :advanced)
                     (assoc-in [:keycard :flow] :recovery)
                     (assoc-in [:keycard :from-key-storage-and-migration?] true)
                     (assoc-in [:keycard :converting-account?]
                               (not (get-in db [:multiaccounts/key-storage :reset-db-checked?])))
                     (assoc-in [:keycard :delete-account?]
                               (true? (get-in db [:multiaccounts/key-storage :reset-db-checked?])))
                     (dissoc :multiaccounts/key-storage))}
            (popover/hide-popover)
            (common/listen-to-hardware-back-button)
            (navigation/navigate-to-cofx :keycard-onboarding-intro nil)))

(rf/defn goto-multiaccounts-screen
  {:events [::hide-popover-and-goto-multiaccounts-screen]}
  [cofx _]
  (rf/merge cofx
            (popover/hide-popover)
            (navigation/navigate-to-cofx :multiaccounts nil)))

(rf/defn confirm-logout-and-goto-key-storage
  {:events [::confirm-logout-and-goto-key-storage]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            {:db (assoc db :goto-key-storage? true)}
            (multiaccounts.logout/logout)))

(rf/defn logout-and-goto-key-storage
  {:events [::logout-and-goto-key-storage]}
  [_]
  {:ui/show-confirmation
   {:title               (i18n/label :t/logout-title)
    :content             (i18n/label :t/logout-key-management)
    :confirm-button-text (i18n/label :t/logout)
    :on-accept           #(re-frame/dispatch [::confirm-logout-and-goto-key-storage])
    :on-cancel           nil}})

(comment
  ;; check import mnemonic output
  (native-module/multiaccount-import-mnemonic
   "rocket mixed rebel affair umbrella legal resemble scene virus park deposit cargo"
   nil
   (fn [result]
     (prn (types/json->clj result))))
  ;; check delete account output
  (native-module/delete-multiaccount "0x3831d0f22996a65970a214f0a94bfa9a63a21dac235d8dadb91be8e32e7d3ab7"
                                     (fn [result]
                                       (prn ::--delete-account-res-> result))))
