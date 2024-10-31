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
  (when (push-event? (interceptor/get-coeffect context :db))
    (when-let [event (tracking/metrics-event (interceptor/get-coeffect context :event))]
      (log/debug "tracking event" event)
      (if (or (seq? event) (vector? event))
        (doseq [e event]
          (native-module/add-centralized-metric e))
        (native-module/add-centralized-metric event))))
  context)

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

(rf/reg-event-fx :centralized-metrics/check-modal
 (fn [{:keys [db]} [modal-view]]
   (when-not (:centralized-metrics/user-confirmed? db)
     {:fx [[:dispatch
            [:show-bottom-sheet
             {:content  (fn [] [modal-view])
              ;; When in the profiles screen do biometric auth after the metrics sheet is dismissed
              ;; https://github.com/status-im/status-mobile/issues/20932
              :on-close (when (= (:view-id db) :screen/profile.profiles)
                          #(rf/dispatch [:profile.login/login-with-biometric-if-available
                                         (get-in db [:profile/login :key-uid])]))
              :shell?   true}]]]})))

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
