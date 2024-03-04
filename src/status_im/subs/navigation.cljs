(ns status-im.subs.navigation
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :navigation/current-screen-id
 :<- [:view-id]
 :<- [:modal-view-ids]
 (fn [[view-id modal-view-ids]]
   (or (some-> modal-view-ids
               last
               last)
       view-id)))
