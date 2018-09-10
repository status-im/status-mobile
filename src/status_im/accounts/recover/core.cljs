(ns status-im.accounts.recover.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.create.core :as accounts.create]
            [status-im.accounts.db :as db]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.utils.ethereum.mnemonic :as mnemonic]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]))

(defn check-password-errors [password]
  (cond (string/blank? password) :required-field
        (not (db/valid-length? password)) :recover-password-too-short))

(defn check-phrase-errors [recovery-phrase]
  (cond (string/blank? recovery-phrase) :required-field
        (not (mnemonic/valid-phrase? recovery-phrase)) :recovery-phrase-invalid))

(defn check-phrase-warnings [recovery-phrase]
  (when (not (mnemonic/status-generated-phrase? recovery-phrase))
    :recovery-phrase-unknown-words))

(defn recover-account! [masked-passphrase password]
  (status/recover-account
   (mnemonic/sanitize-passphrase (security/unmask masked-passphrase))
   password
   (fn [result]
     ;; here we deserialize result, dissoc mnemonic and serialize the result again
     ;; because we want to have information about the result printed in logs, but
     ;; don't want secure data to be printed
     (let [data (-> (types/json->clj result)
                    (dissoc :mnemonic)
                    (types/clj->json))]
       (re-frame/dispatch [:accounts.recover.callback/recover-account-success data password])))))

(defn set-phrase [masked-recovery-phrase {:keys [db]}]
  (let [recovery-phrase (security/unmask masked-recovery-phrase)]
    {:db (update db :accounts/recover assoc
                 :passphrase (string/lower-case recovery-phrase)
                 :passphrase-valid? (not (check-phrase-errors recovery-phrase)))}))

(defn validate-phrase [{:keys [db]}]
  (let [recovery-phrase (get-in db [:accounts/recover :passphrase])]
    {:db (update db :accounts/recover assoc
                 :passphrase-error (check-phrase-errors recovery-phrase)
                 :passphrase-warning (check-phrase-warnings recovery-phrase))}))

(defn set-password [masked-password {:keys [db]}]
  (let [password (security/unmask masked-password)]
    {:db (update db :accounts/recover assoc
                 :password password
                 :password-valid? (not (check-password-errors password)))}))

(defn validate-password [{:keys [db]}]
  (let [password (get-in db [:accounts/recover :password])]
    {:db (assoc-in db [:accounts/recover :password-error] (check-password-errors password))}))

(defn validate-recover-result [{:keys [error pubkey address]} password {:keys [db] :as cofx}]
  (if (empty? error)
    (let [account {:pubkey     pubkey
                   :address    address
                   :photo-path (identicon/identicon pubkey)
                   :mnemonic   ""}]
      (accounts.create/on-account-created account password true cofx))
    {:db (assoc-in db [:accounts/recover :password-error] :recover-password-invalid)}))

(defn on-account-recovered [result password {:keys [db] :as cofx}]
  (let [data (types/json->clj result)]
    (handlers-macro/merge-fx cofx
                             {:db (assoc-in db [:accounts/recover :processing?] false)}
                             (validate-recover-result data password))))

(defn recover-account [{:keys [db]}]
  (let [{:keys [password passphrase]} (:accounts/recover db)]
    {:db (assoc-in db [:accounts/recover :processing?] true)
     :accounts.recover/recover-account [(security/mask-data passphrase) password]}))

(defn recover-account-with-checks [{:keys [db] :as cofx}]
  (let [{:keys [passphrase]} (:accounts/recover db)]
    (if (mnemonic/status-generated-phrase? passphrase)
      (recover-account cofx)
      {:ui/show-confirmation {:title               (i18n/label :recovery-typo-dialog-title)
                              :content             (i18n/label :recovery-typo-dialog-description)
                              :confirm-button-text (i18n/label :recovery-confirm-phrase)
                              :on-accept           #(re-frame/dispatch [:accounts.recover.ui/recover-account-confirmed])}})))

(re-frame/reg-fx
 :accounts.recover/recover-account
 (fn [[masked-passphrase password]]
   (recover-account! masked-passphrase password)))
