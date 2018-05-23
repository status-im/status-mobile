(ns status-im.ui.screens.bootnodes-settings.edit-bootnode.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :refer [register-handler] :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]
            [status-im.ui.screens.accounts.utils :as accounts.utils]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [status-im.utils.inbox :as utils.inbox]))

(defn- new-bootnode [id bootnode-name address chain]
  {:address address
   :chain   chain
   :id      (string/replace id "-" "")
   :name    bootnode-name})

(defn save-new-bootnode [{{:bootnodes/keys [manage] :account/keys [account] :as db} :db :as cofx} _]
  (let [{:keys [name url]} manage
        network            (:network db)
        bootnode           (new-bootnode
                            (:random-id cofx)
                            (:value name)
                            (:value url)
                            network)
        new-bootnodes      (assoc-in (:bootnodes account) [network (:id bootnode)] bootnode)]

    (handlers-macro/merge-fx cofx
                             {:db       (dissoc db :bootnodes/manage)
                              :dispatch [:navigate-back]}
                             (accounts.utils/account-update {:bootnodes new-bootnodes}))))

(handlers/register-handler-fx
 :save-new-bootnode
 [(re-frame/inject-cofx :random-id)]
 save-new-bootnode)

(handlers/register-handler-fx
 :bootnode-set-input
 (fn [{db :db} [_ input-key value]]
   {:db (update db :bootnodes/manage assoc input-key {:value value
                                                      :error (if (= input-key :name)
                                                               (string/blank? value)
                                                               (not (utils.inbox/valid-enode-address? value)))})}))

(handlers/register-handler-fx
 :edit-bootnode
 (fn [{db :db} _]
   {:db       (update-in db [:bootnodes/manage] assoc
                         :name  {:error true}
                         :url   {:error true})
    :dispatch [:navigate-to :edit-bootnode]}))
