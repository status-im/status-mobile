(ns status-im.multiaccounts.recover.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [status-im.multiaccounts.db :as db]
            [status-im.ethereum.mnemonic :as mnemonic]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.security :as security]
            [status-im.utils.types :as types]
            [status-im.constants :as constants]))

(defn check-password-errors [password]
  (cond (string/blank? password) :required-field
        (not (db/valid-length? password)) :recover-password-too-short))

(defn check-phrase-errors [recovery-phrase]
  (cond (string/blank? recovery-phrase) :required-field
        (not (mnemonic/valid-phrase? recovery-phrase)) :recovery-phrase-invalid))

(defn check-phrase-warnings [recovery-phrase]
  (when (not (mnemonic/status-generated-phrase? recovery-phrase))
    :recovery-phrase-unknown-words))

(defn recover-multiaccount! [masked-passphrase password]
  (status/recover-multiaccount
   (mnemonic/sanitize-passphrase (security/safe-unmask-data masked-passphrase))
   password
   (fn [result]
     ;; here we deserialize result, dissoc mnemonic and serialize the result again
     ;; because we want to have information about the result printed in logs, but
     ;; don't want secure data to be printed
     (let [deser (types/json->clj result)
           data (-> deser
                    (dissoc :mnemonic)
                    (types/clj->json))
           address (deser :address)
           pubkey (deser :walletPubKey)
           name (gfycat/generate-gfy pubkey)
           photo-path (identicon/identicon pubkey)
           multiaccounts [(dissoc deser :mnemonic)]
           error (deser :error)]
       (re-frame/dispatch [:multiaccounts.recover.callback/recover-multiaccount-success data masked-passphrase password address name photo-path multiaccounts error])))))

(fx/defn set-phrase
  [{:keys [db]} masked-recovery-phrase]
  (let [recovery-phrase (security/safe-unmask-data masked-recovery-phrase)]
    {:db (update db :multiaccounts/recover assoc
                 :passphrase (string/lower-case recovery-phrase)
                 :passphrase-valid? (not (check-phrase-errors recovery-phrase)))}))

(fx/defn validate-phrase
  [{:keys [db]}]
  (let [recovery-phrase (get-in db [:multiaccounts/recover :passphrase])]
    {:db (update db :multiaccounts/recover assoc
                 :passphrase-error (check-phrase-errors recovery-phrase)
                 :passphrase-warning (check-phrase-warnings recovery-phrase))}))

(fx/defn set-password
  [{:keys [db]} masked-password]
  (let [password (security/safe-unmask-data masked-password)]
    {:db (update db :multiaccounts/recover assoc
                 :password password
                 :password-valid? (not (check-password-errors password)))}))

(fx/defn validate-password
  [{:keys [db]}]
  (let [password (get-in db [:multiaccounts/recover :password])]
    {:db (assoc-in db [:multiaccounts/recover :password-error] (check-password-errors password))}))

(fx/defn validate-recover-result
  [{:keys [db] :as cofx} {:keys [error pubkey address walletAddress walletPubKey chatAddress chatPubKey]} password]
  (if (empty? error)
    (let [account-address (-> address
                              (string/lower-case)
                              (string/replace-first "0x" ""))
          keycard-account? (boolean (get-in db [:multiaccounts account-address :keycard-instance-uid]))]
      (if keycard-account?
        ;; trying to recover account created with keycard
        {:db        (-> db
                        (update :multiaccounts/recover assoc
                                :processing? false
                                :passphrase-error :recover-keycard-multiaccount-not-supported)
                        (update :multiaccounts/recover dissoc
                                :passphrase-valid?))
         :node/stop nil}
        (let [multiaccount {:derived    {constants/path-whisper-keyword        {:publicKey chatPubKey
                                                                                :address chatAddress}
                                         constants/path-default-wallet-keyword {:publicKey walletPubKey
                                                                                :address walletAddress}}
                            :address    address
                            :mnemonic   ""}]
          (multiaccounts.create/on-multiaccount-created
           cofx multiaccount password {:seed-backed-up? true}))))
    {:db        (-> db
                    (update :multiaccounts/recover assoc
                            :processing? false
                            :password ""
                            :password-error :recover-password-invalid)
                    (update :multiaccounts/recover dissoc
                            :password-valid?))
     :node/stop nil}))

(fx/defn on-multiaccount-recovered
  [cofx result password]
  (let [data (types/json->clj result)]
    (validate-recover-result cofx data password)))

(fx/defn recover-multiaccount
  [{:keys [db random-guid-generator] :as cofx}]
  (fx/merge
   cofx
   {:db (-> db
            (assoc-in [:multiaccounts/recover :processing?] true)
            (assoc :node/on-ready :recover-multiaccount)
            (assoc :multiaccounts/new-installation-id (random-guid-generator)))}
   (node/initialize nil)))

(fx/defn navigate-to-recover-multiaccount-screen [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (-> db
                     (assoc-in [:multiaccounts/recover :recovery-enter-passphrase-screen] (:recovery-enter-passphrase-screen cofx))
                     (assoc-in [:multiaccounts/recover :recovery-trial-success-screen] (:recovery-trial-success-screen cofx))
                     (assoc-in [:multiaccounts/recover :name] (:name cofx))
                     (assoc-in [:multiaccounts/recover :address] (:address cofx))
                     (assoc-in [:multiaccounts/recover :masked-passphrase] (:masked-passphrase cofx))
                     (assoc-in [:multiaccounts/recover :photo-path] (:photo-path cofx))
                     (assoc-in [:multiaccounts/recover :error] (:error cofx)))}
            (navigation/navigate-to-cofx :recover nil)))

(fx/defn navigate-to-recover-multiaccount-password-screen [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc db :intro-wizard {:step :create-code
                                          :weak-password? true
                                          :encrypt-with-password? true
                                          :accounts (:accounts cofx)
                                          :recovery? true
                                          :masked-passphrase (cofx :masked-passphrase)})}
            (navigation/navigate-to-cofx :intro-wizard nil)))

(re-frame/reg-fx
 :multiaccounts.recover/recover-multiaccount
 (fn [[masked-passphrase password]]
   (recover-multiaccount! masked-passphrase password)))
