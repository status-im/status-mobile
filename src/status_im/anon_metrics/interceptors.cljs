(ns status-im.anon-metrics.interceptors
  (:require [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]
            [re-frame.interceptor :refer [->interceptor]]
            [status-im.utils.platform :as platform]
            [status-im.utils.build :as build]
            [status-im.anon-metrics.transformers :as txf]))

(defn transform-and-log [context]
  (log/info :catch-event-fn (get-in context [:coeffects :event]))
  (when-let [transformed-payload (txf/transform context)]
    (json-rpc/call {:method "appmetrics_saveAppMetrics"
                    :params [[{:event (-> context :coeffects :event first)
                               :value transformed-payload
                               :app_version build/version
                               :os platform/os}]]
                    :on-failure #(log/error)})))

(defn catch-events-before [context]
  (log/info "catch-events/interceptor fired")
  (transform-and-log context)
  context)

(def catch-events
  (->interceptor
   :id     :catch-events
   :before catch-events-before))
