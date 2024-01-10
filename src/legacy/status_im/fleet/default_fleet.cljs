(ns legacy.status-im.fleet.default-fleet (:require-macros [legacy.status-im.utils.slurp :refer [slurp]]))

(def default-fleets
  (slurp "resources/config/fleets.json"))
