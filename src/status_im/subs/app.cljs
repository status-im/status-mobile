(ns status-im.subs.app
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
 :app/last-user-notification
 :<- [:app]
 :-> :last-user-notification)


(rf/reg-sub
 :app/use-cases
 :<- [:app]
 :-> :use-cases-stack)

(rf/reg-sub
 :app/current-use-case
 :<- [:app/use-cases]
 (fn [use-cases]
   (first use-cases)))

(rf/reg-sub
 :app/use-case-active?
 :<- [:app/use-cases]
 (fn [use-cases [_sub-name use-case]]
   (some #(= use-case %) use-cases)))
