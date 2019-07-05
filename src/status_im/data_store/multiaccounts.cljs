(ns status-im.data-store.multiaccounts
  (:require [re-frame.core :as re-frame]
            [status-im.utils.types :as types]))

;; TODO janherich: define as cofx once debug handlers are refactored
(defn get-by-address [address])

(defn- deserialize-bootnodes [bootnodes]
  (reduce-kv
   (fn [acc id {:keys [chain] :as bootnode}]
     (assoc-in acc [chain id] bootnode))
   {}
   bootnodes))

(defn- deserialize-networks [networks]
  (reduce-kv
   (fn [acc network-id props]
     (assoc acc network-id (update props :config types/json->clj)))
   {}
   networks))

(defn- deserialize-extensions [extensions])

(defn- deserialize-multiaccount [multiaccount])

(defn- serialize-bootnodes [bootnodes]
  (->> bootnodes
       vals
       (mapcat vals)))

(defn- serialize-networks [networks]
  (map (fn [[_ props]]
         (update props :config types/clj->json))
       networks))

(defn- serialize-extensions [extensions])

(defn- serialize-multiaccount [multiaccount])

(defn save-multiaccount-tx
  "Returns tx function for saving multiaccount"
  [multiaccount]
  (fn [realm]))

(defn delete-multiaccount-tx
  "Returns tx function for deleting multiaccount"
  [address]
  (fn [realm]))

(re-frame/reg-cofx
 :data-store/get-all-multiaccounts
 (fn [cofx _]
   (assoc cofx :all-multiaccounts [])))
