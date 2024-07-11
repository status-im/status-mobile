(ns status-im.setup.interceptors
  (:require
    [re-frame.core :as re-frame]
    [re-frame.std-interceptors :as std-interceptors]
    [status-im.contexts.centralized-metrics.events :as centralized-metrics]
    [utils.re-frame :as rf]))

(defn register-global-interceptors
  []
  (re-frame/reg-global-interceptor rf/debug-handlers-names)
  (re-frame/reg-global-interceptor centralized-metrics/interceptor)
  (re-frame/reg-global-interceptor (re-frame/inject-cofx :now))

  ;; Interceptor `trim-v` removes the first element of the event vector.
  ;;
  ;; Without the interceptor:
  ;;
  ;;   (re-frame/reg-event-fx
  ;;    :some-event-id
  ;;    (fn [{:keys [db]} [_event-id arg1 arg2]]
  ;;      {:db ...
  ;;       :fx [...]}))
  ;;
  ;; With the interceptor:
  ;;
  ;;   (re-frame/reg-event-fx
  ;;    :some-event-id
  ;;    (fn [{:keys [db]} [arg1 arg2]]
  ;;      {:db ...
  ;;       :fx [...]}))
  ;;
  (re-frame/reg-global-interceptor std-interceptors/trim-v))
