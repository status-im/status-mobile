(ns status-im.multiaccounts.delete.core
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]))

(re-frame/reg-fx
 :multiaccounts/delete
 (fn [address]
   (status/delete-account address)))
