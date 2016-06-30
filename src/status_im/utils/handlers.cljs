(ns status-im.utils.handlers
  (:require [re-frame.core :refer [after dispatch debug] :as re-core]
            [re-frame.utils :refer [log]]))

(defn side-effect!
  "Middleware for handlers that will not affect db."
  [handler]
  (fn [db params]
    (handler db params)
    db))

(defn debug-handlers-names
  "Middleware which logs debug information to js/console for each event.
  Includes a clojure.data/diff of the db, before vs after, showing the changes
  caused by the event."
  [handler]
  (fn debug-handler
    [db v]
    (log "Handling re-frame event: " (first v))
    (let [new-db  (handler db v)]
      new-db)))

(defn register-handler
  ([name handler] (register-handler name nil handler))
  ([name middleware handler]
   (re-core/register-handler name [debug-handlers-names middleware] handler)))
