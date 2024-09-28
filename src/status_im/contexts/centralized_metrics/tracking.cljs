(ns status-im.contexts.centralized-metrics.tracking
  (:require
    [clojure.string]
    [legacy.status-im.utils.build :as build]
    [react-native.platform :as platform]
    [status-im.navigation.screens :as screens]))

(defn key-value-event
  [event-name val-key value]
  {:metric
   {:eventName  event-name
    :platform   platform/os
    :appVersion build/app-short-version
    :eventValue {val-key value}}})

(defn user-journey-event
  [action]
  (key-value-event "user-journey" :action action))

(defn navigation-event
  [view-id]
  (key-value-event "navigation" :viewId view-id))

(defn screen-event
  [screen event-data]
  (let [screen-id (:name screen)
        event-id  (get-in screen [:metrics :event :id] screen-id)]
    {:metric
     {:eventName  (name event-id)
      :platform   platform/os
      :appVersion build/app-short-version
      :eventValue (assoc event-data
                         :viewId   (name screen-id)
                         :viewName (-> screen-id symbol str))}}))

(def ^:const app-started-event "app-started")

(def ^:const view-ids-to-track
  #{;; Tabs
    :communities-stack
    :chats-stack
    :wallet-stack})

(defn track-view-id-event
  [view-id]
  (let [screens-by-name screens/onboarding-screens-by-name]
    (if-let [screen (get screens-by-name view-id)]
      (when (get-in screen [:metrics :track?] false)
        [(navigation-event (name view-id))
         (screen-event screen {})])
      (when (contains? view-ids-to-track view-id)
        (navigation-event (name view-id))))))

(defn tracked-event
  [[event-name second-parameter]]
  (case event-name
    :profile/get-profiles-overview-success
    (user-journey-event app-started-event)

    :centralized-metrics/toggle-centralized-metrics
    (key-value-event "events.metrics-enabled" :enabled second-parameter)

    :set-view-id
    (track-view-id-event second-parameter)

    nil))
