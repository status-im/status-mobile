(ns status-im.subs.app
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
 :app/last-user-notification
 :<- [:app]
 :-> :last-user-notification)
