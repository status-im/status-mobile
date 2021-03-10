(ns status-im.anon-metrics.transformers
  "`status-go` defines the shape of expected events so that we don't overcollect data (by mistake or intentionally).
  `transformers` ns transform the event payload to match the expected shape on `status-go` side.")

(defn navigate-to-txf [event]
  {:view-id (second event)
   :params (-> event
               (nth 2)
               (select-keys [:screen]))})

(def transformations
  {:navigate-to navigate-to-txf})

(defn transform [ctx]
  (let [event (-> ctx :coeffects :event)]
    (when-let [txf (-> event first transformations)]
      (txf event))))
