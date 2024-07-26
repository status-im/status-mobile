(ns status-im.contexts.centralized-metrics.events
  (:require
    [native-module.core :as native-module]
    [re-frame.interceptor :as interceptor]
    status-im.contexts.centralized-metrics.effects
    [status-im.contexts.centralized-metrics.tracking :as tracking]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(def ^:const user-confirmed-key :centralized-metrics/user-confirmed?)
(def ^:const enabled-key :centralized-metrics/enabled?)

(defn show-confirmation-modal?
  [db]
  (not (user-confirmed-key db)))

(defn push-event?
  [db]
  (or (not (user-confirmed-key db))
      (enabled-key db)))

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
               user-confirmed-key
               true
               enabled-key
               enabled?)}))

(rf/reg-event-fx :centralized-metrics/check-modal
 (fn [{:keys [db]} [modal-view]]
   (when (show-confirmation-modal? db)
     {:fx [[:dispatch
            [:show-bottom-sheet
             {:content (fn [] [modal-view])
              :shell?  true}]]]})))

