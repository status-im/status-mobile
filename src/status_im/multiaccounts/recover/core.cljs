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

(defn recover-multiaccount! [masked-passphrase password]
  (status/recover-multiaccount
   (mnemonic/sanitize-passphrase (security/safe-unmask-data masked-passphrase))
   password
   (fn [result]
     ;; here we deserialize result, dissoc mnemonic and serialize the result again
     ;; because we want to have information about the result printed in logs, but
     ;; don't want secure data to be printed
     (let [data (-> (types/json->clj result)
                    (dissoc :mnemonic)
                    (types/clj->json))]
       (re-frame/dispatch [:multiaccounts.recover.callback/recover-multiaccount-success data password])))))

(fx/defn set-phrase
  [{:keys [db]} masked-recovery-phrase]
  (let [recovery-phrase (security/safe-unmask-data masked-recovery-phrase)]
    (fx/merge
     {:db (update db :multiaccounts/recover assoc
                  :passphrase (string/lower-case recovery-phrase)
                  :passphrase-error nil
                  :next-button-disabled? (or (empty? recovery-phrase)
                                             (not (mnemonic/valid-length? recovery-phrase))))})))

(fx/defn validate-phrase
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
  [{:keys [db]} masked-password]
  (let [password (security/safe-unmask-data masked-password)]
    {:db (update db :multiaccounts/recover assoc
                 :password password
                 :password-error nil
                 :password-valid? (not (check-password-errors password)))}))

(fx/defn validate-password
  [{:keys [db]}]
  (let [password (get-in db [:multiaccounts/recover :password])]
    {:db (assoc-in db [:multiaccounts/recover :password-error] (check-password-errors password))}))

(fx/defn validate-recover-result
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
      (let [multiaccount' (assoc multiaccount :derived (get-in db [:multiaccounts/recover :derived]))]
        (multiaccounts.create/on-multiaccount-created cofx
                                                      multiaccount'
                                                      password
                                                      {:seed-backed-up? true})))))

(fx/defn on-multiaccount-recovered
  {:events       [:multiaccounts.recover.callback/recover-multiaccount-success]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [cofx password]
  (validate-recover-result cofx password))

(fx/defn multiaccount-store-derived
  [{:keys [db]}]
  (let [id (get-in db [:multiaccounts/recover :root-key :id])
        password (get-in db [:multiaccounts/recover :password])]
    (status/multiaccount-store-derived
     id
     [constants/path-whisper constants/path-default-wallet]
     password
     #(re-frame/dispatch [:multiaccounts.recover.callback/recover-multiaccount-success password]))))

(fx/defn recover-multiaccount
  [{:keys [db random-guid-generator] :as cofx}]
  (let [{:keys [password passphrase]} (:multiaccounts/recover db)]
    {:db (-> db
             (assoc-in [:multiaccounts/recover :processing?] true)
             (assoc :multiaccounts/new-installation-id (random-guid-generator)))
     :multiaccounts.recover/recover-multiaccount [(security/mask-data passphrase) password]}))

(fx/defn recover-multiaccount-with-checks [{:keys [db] :as cofx}]
  (let [{:keys [passphrase processing?]} (:multiaccounts/recover db)]
    (when-not processing?
      (if (mnemonic/status-generated-phrase? passphrase)
        (recover-multiaccount cofx)
        {:ui/show-confirmation
         {:title               (i18n/label :recovery-typo-dialog-title)
          :content             (i18n/label :recovery-typo-dialog-description)
          :confirm-button-text (i18n/label :recovery-confirm-phrase)
          :on-accept           #(re-frame/dispatch [:multiaccounts.recover.ui/recover-multiaccount-confirmed])}}))))

(fx/defn navigate-to-recover-multiaccount-screen [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :multiaccounts/recover)}
            (navigation/navigate-to-cofx :recover-multiaccount nil)))

(re-frame/reg-fx
 :multiaccounts.recover/recover-multiaccount
 (fn [[masked-passphrase password]]
   (recover-multiaccount! masked-passphrase password)))

(re-frame/reg-fx
 :multiaccounts.recover/import-mnemonic
 (fn [{:keys [passphrase password]}]
   (status-im.native-module.core/multiaccount-import-mnemonic
    passphrase
    password
    (fn [result]
      (re-frame/dispatch [:multiaccounts.recover/import-mnemonic-success result])))))

(re-frame/reg-fx
 :multiaccounts.recover/derive-addresses
 (fn [{:keys [account-id paths]}]
   (status-im.native-module.core/multiaccount-derive-addresses
    account-id
    paths
    (fn [result]
      (re-frame/dispatch [:multiaccounts.recover/derive-addresses-success result])))))

(fx/defn on-import-mnemonic-success
  {:events [:multiaccounts.recover/import-mnemonic-success]}
  [{:keys [db] :as cofx} result]
  (let [{:keys [id] :as data} (types/json->clj result)]
    {:db                                     (assoc-in db [:multiaccounts/recover :root-key] data)
     :multiaccounts.recover/derive-addresses {:account-id id
                                              :paths      [constants/path-default-wallet
                                                           constants/path-whisper]}}))

(fx/defn on-derive-addresses-success
  {:events [:multiaccounts.recover/derive-addresses-success]}
  [{:keys [db] :as cofx} result]
  (let [data (types/json->clj result)]
    (fx/merge cofx
              {:db (assoc-in db [:multiaccounts/recover :derived] data)}
              (navigation/navigate-to-cofx :recover-multiaccount-success nil))))

(fx/defn re-encrypt-pressed
  {:events [:recover.success.ui/re-encrypt-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:intro-wizard :selected-storage-type] :default)}
            (navigation/navigate-to-cofx :recover-multiaccount-select-storage nil)))

(fx/defn enter-phrase-pressed
  {:events [:recover.ui/enter-phrase-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db       (assoc db :multiaccounts/recover {:next-button-disabled? true})
             :dispatch [:bottom-sheet/hide-sheet]}
            (navigation/navigate-to-cofx :recover-multiaccount-enter-phrase nil)))

(fx/defn proceed-to-import-mnemonic
  [{:keys [db random-guid-generator] :as cofx}]
  (let [{:keys [password passphrase]} (:multiaccounts/recover db)]
    (when (mnemonic/valid-length? passphrase)
      (fx/merge cofx
                {:db (assoc db :multiaccounts/new-installation-id (random-guid-generator))
                 :multiaccounts.recover/import-mnemonic {:passphrase passphrase
                                                         :password   password}}))))

(fx/defn enter-phrase-next-button-pressed
  {:events       [:recover.enter-passphrase.ui/input-submitted
                  :recover.enter-passphrase.ui/next-pressed]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)]}
  [{:keys [db] :as cofx}]
  (proceed-to-import-mnemonic cofx))

(fx/defn cancel-pressed
  {:events [:recover.ui/cancel-pressed]}
  [{:keys [db] :as cofx}]
  ;; Workaround for multiple Cancel button clicks
  ;; that can break navigation tree
  (when-not (#{:multiaccounts :login} (:view-id db))
    (navigation/navigate-back cofx)))

(fx/defn select-storage-next-pressed
  {:events       [:recover.select-storage.ui/next-pressed]
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
  {:events [:recover.enter-password.ui/input-submitted
            :recover.enter-password.ui/next-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            (validate-password)
            (proceed-to-password-confirm)))

(fx/defn confirm-password-next-button-pressed
  {:events       [:recover.confirm-password.ui/input-submitted
                  :recover.confirm-password.ui/next-pressed]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)]}
  [{:keys [db] :as cofx}]
  (let [{:keys [password password-confirmation]} (:multiaccounts/recover db)]
    (if (= password password-confirmation)
      (fx/merge cofx
                {:db (assoc db :intro-wizard nil)}
                (multiaccount-store-derived)
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
  {:events [:recover.enter-passphrase.ui/input-changed]}
  [cofx input]
  (fx/merge cofx
            (set-phrase input)
            (count-words)
            (run-validation)))

(fx/defn enter-password-input-changed
  {:events [:recover.enter-password.ui/input-changed]}
  [cofx input]
  (set-password cofx input))

(fx/defn confirm-password-input-changed
  {:events [:recover.confirm-password.ui/input-changed]}
  [{:keys [db]} input]
  {:db (-> db
           (assoc-in [:multiaccounts/recover :password-confirmation] input)
           (assoc-in [:multiaccounts/recover :password-error] nil))})
