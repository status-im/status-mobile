(ns status-im.fleet.default-fleet (:require-macros [status-im.utils.slurp :refer [slurp]]))

(def default-fleets
  (slurp "resources/config/fleets.json"))
