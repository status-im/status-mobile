(ns status-im.node.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.fleet.core :as fleet]
            [status-im.native-module.core :as status]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as utils.handlers]
            [status-im.utils.platform :as utils.platform]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]))

(defn- add-custom-bootnodes [config network all-bootnodes]
  (let [bootnodes (as-> all-bootnodes $
                    (get $ network)
                    (vals $)
                    (map :address $))]
    (if (seq bootnodes)
      (assoc-in config [:ClusterConfig :BootNodes] bootnodes)
      config)))

(defn- add-log-level [config log-level]
  (if (empty? log-level)
    (assoc config
           :LogLevel "ERROR"
           :LogEnabled false)
    (assoc config
           :LogLevel log-level
           :LogEnabled true)))

(defn get-network-genesis-hash-prefix
  "returns the hex representation of the first 8 bytes of a network's genesis hash"
  [network]
  (case network
    1 "d4e56740f876aef8"
    3 "41941023680923e0"
    4 "6341fd3daf94b748"
    nil))

(defn get-les-topic
  "returns discovery v5 topic derived from genesis of the provided network"
  [network]
  (let [les-discovery-identifier "LES2@"
        hash-prefix (get-network-genesis-hash-prefix network)]
    (when hash-prefix
      (str les-discovery-identifier hash-prefix))))

(defn get-topics
  [network]
  (let [les-topic (get-les-topic network)]
    (cond-> {"whisper" {:Min 2, :Max 2}}
      les-topic (assoc les-topic {:Min 2, :Max 2}))))

(defn- get-base-node-config [config]
  (let [initial-props @(re-frame/subscribe [:initial-props])
        status-node-port (get initial-props :STATUS_NODE_PORT)]
    (cond-> (assoc config
                   :Name "StatusIM")
      config/dev-build?
      (assoc :ListenAddr ":30304"
             :DataDir (str (:DataDir config) "_dev"))
      status-node-port
      (assoc :ListenAddr (str ":" status-node-port)))))

(defn- pick-nodes
  "Picks `limit` different nodes randomly from the list of nodes
  if there is more than `limit` nodes in the list, otherwise return the list
  of nodes"
  [limit nodes]
  (take limit (shuffle nodes)))

(defn get-log-level
  [multiaccount-settings]
  (or (:log-level multiaccount-settings)
      (if utils.platform/desktop? ""
          config/log-level-status-go)))

(defn- ulc-network? [config]
  (get-in config [:LightEthConfig :ULC] false))

(defn- add-ulc-trusted-nodes [config trusted-nodes]
  (if (ulc-network? config)
    (-> config
        (assoc-in [:LightEthConfig :TrustedNodes] trusted-nodes)
        (assoc-in [:LightEthConfig :MinTrustedFraction] 50))
    config))

(defn- get-multiaccount-node-config
  [{:keys [multiaccount :networks/networks :networks/current-network]
    :or {current-network config/default-network
         networks constants/default-networks}
    :as db}]
  (let [current-fleet-key (fleet/current-fleet db)
        current-fleet (get (fleet/fleets db) current-fleet-key)
        rendezvous-nodes (pick-nodes 3 (vals (:rendezvous current-fleet)))
        {:keys [installation-id settings bootnodes]
         :or {settings constants/default-multiaccount-settings}} multiaccount
        use-custom-bootnodes (get-in settings [:bootnodes current-network])
        log-level (get-log-level settings)
        datasync? (:datasync? settings)
        disable-discovery-topic? (:disable-discovery-topic? settings)
        v1-messages? (:v1-messages? settings)]
    (cond-> (get-in networks [current-network :config])
      :always
      (get-base-node-config)

      current-fleet
      (assoc :NoDiscovery   false
             :Rendezvous    (not (empty? rendezvous-nodes))
             :ClusterConfig {:Enabled true
                             :Fleet              (name current-fleet-key)
                             :BootNodes          (pick-nodes 4 (vals (:boot current-fleet)))
                             :TrustedMailServers (pick-nodes 6 (vals (:mail current-fleet)))
                             :StaticNodes        (into (pick-nodes 2 (vals (:whisper current-fleet))) (vals (:static current-fleet)))
                             :RendezvousNodes    rendezvous-nodes})

      :always
      (assoc :WalletConfig {:Enabled true}
             :BrowsersConfig {:Enabled true}
             :PermissionsConfig {:Enabled true}
             :WhisperConfig           {:Enabled true
                                       :LightClient true
                                       :MinimumPoW 0.001
                                       :EnableNTPSync true}
             :ShhextConfig        {:BackupDisabledDataDir      (utils.platform/no-backup-directory)
                                   :InstallationID             installation-id
                                   :MaxMessageDeliveryAttempts config/max-message-delivery-attempts
                                   :MailServerConfirmations    config/mailserver-confirmations-enabled?
                                   :DataSyncEnabled (boolean datasync?)
                                   :DisableGenericDiscoveryTopic (boolean disable-discovery-topic?)
                                   :SendV1Messages (boolean v1-messages?)
                                   :PFSEnabled              true}
             :RequireTopics           (get-topics current-network)
             :StatusAccountsConfig {:Enabled true})

      (and
       config/bootnodes-settings-enabled?
       use-custom-bootnodes)
      (add-custom-bootnodes current-network bootnodes)

      :always
      (add-ulc-trusted-nodes (vals (:static current-fleet)))

      :always
      (add-log-level log-level))))

(defn get-verify-multiaccount-config
  "Is used when the node has to be started before
  `VerifyAccountPassword` call."
  [db network]
  (-> (get-in (:networks/networks db) [network :config])
      (get-base-node-config)

      (assoc :ShhextConfig {:BackupDisabledDataDir (utils.platform/no-backup-directory)})
      (assoc :PFSEnabled false
             :NoDiscovery true)
      (add-log-level config/log-level-status-go)))

(fx/defn update-sync-state
  [{:keys [db]} error sync-state]
  {:db (assoc db :node/chain-sync-state
              (if error
                {:error error}
                (when sync-state (js->clj sync-state :keywordize-keys true))))})

(defn get-new-config
  [db]
  (types/clj->json (get-multiaccount-node-config db)))

(fx/defn save-new-config
  {:events [::save-new-config]}
  [{:keys [db]} config {:keys [on-success]}]
  {::json-rpc/call [{:method "settings_saveNodeConfig"
                     :params [config]
                     :on-success on-success}]})

(fx/defn prepare-new-config
  [{:keys [db]} {:keys [on-success]}]
  {::prepare-new-config [(get-new-config db)
                         #(re-frame/dispatch [::save-new-config % {:on-success on-success}])]})

(re-frame/reg-fx
 ::prepare-new-config
 (fn [[config callback]]
   (status/prepare-dir-and-update-config config callback)))

(re-frame/reg-fx
 :node/ready
 (fn [config]
   (status/node-ready)))

(re-frame/reg-fx
 :node/les-show-debug-info
 (fn [[multiaccount]]
   #_(.getBalance
      (.-eth eb3)
      (:address multiaccount)
      (fn [error-balance balance]
        (.getBlockNumber
         (.-eth eb3)
         (fn
           [error-block block]
           (utils/show-popup
            "LES sync status"
            (str
             "* multiaccount="   (:address multiaccount) "\n"
             "* latest block="   (or error-block block) "\n"
             "* balance="        (or error-balance balance) "\n"
             "* eth_getSyncing=" (or chain-sync-state "false")))))))))

(defn display-les-debug-info
  [{{:keys [multiaccount]} :db}]
  {:node/les-show-debug-info [multiaccount]})
