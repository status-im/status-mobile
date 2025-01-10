(ns legacy.status-im.node.core
  (:require
    [legacy.status-im.utils.deprecated-types :as types]))

(defn fleets
  [{:keys [custom-fleets]}]
  (as-> [(js/require "./fleets.js")] $
    (mapv #(:fleets (types/json->clj %)) $)
    (conj $ custom-fleets)
    (reduce merge $)))
