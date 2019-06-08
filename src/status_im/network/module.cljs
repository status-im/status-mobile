(ns status-im.network.module
  (:require-macros [status-im.modules :as modules])
  (:require status-im.network.net-info
            status-im.network.ui.db
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]))

(modules/defmodule network
  {:save                        'status-im.network.core/save
   :connect                     'status-im.network.core/connect
   :delete                      'status-im.network.core/delete
   :get-network-id-for-chain-id 'status-im.network.core/get-network-id-for-chain-id
   :get-network                 'status-im.network.core/get-network
   :remove-network              'status-im.network.core/remove-network
   :edit-network-view           'status-im.network.ui.edit-network.views/edit-network
   :network-settings-view       'status-im.network.ui.views/network-settings
   :network-details-view        'status-im.network.ui.network-details.views/network-details})

(defn save [& args]
  (apply (get-symbol :save) args))

(defn connect [& args]
  (apply (get-symbol :connect) args))

(defn delete [& args]
  (apply (get-symbol :delete) args))

(defn get-network-id-for-chain-id [& args]
  (apply (get-symbol :get-network-id-for-chain-id) args))

(defn get-network [& args]
  (apply (get-symbol :get-network) args))

(defn remove-network [& args]
  (apply (get-symbol :remove-network) args))

(defn edit-network-view []
  [(get-symbol :edit-network-view)])

(defn network-settings-view []
  [(get-symbol :network-settings-view)])

(defn network-details-view []
  [(get-symbol :network-details-view)])

;; Initialization

(re-frame/reg-sub
 :get-network-id
 :<- [:network]
 (fn [network]
   (ethereum/network->chain-id network)))
