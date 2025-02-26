(ns status-im.infra.transform
  (:require
    [camel-snake-kebab.extras :as cske]
    [clojure.set :as set]
    [clojure.string :as string]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.collectible.utils :as collectible-utils]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [utils.collection :as utils.collection]
    [utils.money :as money]
    [utils.number :as utils.number]
    [utils.transforms :as transforms]))

(defn- add-keys-to-saved-address
  [saved-address]
  (assoc saved-address :ens? (not (string/blank? (:ens saved-address)))))

(defn rpc->saved-address
  [saved-address]
  (-> saved-address
      (set/rename-keys {:chainShortNames  :chain-short-names
                        :isTest           :test?
                        :createdAt        :created-at
                        :colorId          :customization-color
                        :mixedcaseAddress :mixedcase-address
                        :removed          :removed?})
      (update :customization-color (comp keyword string/lower-case))
      add-keys-to-saved-address))

(defn rpc->saved-addresses
  [saved-addresses]
  (map rpc->saved-address saved-addresses))
