(ns status-im2.contexts.onboarding.events
  (:require
    [utils.re-frame :as rf]
    [re-frame.core :as re-frame]
    [status-im2.config :as config]
    [clojure.string :as string]
    [utils.security.core :as security]
    [status-im.native-module.core :as status]
    [status-im.ethereum.core :as ethereum]))

(re-frame/reg-fx
 ::create-account-and-login
 (fn [request]
   (status/create-account-and-login request)))

(rf/defn on-delete-profile-success
  {:events [:onboarding-2/on-delete-profile-success]}
  [{:keys [db]} key-uid]
  {:db (update-in db [:multiaccounts/multiaccounts] dissoc key-uid)})

(rf/defn profile-data-set
  {:events [:onboarding-2/profile-data-set]}
  [{:keys [db]} onboarding-data]
  {:db       (assoc db :onboarding-2/profile onboarding-data)
   :dispatch [:navigate-to :create-profile-password]})

(rf/defn password-set
  {:events [:onboarding-2/password-set]}
  [{:keys [db]} password]
  {:db       (assoc-in db [:onboarding-2/profile :password] password)
   :dispatch [:navigate-to :enable-biometrics]})

(defn strip-file-prefix
  [path]
  (when path
    (string/replace-first path "file://" "")))

(rf/defn create-account-and-login
  {:events [:onboarding-2/create-account-and-login]}
  [{:keys [db]}]
  (let [{:keys [display-name
                password
                image-path
                color]} (:onboarding-2/profile db)
        log-enabled?    (boolean (not-empty config/log-level))
        request         {:displayName              display-name
                         :password                 (ethereum/sha3 (security/safe-unmask-data password))
                         :imagePath                (strip-file-prefix image-path)
                         :color                    color
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
                         :verifyTransactionChainID config/verify-transaction-chain-id}]
    {::create-account-and-login request
     :dispatch                  [:navigate-to :generating-keys]
     :db                        (-> db
                                    (dissoc :onboarding-2/profile)
                                    (assoc :onboarding-2/new-account? true))}))
