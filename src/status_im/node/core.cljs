(ns status-im.node.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.fleet.core :as fleet]
            [status-im.native-module.core :as status]
            [status-im.utils.config :as config]
            [status-im.utils.types :as types]
            [status-im.utils.platform :as utils.platform]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]))

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

(defn get-multiaccount-network [db address]
  (get-in db [:multiaccount address :network]))

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

(defn- get-multiaccount-node-config [db address]
  (let [multiaccounts (get db :multiaccounts/multiaccounts)
        current-fleet-key (fleet/current-fleet db address)
        current-fleet (get (fleet/fleets db) current-fleet-key)
        rendezvous-nodes (pick-nodes 3 (vals (:rendezvous current-fleet)))
        {:keys [network installation-id settings bootnodes networks]}
        (merge
         {:network         config/default-network
          :networks        (:networks/networks db)
          :settings        (constants/default-multiaccount-settings)
          :installation-id (get db :multiaccounts/new-installation-id)}
         (get multiaccounts address))
        use-custom-bootnodes (get-in settings [:bootnodes network])
        log-level (get-log-level settings)]
    (cond-> (get-in networks [network :config])
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
                                   :PFSEnabled              true}
             :RequireTopics           (get-topics network))

      (and
       config/bootnodes-settings-enabled?
       use-custom-bootnodes)
      (add-custom-bootnodes network bootnodes)

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

(fx/defn start
  [{:keys [db]} address]
  (let [network     (if address
                      (get-multiaccount-network db address)
                      (:network db))
        node-config (if (= (:node/on-ready db) :verify-multiaccount)
                      (get-verify-multiaccount-config db network)
                      (get-multiaccount-node-config db address))
        node-config-json (types/clj->json node-config)]
    (log/info "Node config: " node-config-json)
    {:db        (assoc db
                       :network network
                       :node/status :starting)
     :node/start node-config-json}))

(fx/defn stop
  [{:keys [db]}]
  {:db        (assoc db :node/status :stopping)
   :node/stop nil})

(fx/defn initialize
  [{{:node/keys [status] :as db} :db :as cofx} address]
  (let [restart {:db (assoc db :node/restart? true :node/address address)}]
    (case status
      :started nil
      :starting restart
      :stopping restart
      (start cofx address))))

(re-frame/reg-fx
 :node/start
 (fn [config]
   (status/start-node config)))

(re-frame/reg-fx
 :node/ready
 (fn [config]
   (status/node-ready)))

(re-frame/reg-fx
 :node/stop
 (fn []
   (status/stop-node)))

(re-frame/reg-fx
 :node/les-show-debug-info
 (fn [[web3 multiaccount chain-sync-state]]
   (.getBalance
    (.-eth web3)
    (:address multiaccount)
    (fn [error-balance balance]
      (.getBlockNumber
       (.-eth web3)
       (fn
         [error-block block]
         (utils/show-popup
          "LES sync status"
          (str
           "* multiaccount="        (:address multiaccount) "\n"
           "* latest block="   (or error-block block) "\n"
           "* balance="        (or error-balance balance) "\n"
           "* eth_getSyncing=" (or chain-sync-state "false")))))))))

(defn display-les-debug-info
  [{{:keys [web3 multiaccount] :node/keys [chain-sync-state]} :db}]
  {:node/les-show-debug-info [web3 multiaccount]})
