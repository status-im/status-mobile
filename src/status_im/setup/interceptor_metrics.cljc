(ns status-im.setup.interceptor-metrics
  (:require
    #?@(:mobile
          [[re-frame.core :as re-frame]
           [status-im.contexts.centralized-metrics.events :as centralized-metrics]])))

(defn setup-centralized-metrics-interceptor
  []
  #?(:mobile
       (re-frame/reg-global-interceptor centralized-metrics/interceptor)))
