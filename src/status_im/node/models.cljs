(ns status-im.node.models
  (:require [status-im.utils.config :as config]
            [status-im.utils.types :as types]))

(defn- add-custom-bootnodes [config network all-bootnodes]
  (let [bootnodes (as-> all-bootnodes $
                    (get $ network)
                    (vals $)
                    (map :address $))]
    (if (seq bootnodes)
      (assoc config :ClusterConfig {:Enabled   true
                                    :BootNodes bootnodes})
      config)))

(defn get-account-network [db address]
  (get-in db [:accounts/accounts address :network]))

(defn- get-account-node-config [db address]
  (let [accounts (get db :accounts/accounts)
        {:keys [network
                settings
                bootnodes
                networks]} (get accounts address)
        use-custom-bootnodes (get-in settings [:bootnodes network])]
    (cond-> (get-in networks [network :config])
      (and
       config/bootnodes-settings-enabled?
       use-custom-bootnodes)
      (add-custom-bootnodes network bootnodes))))

(defn start
  ([cofx]
   (start nil cofx))
  ([address {:keys [db]}]
   (let [network     (if address
                       (get-account-network db address)
                       (:network db))
         node-config (if address
                       (get-account-node-config db address)
                       (get-in (:networks/networks db) [network :config]))
         node-config-json (types/clj->json node-config)]
     {:db              (assoc db
                              :network network)
      :node/start node-config-json})))

(defn restart
  []
  {:node/stop nil})

(defn initialize
  [address {{:keys [status-node-started?] :as db} :db :as cofx}]
  (if (not status-node-started?)
    (start address cofx)
    (restart)))
