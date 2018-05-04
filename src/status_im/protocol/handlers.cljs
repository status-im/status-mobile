(ns status-im.protocol.handlers
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.native-module.core :as status]
            [status-im.transport.message-cache :as message-cache]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.ethereum.core :as utils]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.utils.web3-provider :as web3-provider]
            [status-im.transport.core :as transport]))

;;;; COFX
(re-frame/reg-cofx
 ::get-web3
 (fn [coeffects _]
   (assoc coeffects :web3 (web3-provider/make-web3))))

(re-frame/reg-fx
  ::web3-get-syncing
  (fn [web3]
    (when web3
      (.getSyncing
       (.-eth web3)
       (fn [error sync]
         (re-frame/dispatch [:update-sync-state error sync]))))))

(re-frame/reg-fx
  ::status-init-jail
  (fn []
    (status/init-jail)))

;;; INITIALIZE PROTOCOL
(handlers/register-handler-fx
  :initialize-protocol
  [re-frame/trim-v
   (re-frame/inject-cofx ::get-web3)
   (re-frame/inject-cofx :data-store/transport)]
  (fn [{:data-store/keys [transport] :keys [db web3] :as cofx} [current-account-id ethereum-rpc-url]]
    (handlers-macro/merge-fx cofx
                             {:db (assoc db
                                    :web3 web3
                                    :rpc-url (or ethereum-rpc-url constants/ethereum-rpc-url)
                                    :transport/chats transport)}
                             (transport/init-whisper current-account-id))))

;;; NODE SYNC STATE

(handlers/register-handler-db
  :update-sync-state
  (fn [{:keys [sync-state sync-data] :as db} [_ error sync]]
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
      (cond-> db
              (when (and (not= sync-data state) (= :in-progress new-state)))
              (assoc :sync-data state)
              (when (not= sync-state new-state))
              (assoc :sync-state new-state)))))

(handlers/register-handler-fx
  :check-sync
  (fn [{{:keys [web3]} :db} _]
    {::web3-get-syncing web3
     :dispatch-later    [{:ms 10000 :dispatch [:check-sync]}]}))

(handlers/register-handler-fx
  :initialize-sync-listener
  (fn [{{:keys [sync-listening-started network networks/networks] :as db} :db} _]
    (when (and (not sync-listening-started)
               (not (utils/network-with-upstream-rpc? networks network)))
      {:db       (assoc db :sync-listening-started true)
       :dispatch [:check-sync]})))
