(ns status-im.contexts.centralized-metrics.tracking
  (:require
    [clojure.string]
    [legacy.status-im.utils.build :as build]
    [react-native.platform :as platform]
    [status-im.navigation.screens :as screens]))

(defn key-value-event
  [event-name event-value]
  {:metric
   {:eventName  event-name
    :platform   platform/os
    :appVersion build/app-short-version
    :eventValue event-value}})

(defn user-journey-event
  [action]
  (key-value-event "user-journey" {:action action}))

(defn navigation-event
  [view-id]
  (key-value-event "navigation" {:viewId view-id}))

(defn screen-event
  [screen event-data]
  (let [screen-id (:name screen)
        view-id   (get-in screen [:metrics :alias-id] screen-id)]
    {:metric
     {:eventName  "navigation"
      :platform   platform/os
      :appVersion build/app-short-version
      :eventValue (assoc event-data :viewId (name view-id))}}))

(def ^:const app-started-event "app-started")

(def ^:const view-ids-to-track
  #{;; Tabs
    :communities-stack
    :chats-stack
    :wallet-stack})

(defn track-view-id-event
  [view-id]
  (if-let [screen (get screens/screens-by-name view-id)]
    (when (get-in screen [:metrics :track?])
      (screen-event screen {}))
    (when (contains? view-ids-to-track view-id)
      (navigation-event (name view-id)))))

(defn navigated-to-collectibles-tab-event
  [location]
  (key-value-event "navigated-to-collectibles-tab" {:location location}))

(defn metrics-event
  [[rf-event-name rf-event-parameter]]
  (case rf-event-name
    :profile/get-profiles-overview-success
    (user-journey-event app-started-event)

    :centralized-metrics/toggle-centralized-metrics
    (key-value-event "events.metrics-enabled" {:enabled rf-event-parameter})

    :set-view-id
    (track-view-id-event rf-event-parameter)

    :wallet/select-account-tab
    (when (= rf-event-parameter :collectibles)
      (navigated-to-collectibles-tab-event :account))

    :wallet/select-home-tab
    (when (= rf-event-parameter :collectibles)
      (navigated-to-collectibles-tab-event :home))

    nil))
