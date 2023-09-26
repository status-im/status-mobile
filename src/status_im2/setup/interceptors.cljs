(ns status-im2.setup.interceptors
  (:require [re-frame.core :as re-frame]
            [re-frame.std-interceptors :as std-interceptors]))

(defn register-global-interceptors
  []
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
