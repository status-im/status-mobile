(ns status-im2.subs.pairing
  (:require [re-frame.core :as re-frame]
            [status-im.pairing.core :as pairing]))

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
 :<- [:multiaccount]
 (fn [multiaccount] (:installation-id multiaccount)))

(re-frame/reg-sub
 :pairing/installation-name
 :<- [:multiaccount]
 (fn [multiaccount] (:installation-name multiaccount)))

(re-frame/reg-sub
 :pairing/pairing-in-progress
 :<- [:syncing]
 (fn [syncing]
   (:pairing-in-progress? syncing)))