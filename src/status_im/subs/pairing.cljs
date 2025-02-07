(ns status-im.subs.pairing
  (:require
    [legacy.status-im.pairing.core :as pairing]
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :pairing/installations
 :<- [:get-pairing-installations]
 :<- [:pairing/installation-id]
 (fn [[installations installation-id]]
   (->> installations
        vals
        (pairing/sort-installations installation-id))))

(re-frame/reg-sub
 :pairing/paired-devices-count
 :<- [:pairing/installations]
 (fn [installations]
   (count (filter :enabled? (rest installations)))))

(re-frame/reg-sub
 :pairing/enabled-installations
 :<- [:pairing/installations]
 (fn [installations]
   (filter :enabled? installations)))

(re-frame/reg-sub
 :pairing/installation-id
 :<- [:profile/profile]
 (fn [multiaccount] (:installation-id multiaccount)))

(re-frame/reg-sub
 :pairing/pairing-status
 :<- [:syncing]
 (fn [syncing]
   (:pairing-status syncing)))

(re-frame/reg-sub
 :pairing/has-paired-devices
 :<- [:pairing/enabled-installations]
 (fn [installations]
   (> (count installations) 1)))
