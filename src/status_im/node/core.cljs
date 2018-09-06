(ns status-im.node.core
  (:require [re-frame.core :as re-frame]
            [status-im.fleet.core :as fleet]
            [status-im.native-module.core :as status]
            [status-im.utils.config :as config]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(defn- add-custom-bootnodes [config network all-bootnodes]
  (let [bootnodes (as-> all-bootnodes $
                    (get $ network)
                    (vals $)
                    (map :address $))]
    (if (seq bootnodes)
      (assoc config :ClusterConfig {:Enabled   true
                                    :BootNodes bootnodes})
      config)))

(defn- add-log-level [config log-level]
  (if (empty? log-level)
    (assoc config
           :LogEnabled false)
    (assoc config
           :LogLevel log-level
           :LogEnabled true)))

(defn get-account-network [db address]
  (get-in db [:accounts/accounts address :network]))

(defn- get-account-node-config [db address]
  (let [accounts (get db :accounts/accounts)
        {:keys [network
                settings
                bootnodes
                networks]} (get accounts address)
        use-custom-bootnodes (get-in settings [:bootnodes network])
        log-level (or (:log-level settings)
                      config/log-level-status-go)]
    (cond-> (get-in networks [network :config])
      (and
       config/bootnodes-settings-enabled?
       use-custom-bootnodes)
      (add-custom-bootnodes network bootnodes)

      :always
      (add-log-level log-level))))

(defn get-node-config [db network]
  (-> (get-in (:networks/networks db) [network :config])
      (add-log-level config/log-level-status-go)))

(defn start
  ([cofx]
   (start nil cofx))
  ([address {:keys [db]}]
   (let [network     (if address
                       (get-account-network db address)
                       (:network db))
         node-config (if address
                       (get-account-node-config db address)
                       (get-node-config db network))
         node-config-json (types/clj->json node-config)
         fleet (name (fleet/current-fleet db address))]
     (log/info "Node config: " node-config-json)
     {:db         (assoc db :network network)
      :node/start [node-config-json fleet]})))

(defn restart
  []
  {:node/stop nil})

(defn initialize
  [address {{:keys [status-node-started?] :as db} :db :as cofx}]
  (if (not status-node-started?)
    (start address cofx)
    (restart)))

(re-frame/reg-fx
 :node/start
 (fn [[config fleet]]
   (status/start-node config fleet)))

(re-frame/reg-fx
 :node/stop
 (fn []
   (status/stop-node)))
