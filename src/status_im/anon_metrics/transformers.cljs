(ns status-im.anon-metrics.transformers)

(defn navigate-to-txf [context]
  {:view-id (second context)
   :params (-> context
               (nth 2)
               (select-keys [:screen]))})

(def transformations
  {:navigate-to navigate-to-txf})

(defn transform [ctx]
  (when-let [txf (-> ctx first transformations)]
    (txf ctx)))
