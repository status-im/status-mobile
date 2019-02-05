(ns status-im.contact.device-info
  (:require [status-im.utils.config :as config]))

(defn all [{:keys [db]}]
  (filter
   :fcm-token
   (conj
    (->> (:pairing/installations db)
         (vals)
         (filter :enabled?)
         (filter :fcm-token)
         (take config/max-installations)
         (map #(hash-map :id (:installation-id %)
                         :fcm-token (:fcm-token %))))
    {:id (get-in db [:account/account :installation-id])
     :fcm-token (get-in db [:notifications :fcm-token])})))

(defn merge-info [timestamp previous-devices new-devices]
  (reduce (fn [acc {:keys [id] :as new-device}]
            (if (:fcm-token new-device)
              (assoc acc
                     id
                     (assoc new-device :timestamp timestamp))
              acc))
          previous-devices
          new-devices))
