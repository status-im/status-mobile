(ns status-im.contexts.centralized-metrics.tracking
  (:require
    [legacy.status-im.utils.build :as build]
    [react-native.platform :as platform]))

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

(def ^:const app-started-event "app-started")

(def ^:const view-ids-to-track
  #{;; Tabs
    :communities-stack
    :chats-stack
    :wallet-stack

    ;; Onboarding
    :screen/onboarding.intro
    :screen/onboarding.new-to-status
    :screen/onboarding.sync-or-recover-profile
    :screen/onboarding.enter-seed-phrase
    :screen/onboarding.create-profile
    :screen/onboarding.create-profile-password
    :screen/onboarding.enable-biometrics
    :screen/onboarding.generating-keys
    :screen/onboarding.enable-notifications
    :screen/onboarding.sign-in-intro
    :screen/onboarding.sign-in
    :screen/onboarding.syncing-progress
    :screen/onboarding.syncing-progress-intro
    :screen/onboarding.syncing-results
    :screen/onboarding.welcome})

(defn track-view-id-event
  [view-id]
  (when (contains? view-ids-to-track view-id)
    (navigation-event (name view-id))))

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
