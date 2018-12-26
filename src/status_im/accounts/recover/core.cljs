(ns status-im.accounts.recover.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.create.core :as accounts.create]
            [status-im.accounts.db :as db]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.ethereum.mnemonic :as mnemonic]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]
            [status-im.utils.fx :as fx]
            [status-im.node.core :as node]))

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
   (mnemonic/sanitize-passphrase (security/safe-unmask-data masked-passphrase))
   password
   (fn [result]
     ;; here we deserialize result, dissoc mnemonic and serialize the result again
     ;; because we want to have information about the result printed in logs, but
     ;; don't want secure data to be printed
     (let [data (-> (types/json->clj result)
                    (dissoc :mnemonic)
                    (types/clj->json))]
       (re-frame/dispatch [:accounts.recover.callback/recover-account-success data password])))))

(fx/defn set-phrase
  [{:keys [db]} masked-recovery-phrase]
  (let [recovery-phrase (security/safe-unmask-data masked-recovery-phrase)]
    {:db (update db :accounts/recover assoc
                 :passphrase (string/lower-case recovery-phrase)
                 :passphrase-valid? (not (check-phrase-errors recovery-phrase)))}))

(fx/defn validate-phrase
  [{:keys [db]}]
  (let [recovery-phrase (get-in db [:accounts/recover :passphrase])]
    {:db (update db :accounts/recover assoc
                 :passphrase-error (check-phrase-errors recovery-phrase)
                 :passphrase-warning (check-phrase-warnings recovery-phrase))}))

(fx/defn set-password
  [{:keys [db]} masked-password]
  (let [password (security/safe-unmask-data masked-password)]
    {:db (update db :accounts/recover assoc
                 :password password
                 :password-valid? (not (check-password-errors password)))}))

(fx/defn validate-password
  [{:keys [db]}]
  (let [password (get-in db [:accounts/recover :password])]
    {:db (assoc-in db [:accounts/recover :password-error] (check-password-errors password))}))

(fx/defn validate-recover-result
  [{:keys [db] :as cofx} {:keys [error pubkey address]} password]
  (if (empty? error)
    (let [account {:pubkey     pubkey
                   :address    address
                   :photo-path (identicon/identicon pubkey)
                   :mnemonic   ""}]
      (accounts.create/on-account-created
       cofx account password {:seed-backed-up? true}))
    {:db        (-> db
                    (update :accounts/recover assoc
                            :processing? false
                            :password ""
                            :password-error :recover-password-invalid)
                    (update :accounts/recover dissoc
                            :password-valid?))
     :node/stop nil}))

(fx/defn on-account-recovered
  [cofx result password]
  (let [data (types/json->clj result)]
    (validate-recover-result cofx data password)))

(fx/defn recover-account
  [{:keys [db random-guid-generator] :as cofx}]
  (fx/merge
   cofx
   {:db (-> db
            (assoc-in [:accounts/recover :processing?] true)
            (assoc :node/on-ready :recover-account)
            (assoc :accounts/new-installation-id (random-guid-generator)))}
   (node/initialize nil)))

(fx/defn recover-account-with-checks [{:keys [db] :as cofx}]
  (let [{:keys [passphrase processing?]} (:accounts/recover db)]
    (when-not processing?
      (if (mnemonic/status-generated-phrase? passphrase)
        (recover-account cofx)
        {:ui/show-confirmation
         {:title               (i18n/label :recovery-typo-dialog-title)
          :content             (i18n/label :recovery-typo-dialog-description)
          :confirm-button-text (i18n/label :recovery-confirm-phrase)
          :on-accept           #(re-frame/dispatch [:accounts.recover.ui/recover-account-confirmed])}}))))

(fx/defn navigate-to-recover-account-screen [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :accounts/recover)}
            (navigation/navigate-to-cofx :recover nil)))

(re-frame/reg-fx
 :accounts.recover/recover-account
 (fn [[masked-passphrase password]]
   (recover-account! masked-passphrase password)))
