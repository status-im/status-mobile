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

(defn get-account-network [db address]
  (get-in db [:accounts/accounts address :network]))

(defn- get-base-node-config [config]
  (cond-> (assoc config
                 :Name "StatusIM"
                 :BackupDisabledDataDir (utils.platform/no-backup-directory))
    config/dev-build?
    (assoc :ListenAddr ":30304"
           :DataDir (str (:DataDir config) "_dev"))))

(defn- pick-nodes
  "Picks `limit` different nodes randomly from the list of nodes
  if there is more than `limit` nodes in the list, otherwise return the list
  of nodes"
  [limit nodes]
  (take limit (shuffle nodes)))

(defn- get-account-node-config [db address]
  (let [accounts (get db :accounts/accounts)
        current-fleet-key (fleet/current-fleet db address)
        current-fleet (get fleet/fleets current-fleet-key)
        rendezvous-nodes (pick-nodes 3 (vals (:rendezvous current-fleet)))
        {:keys [network
                installation-id
                settings
                bootnodes
                networks]} (get accounts address)
        use-custom-bootnodes (get-in settings [:bootnodes network])
        log-level (or (:log-level settings)
                      config/log-level-status-go)]
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
                             :StaticNodes        (pick-nodes 2 (vals (:whisper current-fleet)))
                             :RendezvousNodes    rendezvous-nodes})

      :always
      (assoc :WhisperConfig         {:Enabled true
                                     :LightClient true
                                     :MinimumPoW 0.001
                                     :EnableNTPSync true}
             :RequireTopics         (get-topics network)
             :InstallationID        installation-id
             :PFSEnabled            (or config/pfs-encryption-enabled?
                                        ;; We don't check dev-mode? here as
                                        ;; otherwise we would have to restart the node
                                        ;; when the user enables it
                                        (config/group-chats-enabled? true)
                                        (config/pairing-enabled? true)))

      (and
       config/bootnodes-settings-enabled?
       use-custom-bootnodes)
      (add-custom-bootnodes network bootnodes)

      :always
      (add-log-level log-level))))

(defn get-node-config [db network]
  (-> (get-in (:networks/networks db) [network :config])
      (get-base-node-config)
      (assoc :PFSEnabled false
             :NoDiscovery true)
      (add-log-level config/log-level-status-go)))

(fx/defn update-sync-state
  [{:keys [db]} error sync-state]
  {:db (assoc db :node/chain-sync-state
              (if error
                {:error error}
                (when sync-state (js->clj sync-state :keywordize-keys true))))})

(fx/defn update-block-number
  [{:keys [db]} error block-number]
  (when-not error
    {:db (assoc db :node/latest-block-number block-number)}))

(fx/defn start
  [{:keys [db]} address]
  (let [network     (if address
                      (get-account-network db address)
                      (:network db))
        node-config (if address
                      (get-account-node-config db address)
                      (get-node-config db network))
        node-config-json (types/clj->json node-config)]
    (log/info "Node config: " node-config-json)
    {:db        (assoc db
                       :network network
                       :node/status :starting)
     :node/start node-config-json}))

(defn stop
  [{:keys [db]}]
  {:db        (assoc db :node/status :stopping)
   :node/stop nil})

(fx/defn initialize
  [{{:node/keys [status] :as db} :db :as cofx} address]
  (let [restart {:db (assoc db :node/restart? true :node/address address)}]
    (case status
      :started (stop cofx)
      :starting (do
                  (when utils.platform/desktop?
                    (status/stop-node))
                  restart)
      :stopping restart
      (start cofx address))))

(re-frame/reg-fx
 :node/start
 (fn [config]
   (status/start-node config)))

(re-frame/reg-fx
 :node/stop
 (fn []
   (status/stop-node)))

(re-frame/reg-fx
 :node/les-show-debug-info
 (fn [[web3 account chain-sync-state]]
   (.getBalance
    (.-eth web3)
    (:address account)
    (fn [error-balance balance]
      (.getBlockNumber
       (.-eth web3)
       (fn
         [error-block block]
         (utils/show-popup
          "LES sync status"
          (str
           "* account="        (:address account) "\n"
           "* latest block="   (or error-block block) "\n"
           "* balance="        (or error-balance balance) "\n"
           "* eth_getSyncing=" (or chain-sync-state "false")))))))))

(defn display-les-debug-info [{{:keys [web3] :account/keys [account] :node/keys [chain-sync-state]} :db}]
  {:node/les-show-debug-info [web3 account]})
