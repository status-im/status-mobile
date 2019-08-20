(ns status-im.protocol.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.i18n :as i18n]
            [status-im.mailserver.core :as mailserver]
            [status-im.node.core :as node]
            [status-im.transport.core :as transport]
            [status-im.tribute-to-talk.core :as tribute-to-talk]
            [status-im.utils.fx :as fx]
            [status-im.utils.semaphores :as semaphores]
            [status-im.utils.utils :as utils]))

(fx/defn update-sync-state
  [{{:keys [sync-state sync-data] :as db} :db} error sync]
  (let [{:keys [highestBlock currentBlock] :as state}
        (js->clj sync :keywordize-keys true)
        syncing?  (> (- highestBlock currentBlock) constants/blocks-per-hour)
        new-state (cond
                    error :offline
                    syncing? (if (= sync-state :done)
                               :pending
                               :in-progress)
                    :else (if (or (= sync-state :done)
                                  (= sync-state :pending))
                            :done
                            :synced))]
    {:db (cond-> db
           (and (not= sync-data state) (= :in-progress new-state))
           (assoc :sync-data state)
           (not= sync-state new-state)
           (assoc :sync-state new-state))}))

(fx/defn update-syncing-progress
  {:events [:ethereum.callback/get-syncing-success]}
  [cofx error sync]
  (fx/merge cofx
            (update-sync-state error sync)
            (node/update-sync-state error sync)))

(fx/defn check-sync-state
  [{:keys [db] :as cofx}]
  (if (:multiaccount db)
    {::json-rpc/call
     [{:method "eth_syncing"
       :on-success
       (fn [sync]
         (re-frame/dispatch [:ethereum.callback/get-syncing-success nil sync]))
       :on-error
       (fn [error]
         (re-frame/dispatch [:ethereum.callback/get-syncing-success error nil]))}]
     :utils/dispatch-later  [{:ms       10000
                              :dispatch [:protocol/state-sync-timed-out]}]}
    (semaphores/free cofx :check-sync-state?)))

(fx/defn start-check-sync-state
  [{{:keys [network multiaccount] :as db} :db :as cofx}]
  (when (and (not (semaphores/locked? cofx :check-sync-state?))
             (not (ethereum/network-with-upstream-rpc?
                   (get-in multiaccount [:networks/networks network]))))
    (fx/merge cofx
              (check-sync-state)
              (semaphores/lock :check-sync-state?))))

(fx/defn initialize-protocol
  [{:data-store/keys [mailserver-topics mailservers]
    :keys [db] :as cofx}]
  (let [network (get-in db [:multiaccount :network])
        network-id (str (get-in db [:multiaccount :networks/networks network :config :NetworkId]))]
    (fx/merge cofx
              {:db (assoc db
                          :rpc-url constants/ethereum-rpc-url
                          :mailserver/topics {})
               ::json-rpc/call
               [{:method "net_version"
                 :on-success
                 (fn [fetched-network-id]
                   ;;FIXME
                   #_(when (not= network-id fetched-network-id) 3
                           (utils/show-popup
                            (i18n/label :t/ethereum-node-started-incorrectly-title)
                            (i18n/label :t/ethereum-node-started-incorrectly-description
                                        {:network-id         network-id
                                         :fetched-network-id fetched-network-id})
                            #(re-frame/dispatch [:protocol.ui/close-app-confirmed]))))}]}
              (tribute-to-talk/init)
              (start-check-sync-state)
              (mailserver/initialize-ranges)
              (mailserver/initialize-mailserver mailservers)
              (transport/init-whisper))))

(fx/defn handle-close-app-confirmed
  [_]
  {:ui/close-application nil})
