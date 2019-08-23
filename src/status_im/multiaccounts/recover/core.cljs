(ns status-im.multiaccounts.recover.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.mnemonic :as mnemonic]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [status-im.multiaccounts.db :as db]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]))

(defn check-password-errors [password]
  (cond (string/blank? password) :required-field
        (not (db/valid-length? password)) :recover-password-too-short))

(defn check-phrase-errors [recovery-phrase]
  (cond (string/blank? recovery-phrase) :required-field
        (not (mnemonic/valid-words? recovery-phrase)) :recovery-phrase-invalid
        (not (mnemonic/valid-length? recovery-phrase)) :recovery-phrase-wrong-length
        (not (mnemonic/status-generated-phrase? recovery-phrase)) :recovery-phrase-unknown-words))

(defn check-phrase-warnings [recovery-phrase]
  (cond (string/blank? recovery-phrase) :required-field
        (not (mnemonic/valid-words? recovery-phrase)) :recovery-phrase-invalid
        (not (mnemonic/status-generated-phrase? recovery-phrase)) :recovery-phrase-unknown-words))

(fx/defn set-phrase
  {:events [::passphrase-input-changed]}
  [{:keys [db]} masked-recovery-phrase]
  (let [recovery-phrase (security/safe-unmask-data masked-recovery-phrase)]
    (fx/merge
     {:db (update db :multiaccounts/recover assoc
                  :passphrase (string/lower-case recovery-phrase)
                  :passphrase-error nil
                  :next-button-disabled? (or (empty? recovery-phrase)
                                             (not (mnemonic/valid-length? recovery-phrase))))})))

(fx/defn validate-phrase
  {:events [::passphrase-input-blured]}
  [{:keys [db]}]
  (let [recovery-phrase (get-in db [:multiaccounts/recover :passphrase])]
    {:db (update db :multiaccounts/recover assoc
                 :passphrase-error (check-phrase-errors recovery-phrase))}))

(fx/defn validate-phrase-for-warnings
  [{:keys [db]}]
  (let [recovery-phrase (get-in db [:multiaccounts/recover :passphrase])]
    {:db (update db :multiaccounts/recover assoc
                 :passphrase-error (check-phrase-warnings recovery-phrase))}))

(fx/defn set-password
  {:events [::password-input-changed
            ::enter-password-input-changed]}
  [{:keys [db]} masked-password]
  (let [password (security/safe-unmask-data masked-password)]
    {:db (update db :multiaccounts/recover assoc
                 :password password
                 :password-error nil
                 :password-valid? (not (check-password-errors password)))}))

(fx/defn validate-password
  {:events [::password-input-blured]}
  [{:keys [db]}]
  (let [password (get-in db [:multiaccounts/recover :password])]
    {:db (assoc-in db [:multiaccounts/recover :password-error] (check-password-errors password))}))

(fx/defn on-store-multiaccount-success
  {:events       [::store-multiaccount-success]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [{:keys [db] :as cofx} password]
  (let [multiaccount (get-in db [:multiaccounts/recover :root-key])
        multiaccount-address (-> (:address multiaccount)
                                 (string/lower-case)
                                 (string/replace-first "0x" ""))
        keycard-multiaccount? (boolean (get-in db [:multiaccounts/multiaccounts multiaccount-address :keycard-instance-uid]))]
    (if keycard-multiaccount?
      ;; trying to recover multiaccount created with keycard
      {:db        (-> db
                      (update :multiaccounts/recover assoc
                              :processing? false
                              :passphrase-error :recover-keycard-multiaccount-not-supported)
                      (update :multiaccounts/recover dissoc
                              :passphrase-valid?))}
      (let [multiaccount (assoc multiaccount :derived (get-in db [:multiaccounts/recover :derived]))]
        (multiaccounts.create/on-multiaccount-created cofx
                                                      multiaccount
                                                      password
                                                      {:seed-backed-up? true})))))

(fx/defn store-multiaccount
  {:events [::recover-multiaccount-confirmed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [password passphrase root-key]} (:multiaccounts/recover db)
        {:keys [id address]} root-key
        callback #(re-frame/dispatch [::store-multiaccount-success password])]
    {:db (assoc-in db [:multiaccounts/recover :processing?] true)
     ::multiaccounts.create/store-multiaccount [id address password callback]}))

(fx/defn recover-multiaccount-with-checks
  {:events [::sign-in-button-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [passphrase processing?]} (:multiaccounts/recover db)]
    (when-not processing?
      (if (mnemonic/status-generated-phrase? passphrase)
        (store-multiaccount cofx)
        {:ui/show-confirmation
         {:title               (i18n/label :recovery-typo-dialog-title)
          :content             (i18n/label :recovery-typo-dialog-description)
          :confirm-button-text (i18n/label :recovery-confirm-phrase)
          :on-accept           #(re-frame/dispatch [::recover-multiaccount-confirmed])}}))))

(fx/defn navigate-to-recover-multiaccount-screen
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :multiaccounts/recover)}
            (navigation/navigate-to-cofx :recover-multiaccount nil)))

(re-frame/reg-fx
 ::store-multiaccount
 (fn [[id address password]]
   (status/multiaccount-store-account
    id
    (security/safe-unmask-data password)
    (fn []
      (status/multiaccount-load-account
       address
       password
       (fn [value]
         (let [{:keys [id]} (types/json->clj value)]
           (status/multiaccount-store-derived
            id
            [constants/path-whisper constants/path-default-wallet]
            password
            #(re-frame/dispatch [::store-multiaccount-success password])))))))))

(re-frame/reg-fx
 ::import-multiaccount
 (fn [{:keys [passphrase password]}]
   (status/multiaccount-import-mnemonic
    passphrase
    password
    (fn [result]
      (let [{:keys [id] :as root-data} (types/json->clj result)]
        (status-im.native-module.core/multiaccount-derive-addresses
         id
         [constants/path-default-wallet constants/path-whisper]
         (fn [result]
           (let [derived-data (types/json->clj result)]
             (re-frame/dispatch [::import-multiaccount-success
                                 root-data derived-data])))))))))

(fx/defn on-import-multiaccount-success
  {:events [::import-multiaccount-success]}
  [{:keys [db] :as cofx} root-data derived-data]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:multiaccounts/recover :root-key] root-data)
                     (assoc-in [:multiaccounts/recover :derived] derived-data))}
            (navigation/navigate-to-cofx :recover-multiaccount-success nil)))

(fx/defn re-encrypt-pressed
  {:events [::re-encrypt-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:intro-wizard :selected-storage-type] :default)}
            (navigation/navigate-to-cofx :recover-multiaccount-select-storage nil)))

(fx/defn enter-phrase-pressed
  {:events [::enter-phrase-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db       (assoc db :multiaccounts/recover {:next-button-disabled? true})
             :dispatch [:bottom-sheet/hide-sheet]}
            (navigation/navigate-to-cofx :recover-multiaccount-enter-phrase nil)))

(fx/defn proceed-to-import-mnemonic
  {:events [::enter-phrase-input-submitted ::enter-phrase-next-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [password passphrase]} (:multiaccounts/recover db)]
    (when (mnemonic/valid-length? passphrase)
      {::import-multiaccount {:passphrase passphrase
                              :password password}})))

(fx/defn cancel-pressed
  {:events [::cancel-pressed]}
  [{:keys [db] :as cofx}]
  ;; Workaround for multiple Cancel button clicks
  ;; that can break navigation tree
  (when-not (#{:multiaccounts :login} (:view-id db))
    (navigation/navigate-back cofx)))

(fx/defn select-storage-next-pressed
  {:events       [::select-storage-next-pressed]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)]}
  [{:keys [db] :as cofx}]
  (let [storage-type (get-in db [:intro-wizard :selected-storage-type])]
    (if (= storage-type :advanced)
      {:dispatch [:recovery.ui/keycard-option-pressed]})
    (navigation/navigate-to-cofx cofx :recover-multiaccount-enter-password nil)))

(fx/defn proceed-to-password-confirm
  [{:keys [db] :as cofx}]
  (when (nil? (get-in db [:multiaccounts/recover :password-error]))
    (navigation/navigate-to-cofx cofx :recover-multiaccount-confirm-password nil)))

(fx/defn enter-password-next-button-pressed
  {:events [::enter-password-input-submitted
            ::enter-password-next-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            (validate-password)
            (proceed-to-password-confirm)))

(fx/defn confirm-password-next-button-pressed
  {:events [::confirm-password-input-submitted
            ::confirm-password-next-pressed]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)]}
  [{:keys [db] :as cofx}]
  (let [{:keys [password password-confirmation]} (:multiaccounts/recover db)]
    (if (= password password-confirmation)
      (fx/merge cofx
                {:db (assoc db :intro-wizard nil)}
                (store-multiaccount)
                (navigation/navigate-to-cofx :keycard-welcome nil))
      {:db (assoc-in db [:multiaccounts/recover :password-error] :password_error1)})))

(fx/defn count-words
  [{:keys [db]}]
  (let [passphrase (get-in db [:multiaccounts/recover :passphrase])]
    {:db (assoc-in db [:multiaccounts/recover :words-count]
                   (mnemonic/words-count passphrase))}))

(fx/defn run-validation
  [{:keys [db] :as cofx}]
  (let [passphrase (get-in db [:multiaccounts/recover :passphrase])]
    (when (= (last passphrase) " ")
      (fx/merge cofx
                (validate-phrase-for-warnings)))))

(fx/defn enter-phrase-input-changed
  {:events [::enter-phrase-input-changed]}
  [cofx input]
  (fx/merge cofx
            (set-phrase input)
            (count-words)
            (run-validation)))

(fx/defn confirm-password-input-changed
  {:events [::confirm-password-input-changed]}
  [{:keys [db]} input]
  {:db (-> db
           (assoc-in [:multiaccounts/recover :password-confirmation] input)
           (assoc-in [:multiaccounts/recover :password-error] nil))})
