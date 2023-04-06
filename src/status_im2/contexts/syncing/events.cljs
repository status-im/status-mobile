(ns status-im2.contexts.syncing.events
  (:require [status-im.native-module.core :as status]
            [status-im2.contexts.syncing.sheets.enter-password.view :as sheet]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]
            [utils.security.core :as security]
            [status-im2.config :as config]
            [status-im.node.core :as node]
            [re-frame.core :as re-frame]
            [status-im.data-store.settings :as data-store.settings]
            [status-im.utils.platform :as utils.platform]
            [status-im2.constants :as constants]))

(rf/defn local-pairing-completed
  {:events [:syncing/pairing-completed]}
  [{:keys [db]}]
  (let [receiver? (= (get-in db [:syncing :role]) constants/local-pairing-role-receiver)]
    (merge
     {:db (dissoc db :syncing)}
     (when receiver?
       {:dispatch [:init-root :enable-notifications]}))))

(rf/defn local-pairing-update-role
  {:events [:syncing/update-role]}
  [{:keys [db]} role]
  {:db (assoc-in db [:syncing :role] role)})

(defn- get-default-node-config
  [installation-id]
  (let [db {:networks/current-network config/default-network
            :networks/networks        (data-store.settings/rpc->networks config/default-networks)
            :multiaccount             {:installation-id           installation-id
                                       :log-level                 config/log-level
                                       :waku-bloom-filter-mode    false
                                       :custom-bootnodes          nil
                                       :custom-bootnodes-enabled? false}}]
    (node/get-multiaccount-node-config db)))

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
                                        {:receiverConfig {:kdfIterations config/default-kdf-iterations
                                                          :nodeConfig final-node-config
                                                          :settingCurrentNetwork config/default-network
                                                          :deviceType utils.platform/os}}))]
            (rf/dispatch [:syncing/update-role constants/local-pairing-role-receiver])
            (status/input-connection-string-for-bootstrapping
             connection-string
             config-map
             #(log/info "Initiated local pairing"
                        {:response %
                         :event    :syncing/input-connection-string-for-bootstrapping}))))]
    (status/prepare-dir-and-update-config "" default-node-config-string callback)))

(rf/defn preparations-for-connection-string
  {:events [:syncing/get-connection-string-for-bootstrapping-another-device]}
  [{:keys [db]} entered-password]
  (let [valid-password? (>= (count entered-password) constants/min-password-length)
        show-sheet      (fn [connection-string]
                          (rf/dispatch
                           [:show-bottom-sheet
                            {:content (fn []
                                        [sheet/qr-code-view-with-connection-string
                                         connection-string])}])
                          (rf/dispatch [:syncing/update-role constants/local-pairing-role-sender]))]
    (if valid-password?
      (let [sha3-pwd   (status/sha3 (str (security/safe-unmask-data entered-password)))
            key-uid    (get-in db [:multiaccount :key-uid])
            config-map (.stringify js/JSON
                                   (clj->js {:senderConfig {:keyUID       key-uid
                                                            :keystorePath ""
                                                            :password     sha3-pwd
                                                            :deviceType   utils.platform/os}
                                             :serverConfig {:timeout 0}}))]
        (status/get-connection-string-for-bootstrapping-another-device
         config-map
         #(show-sheet %)))
      (show-sheet ""))))
