(ns status-im.utils.handlers
  (:require [re-frame.core :refer [after dispatch debug] :as re-core]))

(defn side-effect!
  "Middleware for handlers that will not affect db."
  [handler]
  (fn [db params]
    (handler db params)
    db))

(defn register-handler
  ([name handler] (register-handler name nil handler))
  ([name middleware handler]
   (re-core/register-handler name [debug middleware] handler)))
