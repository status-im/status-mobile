(ns status-im.node.events
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]
            [status-im.utils.config :as config]))

(re-frame/reg-fx
 :node/start
 (fn [[config fleet]]
   (status/start-node config fleet)))

(re-frame/reg-fx
 :node/stop
 (fn []
   (status/stop-node)))
