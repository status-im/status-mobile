(ns status-im.utils.gfycat.core
  (:require [status-im.utils.gfycat.animals :as animals]
            [status-im.utils.gfycat.adjectives :as adjectives]
            [clojure.string :as str]))

(defn- pick-random
  [vector]
  (-> (rand-nth vector)
      str/capitalize))

(defn generate-gfy
  []
  (let [first-adjective (pick-random adjectives/data)
        second-adjective (pick-random adjectives/data)
        animal (pick-random animals/data)]
    (str first-adjective " " second-adjective " " animal)))