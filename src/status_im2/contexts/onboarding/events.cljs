(ns status-im2.contexts.onboarding.events
  (:require
    [utils.re-frame :as rf]
    [re-frame.core :as re-frame]
    [status-im.utils.types :as types]
    [status-im2.config :as config]
    [clojure.string :as string]
    [utils.security.core :as security]
    [status-im.native-module.core :as status]
    [status-im.ethereum.core :as ethereum]))

(re-frame/reg-fx
 :multiaccount/create-account-and-login
 (fn [request]
   (status/create-account-and-login request)))

(re-frame/reg-fx
 :multiaccount/validate-mnemonic
 (fn [[mnemonic on-success on-error]]
   (status/validate-mnemonic
    (security/safe-unmask-data mnemonic)
    (fn [result]
      (let [{:keys [error]} (types/json->clj result)]
        (if (seq error)
          (when on-error (on-error error))
          (on-success mnemonic)))))))

(re-frame/reg-fx
 :multiaccount/restore-account-and-login
 (fn [request]
   (status/restore-account-and-login request)))

(rf/defn profile-data-set
  {:events [:onboarding-2/profile-data-set]}
  [{:keys [db]} onboarding-data]
  {:db       (update db :onboarding-2/profile merge onboarding-data)
   :dispatch [:navigate-to :create-profile-password]})

(defn strip-file-prefix
  [path]
  (when path
    (string/replace-first path "file://" "")))

(rf/defn create-account-and-login
  {:events [:onboarding-2/create-account-and-login]}
  [{:keys [db]}]
  (let [{:keys [display-name
                seed-phrase
                password
                image-path
                color]} (:onboarding-2/profile db)
        log-enabled?    (boolean (not-empty config/log-level))
        effect          (if seed-phrase
                          :multiaccount/restore-account-and-login
                          :multiaccount/create-account-and-login)
        request         {:displayName              display-name
                         :password                 (ethereum/sha3 (security/safe-unmask-data password))
                         :mnemonic                 (when seed-phrase
                                                     (security/safe-unmask-data seed-phrase))
                         :imagePath                (strip-file-prefix image-path)
                         :customizationColor       color
                         :backupDisabledDataDir    (status/backup-disabled-data-dir)
                         :rootKeystoreDir          (status/keystore-dir)
                         ;; Temporary fix until https://github.com/status-im/status-go/issues/3024 is
                         ;; resolved
                         :wakuV2Nameserver         "1.1.1.1"
                         :logLevel                 (when log-enabled? config/log-level)
                         :logEnabled               log-enabled?
                         :logFilePath              (status/log-file-path)
                         :openseaAPIKey            config/opensea-api-key
                         :verifyTransactionURL     config/verify-transaction-url
                         :verifyENSURL             config/verify-ens-url
                         :verifyENSContractAddress config/verify-ens-contract-address
                         :verifyTransactionChainID config/verify-transaction-chain-id
                         :previewPrivacy           config/blank-preview?}]
    {effect    request
     :dispatch [:navigate-to :generating-keys]
     :db       (-> db
                   (dissoc :onboarding-2/profile)
                   (assoc :onboarding-2/new-account? true))}))

(rf/defn on-delete-profile-success
  {:events [:onboarding-2/on-delete-profile-success]}
  [{:keys [db]} key-uid]
  {:db (update-in db [:multiaccounts/multiaccounts] dissoc key-uid)})

(rf/defn password-set
  {:events [:onboarding-2/password-set]}
  [{:keys [db]} password]
  {:db       (assoc-in db [:onboarding-2/profile :password] password)
   :dispatch [:navigate-to :enable-biometrics]})

(rf/defn seed-phrase-entered
  {:events [:onboarding-2/seed-phrase-entered]}
  [_ seed-phrase on-error]
  {:multiaccount/validate-mnemonic [seed-phrase
                                    #(re-frame/dispatch [:onboarding-2/seed-phrase-validated
                                                         seed-phrase])
                                    on-error]})

(rf/defn seed-phrase-validated
  {:events [:onboarding-2/seed-phrase-validated]}
  [{:keys [db]} seed-phrase]
  {:db       (assoc-in db [:onboarding-2/profile :seed-phrase] seed-phrase)
   :dispatch [:navigate-to :create-profile]})

(rf/defn navigate-to-create-profile
  {:events [:onboarding-2/navigate-to-create-profile]}
  [{:keys [db]}]
  ;; Restart the flow
  {:db       (dissoc db :onboarding-2/profile)
   :dispatch [:navigate-to :create-profile]})
