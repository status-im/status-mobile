(ns status-im.common.app-monitoring.events
  (:require
    status-im.common.app-monitoring.effects
    [utils.re-frame :as rf]))

(rf/reg-event-fx :app-monitoring/intended-panic
 (fn [_ [message]]
   {:fx [[:effects.app-monitoring/intended-panic message]]}))
