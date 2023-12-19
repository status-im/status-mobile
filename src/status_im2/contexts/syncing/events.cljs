(ns status-im2.contexts.syncing.events
  (:require
    [clojure.string :as string]
    [legacy.status-im.data-store.settings :as data-store.settings]
    [legacy.status-im.node.core :as node]
    [native-module.core :as native-module]
    [quo.foundations.colors :as colors]
    [re-frame.core :as re-frame]
    [react-native.platform :as platform]
    [status-im2.config :as config]
    [status-im2.constants :as constants]
    [status-im2.contexts.syncing.utils :as sync-utils]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
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
  [installation-id]
  (let [db {:networks/current-network config/default-network
            :networks/networks        (data-store.settings/rpc->networks config/default-networks)
            :profile/profile          {:installation-id           installation-id
                                       :device-name               (native-module/get-installation-name)
                                       :log-level                 config/log-level
                                       :waku-bloom-filter-mode    false
                                       :custom-bootnodes          nil
                                       :custom-bootnodes-enabled? false}}]
    (node/get-multiaccount-node-config db)))

(defn- extract-error
  [json-str]
  (-> json-str
      transforms/json->clj
      (get :error "")
      not-empty))

(defn- input-connection-string-callback
  [res]
  (log/info "[local-pairing] input-connection-string-for-bootstrapping callback"
            {:response res
             :event    :syncing/input-connection-string-for-bootstrapping})
  (let [error (when (extract-error res)
                (str "generic-error: " res))]
    (when (some? error)
      (rf/dispatch [:toasts/upsert
                    {:icon       :i/alert
                     :icon-color colors/danger-50
                     :text       error}]))))

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
  {:events       [:syncing/input-connection-string-for-bootstrapping]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)]}
  [{:keys [random-guid-generator db]} connection-string]
  (let [installation-id (random-guid-generator)
        default-node-config (get-default-node-config installation-id)
        default-node-config-string (.stringify js/JSON (clj->js default-node-config))
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
                                                            :password     sha3-pwd
                                                            :deviceType   platform/os}
                                             :serverConfig {:timeout 0}}))]
        (native-module/get-connection-string-for-bootstrapping-another-device
         config-map
         handle-connection)))))
