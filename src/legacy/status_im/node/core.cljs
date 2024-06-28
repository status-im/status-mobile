(ns legacy.status-im.node.core
  (:require
    [legacy.status-im.utils.deprecated-types :as types]
    [status-im.config :as config]))

(defn fleets
  [{:keys [custom-fleets]}]
  (as-> [(js/require "./fleets.js")] $
    (mapv #(:fleets (types/json->clj %)) $)
    (conj $ custom-fleets)
    (reduce merge $)))

(defn current-fleet-key
  [db]
  (keyword (get-in db
                   [:profile/profile :fleet]
                   config/fleet)))
