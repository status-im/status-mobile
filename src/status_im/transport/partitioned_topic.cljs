(ns status-im.transport.partitioned-topic
  (:require [status-im.utils.random :as random]
            [status-im.transport.utils :as utils]
            [status-im.constants :as constants]
            [status-im.utils.config :as config]))

;; Number of different personal topics
(def n-partitions 5000)

(defn expected-number-of-collisions
  "Expected number of topic collision given the number of expected users,
  we want this value to be greater than a threshold to avoid positive
  identification given the attacker has a topic & public key.
  Used only for safety-checking n-partitions.

  https://en.wikipedia.org/wiki/Birthday_problem#Collision_counting"
  [total-users]
  (+
   (- total-users n-partitions)
   (* n-partitions
      (js/Math.pow
       (/
        (- n-partitions 1)
        n-partitions)
       total-users))))

(defn- partitioned-topic
  [public-key]
  (let [gen (random/rand-gen public-key)]
    (-> (random/seeded-rand-int gen n-partitions)
        (str "-discovery"))))

(defn partitioned-topic-hash
  "Given a public key return a partitioned topic between 0 and n"
  [public-key]
  (-> public-key
      partitioned-topic
      utils/get-topic))

(def discovery-topic constants/contact-discovery)
(defn discovery-topic-hash [] (utils/get-topic constants/contact-discovery))

(defn public-key->discovery-topic
  [public-key]
  (if config/partitioned-topic-enabled?
    (partitioned-topic public-key)
    constants/contact-discovery))

(defn public-key->discovery-topic-hash
  [public-key]
  (if config/partitioned-topic-enabled?
    (partitioned-topic-hash public-key)
    (discovery-topic-hash)))

(defn discovery-topics [public-key]
  [(partitioned-topic-hash public-key) (discovery-topic-hash)])

(defn contains-topic?
  [available-topics topic]
  (contains? available-topics (utils/get-topic topic)))
