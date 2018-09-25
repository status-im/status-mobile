(ns status-im.accounts.access.core
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
            [status-im.utils.fx :as fx]))

(defn check-phrase-errors [recovery-phrase]
  (cond (string/blank? recovery-phrase) :required-field
        (not (mnemonic/valid-phrase? recovery-phrase)) :recovery-phrase-invalid))

(defn check-phrase-warnings [recovery-phrase]
  (when (not (mnemonic/status-generated-phrase? recovery-phrase))
    :recovery-phrase-unknown-words))

(fx/defn set-phrase [{:keys [db]} masked-recovery-phrase]
  (let [recovery-phrase (security/unmask masked-recovery-phrase)]
    {:db (update db :accounts/access assoc :passphrase (string/lower-case recovery-phrase))}))

(fx/defn validate-phrase [{:keys [db]}]
  (let [recovery-phrase (get-in db [:accounts/access :passphrase])]
    {:db (update db :accounts/access assoc
                 :passphrase-error (check-phrase-errors recovery-phrase)
                 :passphrase-warning (check-phrase-warnings recovery-phrase))}))

(defn access-account! [masked-passphrase password]
  (status/access-account
   (mnemonic/sanitize-passphrase (security/unmask masked-passphrase))
   password
   (fn [result]
     ;; here we deserialize result, dissoc mnemonic and serialize the result again
     ;; because we want to have information about the result printed in logs, but
     ;; don't want secure data to be printed
     (let [data (-> (types/json->clj result)
                    (dissoc :mnemonic)
                    (types/clj->json))]
       (re-frame/dispatch [:accounts.access.callback/access-account-success data password])))))

(fx/defn validate-access-result [{:keys [db] :as cofx} {:keys [error pubkey address]} password]
  (if (empty? error)
    (let [account {:pubkey     pubkey
                   :address    address
                   :photo-path (identicon/identicon pubkey)
                   :mnemonic   ""}]
      (accounts.create/on-account-created cofx account password true))
    {:db (assoc-in db [:accounts/access :error] (i18n/label :t/recover-password-invalid))}))

(fx/defn on-account-accessed [cofx result password]
  (let [data (types/json->clj result)]
    (validate-access-result cofx data password)))

(defn access-account [{:keys [db]}]
  (let [{:keys [password passphrase]} (:accounts/access db)]
    {:db (assoc-in db [:accounts/access :processing?] true)
     :accounts.access/access-account [(security/mask-data passphrase) password]}))

(defn access-account-with-checks [{:keys [db] :as cofx}]
  (let [{:keys [passphrase processing?]} (:accounts/access db)]
    (when-not processing?
      (if (mnemonic/status-generated-phrase? passphrase)
        (access-account cofx)
        {:ui/show-confirmation
         {:title               (i18n/label :recovery-typo-dialog-title)
          :content             (i18n/label :recovery-typo-dialog-description)
          :confirm-button-text (i18n/label :recovery-confirm-phrase)
          :on-accept           #(re-frame/dispatch [:accounts.access.ui/access-account-confirmed])}}))))

(fx/defn next-step [{:keys [db] :as cofx} step]
  (case step
    :passphrase {:db (assoc-in db [:accounts/access :step] :enter-password)}
    :enter-password {:db (assoc-in db [:accounts/access :step] :confirm-password)}
    :confirm-password (let [{:keys [password password-confirm]} (:accounts/access db)]
                        (if (= password password-confirm)
                          (access-account-with-checks cofx)
                          {:db (assoc-in db [:accounts/access :error] (i18n/label :t/password_error1))}))))

(fx/defn step-back [{:keys [db] :as cofx} step]
  (case step
    :passphrase (navigation/navigate-back cofx)
    :enter-password {:db (update db :accounts/access merge {:step  :passphrase
                                                            :error nil})}
    :confirm-password {:db (update db :accounts/access merge {:step  :enter-password
                                                              :error nil})}))

(fx/defn account-set-input-text [{:keys [db]} input-key masked-text]
  (let [text (security/unmask masked-text)]
    {:db (update db :accounts/access merge {input-key text :error nil})}))

(fx/defn navigate-to-access-account-screen [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc db :accounts/access {:step :passphrase})}
            (navigation/navigate-to-cofx :access-account nil)))

(re-frame/reg-fx
 :accounts.access/access-account
 (fn [[masked-passphrase password]]
   (access-account! masked-passphrase password)))
