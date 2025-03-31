(ns legacy.status-im.subs.ens
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :ens/current-names
 :<- [:ens/names]
 :<- [:chain-id]
 (fn [[all-names chain-id]]
   (get all-names chain-id)))
