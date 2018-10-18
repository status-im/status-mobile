(ns status-im.protocol.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.transport.core :as transport]
            [status-im.mailserver.core :as mailserver]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.fx :as fx]
            [status-im.utils.semaphores :as semaphores]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

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

(fx/defn check-sync-state
  [{{:keys [web3] :as db} :db :as cofx}]
  (if (:account/account db)
    {:web3/get-syncing web3
     :dispatch-later    [{:ms 10000 :dispatch [:protocol/state-sync-timed-out]}]}
    (semaphores/free cofx :check-sync-state?)))

(fx/defn start-check-sync-state
  [{{:keys [network account/account] :as db} :db :as cofx}]
  (when (and (not (semaphores/locked? cofx :check-sync-state?))
             (not (ethereum/network-with-upstream-rpc? (get-in account [:networks network]))))
    (fx/merge cofx
              (check-sync-state)
              (semaphores/lock :check-sync-state?))))

(fx/defn initialize-protocol
  [{:data-store/keys [transport mailserver-topics mailservers]
    :keys [db web3] :as cofx} address]
  (let [network (get-in db [:account/account :network])
        network-id (str (get-in db [:account/account :networks network :config :NetworkId]))]
    (fx/merge cofx
              {:db                              (assoc db
                                                       :rpc-url constants/ethereum-rpc-url
                                                       :transport/chats transport
                                                       :mailserver/topics mailserver-topics)
               :protocol/assert-correct-network {:web3 web3
                                                 :network-id network-id}}
              (start-check-sync-state)
              (mailserver/initialize-mailserver mailservers)
              (transport/init-whisper address))))

(fx/defn handle-close-app-confirmed
  [_]
  {:ui/close-application nil})

(re-frame/reg-fx
 :protocol/assert-correct-network
 (fn [{:keys [web3 network-id]}]
   ;; ensure that node was started correctly
   (when (and network-id web3) ; necessary because of the unit tests
     (.getNetwork (.-version web3)
                  (fn [error fetched-network-id]
                    (when (and (not error) ; error most probably means we are offline
                               (not= network-id fetched-network-id))
                      (utils/show-popup
                       (i18n/label :t/ethereum-node-started-incorrectly-title)
                       (i18n/label :t/ethereum-node-started-incorrectly-description
                                   {:network-id         network-id
                                    :fetched-network-id fetched-network-id})
                       #(re-frame/dispatch [:protocol.ui/close-app-confirmed]))))))))
