(ns status-im.anon-metrics.transformers)

(defn navigate-to-txf [context]
  (let [event (-> context :coeffects :event)]
    {:view_id (second event)
     :params  {:screen (get-in event [1 :screen] "")}}))

(defn screens-on-will-focus-txf [context]
  (let [view-id (-> context :coeffects :event second)]
    ;; we need this check because in some cases, :screens/on-will-focus is dispatched
    ;; after navigate to this check ensures that only the events that are not handled
    ;; via navigate to are captured hence avoiding duplicates
    (when (#{:chat :empty-tab :wallet :status :my-profile} view-id)
      {:view_id view-id
       :params {:screen ""}})))

(def value-transformations
  {:navigate-to navigate-to-txf
   :screens/on-will-focus screens-on-will-focus-txf})

(defn transform [ctx]
  (when-let [txf (-> ctx :coeffects :event first value-transformations)]
    (txf ctx)))

