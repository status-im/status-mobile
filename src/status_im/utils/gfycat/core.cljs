(ns status-im.utils.gfycat.core
  (:require [status-im.utils.gfycat.animals :as animals]
            [status-im.utils.gfycat.adjectives :as adjectives]
            [clojure.string :as str]
            [status-im.utils.random :as rnd]
            [status-im.utils.datetime :refer [now-ms]]))

(defn- pick-random
  [gen vector]
  (str/capitalize (rnd/seeded-rand-nth gen vector)))

(defn generate-gfy
  ([public-key]
  (let [gen (rnd/rand-gen public-key)
        first-adjective (pick-random gen adjectives/data)
        second-adjective (pick-random gen adjectives/data)
        animal (pick-random gen animals/data)]
    (str first-adjective " " second-adjective " " animal)))
  ([] (generate-gfy (now-ms))))
