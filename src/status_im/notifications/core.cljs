(ns status-im.notifications.core
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]
            [status-im.native-module.core :as status]))

;; FIXME: Repalce with request permission from audio messages PR lib
(re-frame/reg-fx
 ::request-permission
 identity)

(fx/defn request-permission
  {:events [::request-permission]}
  [_]
  {::request-permission true})

(re-frame/reg-fx
 ::local-notification
 (fn [{:keys [title message]}]
   (log/info {:title   title
              :message message})))

(re-frame/reg-fx
 ::enable
 (fn [_]
   (status/enable-notifications)))

(re-frame/reg-fx
 ::disable
 (fn [_]
   (status/disable-notifications)))
