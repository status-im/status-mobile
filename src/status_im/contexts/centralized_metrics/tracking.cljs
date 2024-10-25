(ns status-im.contexts.centralized-metrics.tracking
  (:require
    [legacy.status-im.utils.build :as build]
    [react-native.platform :as platform]))

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
    :screen/onboarding.preparing-status
    :screen/onboarding.sign-in-intro
    :screen/onboarding.sign-in
    :screen/onboarding.syncing-progress
    :screen/onboarding.syncing-progress-intro
    :screen/onboarding.syncing-results
    :screen/onboarding.welcome

    ;; Collectibles
    :screen/wallet.collectible})

(defn track-view-id-event
  [view-id]
  (when (contains? view-ids-to-track view-id)
    (navigation-event (name view-id))))

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
