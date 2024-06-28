(ns status-im.contexts.syncing.events
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [react-native.platform :as platform]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [status-im.contexts.syncing.utils :as sync-utils]
    [taoensso.timbre :as log]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

(rf/defn local-pairing-update-role
  {:events [:syncing/update-role]}
  [{:keys [db]} role]
  {:db (assoc-in db [:syncing :role] role)})

(rf/defn local-pairing-clear-states
  {:events [:syncing/clear-states]}
  [{:keys [db]} role]
  {:db (dissoc db :syncing)})

(defn- get-default-node-config
  []
  (let [log-level  config/log-level
        log-config (if (empty? log-level)
                     {:LogLevel "ERROR" :LogEnabled false}
                     {:LogLevel log-level :LogEnabled true})]
    (merge {:WalletConfig (cond-> {:Enabled true}
                            (not= config/opensea-api-key "")
                            (assoc :OpenseaAPIKey config/opensea-api-key))
            :WakuV2Config {;; Temporary fix until https://github.com/status-im/status-go/issues/3024
                           ;; is resolved
                           :Nameserver  "8.8.8.8"
                           :LightClient false}
            :ShhextConfig {:VerifyTransactionURL     config/verify-transaction-url
                           :VerifyENSURL             config/verify-ens-url
                           :VerifyENSContractAddress config/verify-ens-contract-address
                           :VerifyTransactionChainID config/verify-transaction-chain-id}}
           log-config)))

(defn- input-connection-string-callback
  [res]
  (log/info "[local-pairing] input-connection-string-for-bootstrapping callback"
            {:response res
             :event    :syncing/input-connection-string-for-bootstrapping})
  (let [error (when (sync-utils/extract-error res)
                (str "generic-error: " res))]
    (when (some? error)
      (rf/dispatch [:toasts/upsert
                    {:type :negative
                     :text error}]))))

(rf/defn preflight-outbound-check-for-local-pairing
  {:events [:syncing/preflight-outbound-check]}
  [_ set-checks-passed]
  (let [callback
        (fn [raw-response]
          (log/info "Local pairing preflight check"
                    {:response raw-response
                     :event    :syncing/preflight-outbound-check})
          (let [^js response-js (transforms/json->js raw-response)
                error           (transforms/js->clj (.-error response-js))]
            (set-checks-passed (empty? error))))]
    (native-module/local-pairing-preflight-outbound-check callback)))

(rf/defn initiate-local-pairing-with-connection-string
  {:events [:syncing/input-connection-string-for-bootstrapping]}
  [{:keys [db]} connection-string]
  (let [default-node-config-string (.stringify js/JSON (clj->js (get-default-node-config)))
        callback
        (fn [final-node-config]
          (let [config-map (.stringify js/JSON
                                       (clj->js
                                        {:receiverConfig
                                         {:kdfIterations config/default-kdf-iterations
                                          :nodeConfig final-node-config
                                          :settingCurrentNetwork config/default-network
                                          :deviceType platform/os
                                          :deviceName
                                          (native-module/get-installation-name)}}))]
            (rf/dispatch [:syncing/update-role constants/local-pairing-role-receiver])
            (native-module/input-connection-string-for-bootstrapping
             connection-string
             config-map
             input-connection-string-callback)))]
    (native-module/prepare-dir-and-update-config "" default-node-config-string callback)))

(rf/defn preparations-for-connection-string
  {:events [:syncing/get-connection-string]}
  [{:keys [db] :as cofx} sha3-pwd on-valid-connection-string]
  (let [error             (get-in db [:profile/login :error])
        handle-connection (fn [response]
                            (when (sync-utils/valid-connection-string? response)
                              (on-valid-connection-string response)
                              (rf/dispatch [:syncing/update-role constants/local-pairing-role-sender])
                              (rf/dispatch [:hide-bottom-sheet])))]
    (when-not (and error (string/blank? error))
      (let [key-uid    (get-in db [:profile/profile :key-uid])
            config-map (.stringify js/JSON
                                   (clj->js {:senderConfig {:keyUID       key-uid
                                                            :keystorePath ""
                                                            :password     (security/safe-unmask-data
                                                                           sha3-pwd)
                                                            :deviceType   platform/os}
                                             :serverConfig {:timeout 0}}))]
        (native-module/get-connection-string-for-bootstrapping-another-device
         config-map
         handle-connection)))))

(rf/reg-event-fx
 :syncing/enable-installation
 (fn [_ [installation-id]]
   {:fx [[:json-rpc/call
          [{:method     "wakuext_enableInstallation"
            :params     [installation-id]
            :on-success #(rf/dispatch [:syncing/on-enable-installation-success installation-id])
            :on-error   #(rf/dispatch [:syncing/on-toggle-installation-failed % installation-id])}]]]}))

(rf/reg-event-fx
 :syncing/disable-installation
 (fn [_ [installation-id]]
   {:fx [[:json-rpc/call
          [{:method     "wakuext_disableInstallation"
            :params     [installation-id]
            :on-success #(rf/dispatch [:syncing/on-disable-installation-success installation-id])
            :on-error   #(rf/dispatch [:syncing/on-toggle-installation-failed % installation-id])}]]]}))

(rf/reg-event-fx
 :syncing/on-enable-installation-success
 (fn [{:keys [db]} [installation-id]]
   {:db (assoc-in db
         [:pairing/installations installation-id :enabled?]
         true)
    :fx [[:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/pair-device-toast)}]]]}))

(rf/reg-event-fx
 :syncing/on-disable-installation-success
 (fn [{:keys [db]} [installation-id]]
   {:db (assoc-in db
         [:pairing/installations installation-id :enabled?]
         false)
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch
          [:toasts/upsert
           {:type  :positive
            :theme :dark
            :text  (i18n/label :t/unpair-device-toast)}]]]}))

(rf/reg-event-fx
 :syncing/on-toggle-installation-failed
 (fn [_ [error installation-id]]
   (log/error "Failed to toggle installation"
              {:error           error
               :installation-id installation-id})))
