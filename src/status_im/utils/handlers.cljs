(ns status-im.utils.handlers
  (:require
   [re-frame.core :as re-frame]
   [re-frame.interceptor :refer [->interceptor get-coeffect]]
   [taoensso.timbre :as log]
   [utils.debounce :as debounce]))

(defn- pretty-print-event
  [ctx]
  (let [[first _] (get-coeffect ctx :event)]
    first))

(def handler-nesting-level (atom 0))

(def debug-handlers-names
  "Interceptor which logs debug information to js/console for each event."
  (->interceptor
   :id     :debug-handlers-names
   :before (fn debug-handlers-names-before
             [context]
             (when js/goog.DEBUG
               (reset! handler-nesting-level 0))
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

(defn >evt-once
  [event]
  (debounce/dispatch-and-chill event 3000))
