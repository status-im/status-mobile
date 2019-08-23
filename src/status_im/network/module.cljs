(ns status-im.network.module
  (:require-macros [status-im.modules :as modules])
  (:require status-im.network.net-info
            status-im.network.ui.db
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]))

(modules/defmodule network
  {:edit-network-view           'status-im.network.ui.edit-network.views/edit-network
   :network-settings-view       'status-im.network.ui.views/network-settings
   :network-details-view        'status-im.network.ui.network-details.views/network-details})

(defn edit-network-view []
  [(get-symbol :edit-network-view)])

(defn network-settings-view []
  [(get-symbol :network-settings-view)])

(defn network-details-view []
  [(get-symbol :network-details-view)])
