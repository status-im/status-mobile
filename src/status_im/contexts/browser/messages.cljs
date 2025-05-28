(ns status-im.contexts.browser.messages
  (:require [camel-snake-kebab.extras :as cske]
            [oops.core :as oops]
            [utils.transforms :as transforms]))

(defn- parse-message-data
  [data]
  (->> data
       transforms/json->clj
       (cske/transform-keys transforms/->kebab-case-keyword)))

(defn parse-native-event
  [js-event]
  (let [event (->> (oops/oget js-event "nativeEvent")
                   transforms/js->clj
                   (cske/transform-keys transforms/->kebab-case-keyword))]
    (update event :data parse-message-data)))

(defn event-topic
  [event]
  (-> event :data :topic))

(defn event-payload
  [event]
  (-> event :data :payload))

(defn get-webview-event-data
  [event]
  (-> event
      (select-keys [:url :can-go-forward :can-go-back])))

(defn get-website-metadata
  [event]
  (-> event
      event-payload
      (select-keys [:page-title :logo-url :origin-url])))
