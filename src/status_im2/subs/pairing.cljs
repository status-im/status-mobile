(ns status-im2.subs.pairing
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
 :pairing/enabled-installations
 :<- [:pairing/installations]
 (fn [installations]
   (filter :enabled? installations)))

(re-frame/reg-sub
 :pairing/installation-id
 :<- [:profile/profile]
 (fn [multiaccount] (:installation-id multiaccount)))

(re-frame/reg-sub
 :pairing/installation-name
 :<- [:profile/profile]
 (fn [multiaccount] (:installation-name multiaccount)))

(re-frame/reg-sub
 :pairing/pairing-status
 :<- [:syncing]
 (fn [syncing]
   (:pairing-status syncing)))
