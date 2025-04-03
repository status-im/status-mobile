(ns legacy.status-im.subs.mailservers
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :mailserver/current-name
 :<- [:mailserver/current-id]
 :<- [:fleets/current-fleet]
 :<- [:mailserver/mailservers]
 (fn [[current-mailserver-id current-fleet mailservers]]
   (get-in mailservers [current-fleet current-mailserver-id :name])))
