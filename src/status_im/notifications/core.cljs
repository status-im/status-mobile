(ns status-im.notifications.core
  (:require [re-frame.core :as re-frame]
            [status-im.native-module.core :as status]))

(re-frame/reg-fx
 ::enable
 (fn [_]
   (status/enable-notifications)))

(re-frame/reg-fx
 ::disable
 (fn [_]
   (status/disable-notifications)))
