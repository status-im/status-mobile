(ns status-im.contexts.centralized-metrics.events
  (:require
    [native-module.core :as native-module]
    [re-frame.interceptor :as interceptor]
    status-im.contexts.centralized-metrics.effects
    [status-im.contexts.centralized-metrics.tracking :as tracking]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn push-event?
  [db]
  (or (not (:centralized-metrics/user-confirmed? db))
      (:centralized-metrics/enabled? db)))

(defn centralized-metrics-interceptor
  [context]
  (when-let [event (tracking/tracked-event (interceptor/get-coeffect context :event))]
    (log/debug "tracking event" event)
    (when (push-event? (interceptor/get-coeffect context :db))
      (native-module/add-centralized-metric event)))
  context)

(def interceptor
  (interceptor/->interceptor
   :id    :centralized-metrics
   :after centralized-metrics-interceptor))

(rf/reg-event-fx :centralized-metrics/toggle-centralized-metrics
 (fn [{:keys [db]} [enabled?]]
   {:fx [[:effects.centralized-metrics/toggle-metrics enabled?]]
    :db (assoc db
               :centralized-metrics/user-confirmed? true
               :centralized-metrics/enabled?        enabled?)}))
