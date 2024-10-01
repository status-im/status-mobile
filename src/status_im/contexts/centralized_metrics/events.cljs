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
  (let [rf-event           (interceptor/get-coeffect context :event)
        rf-db              (interceptor/get-effect context :db)
        metrics-event-data (get rf-db :centralized-metrics/event-data)
        metrics-event      (tracking/tracked-event {:rf-event     rf-event
                                                    :metrics-data metrics-event-data})]
    (when metrics-event
      (log/info "tracking event" metrics-event)
      (when (push-event? rf-db)
        (native-module/add-centralized-metric metrics-event)))
    (interceptor/assoc-effect context
                              :db
                              (if metrics-event (dissoc rf-db :centralized-metrics/event-data) rf-db))))

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
