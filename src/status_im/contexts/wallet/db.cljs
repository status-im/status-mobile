(ns status-im.contexts.wallet.db
  (:require [status-im.constants :as constants]))

(def ^:const network-filter-defaults
  {:selector-state    :default
   :selected-networks (set constants/default-network-names)})

(def ^:const defaults
  {:ui {:network-filter network-filter-defaults}})
