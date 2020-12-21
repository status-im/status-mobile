(ns status-im.utils.handlers
  (:require [re-frame.core :as re-frame]
            [re-frame.interceptor :refer [->interceptor get-coeffect]]
            [taoensso.timbre :as log]))

(defn- parse-json
  ;; NOTE(dmitryn) Expects JSON response like:
  ;; {"error": "msg"} or {"result": true}
  [s]
  (try
    (let [res (-> s
                  js/JSON.parse
                  (js->clj :keywordize-keys true))]
      ;; NOTE(dmitryn): AddPeer() may return {"error": ""}
      ;; assuming empty error is a success response
      ;; by transforming {"error": ""} to {:result true}
      (if (and (:error res)
               (= (:error res) ""))
        {:result true}
        res))
    (catch :default ^js e
      {:error (.-message e)})))

(defn response-handler [success-fn error-fn]
  (fn handle-response
    ([response]
     (let [{:keys [error result]} (parse-json response)]
       (handle-response error result)))
    ([error result]
     (if error
       (error-fn error)
       (success-fn result)))))

(defn- pretty-print-event [ctx]
  (let [[first _] (get-coeffect ctx :event)]
    first))

(def debug-handlers-names
  "Interceptor which logs debug information to js/console for each event."
  (->interceptor
   :id     :debug-handlers-names
   :before (fn debug-handlers-names-before
             [context]
             (log/debug "Handling re-frame event: " (pretty-print-event context))
             context)))

(defn register-handler-fx
  ([name handler]
   (register-handler-fx name nil handler))
  ([name interceptors handler]
   (re-frame/reg-event-fx
    name
    [debug-handlers-names (re-frame/inject-cofx :now) interceptors]
    handler)))

(def <sub (comp deref re-frame/subscribe))

(def >evt re-frame/dispatch)
