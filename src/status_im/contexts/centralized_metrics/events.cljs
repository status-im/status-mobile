(ns status-im.contexts.centralized-metrics.events
  (:require
    [native-module.core :as native-module]
    [re-frame.interceptor :as interceptor]
    status-im.contexts.centralized-metrics.effects
    [status-im.contexts.centralized-metrics.tracking :as tracking]
    [utils.re-frame :as rf]))

(defn push-event?
  [db]
  (or (not (:centralized-metrics/user-confirmed? db))
      (:centralized-metrics/enabled? db)))

(defn centralized-metrics-interceptor
  [context]
  (when (push-event? (interceptor/get-coeffect context :db))
    (when-let [event (tracking/metrics-event (interceptor/get-coeffect context :event))]
      (if (or (seq? event) (vector? event))
        (doseq [e event]
          (native-module/add-centralized-metric e))
        (native-module/add-centralized-metric event))))
  context)

;; ignore warning, interceptor used from interceptor_metrics.cljc
#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def interceptor
  (interceptor/->interceptor
   :id    :centralized-metrics
   :after centralized-metrics-interceptor))

(rf/reg-event-fx :centralized-metrics/toggle-centralized-metrics
 (fn [{:keys [db]} [enabled? onboarding?]]
   {:fx [[:effects.centralized-metrics/toggle-metrics enabled?]]
    :db (-> db
            (assoc :centralized-metrics/user-confirmed? true)
            (assoc :centralized-metrics/enabled? enabled?)
            (assoc :centralized-metrics/onboarding-enabled? (and onboarding? enabled?)))}))

(rf/reg-event-fx :centralized-metrics/check-user-confirmation
 (fn [{:keys [db]}]
   (when-not (:centralized-metrics/user-confirmed? db)
     {:fx [[:dispatch [:navigate-to :screen/onboarding.share-usage]]]})))

(rf/reg-fx :effects.centralized-metrics/track
 (fn [event]
   (native-module/add-centralized-metric event)))

(rf/reg-event-fx
 :centralized-metrics/track
 (fn [{:keys [db]} [event-name data]]
   (let [event-id (name event-name)]
     (when (push-event? db)
       {:fx [[:effects.centralized-metrics/track
              (tracking/key-value-event event-id data)]]}))))
