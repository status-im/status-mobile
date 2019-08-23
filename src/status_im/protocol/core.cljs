(ns status-im.protocol.core
  (:require [status-im.constants :as constants]
            [status-im.mailserver.core :as mailserver]
            [status-im.node.core :as node]
            [status-im.transport.core :as transport]
            [status-im.tribute-to-talk.core :as tribute-to-talk]
            [status-im.utils.fx :as fx]))

;;TODO move this logic to status-go
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

(fx/defn initialize-protocol
  [{:data-store/keys [mailserver-topics mailservers] :keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc db
                        :rpc-url constants/ethereum-rpc-url
                        :mailserver/topics mailserver-topics)}
            (tribute-to-talk/init)
            (mailserver/initialize-ranges)
            (mailserver/initialize-mailserver mailservers)
            (transport/init-whisper)))
