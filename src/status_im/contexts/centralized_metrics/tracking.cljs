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
    :screen/onboarding.welcome})

(defn track-view-id-event
  [view-id]
  (when (contains? view-ids-to-track view-id)
    (navigation-event (name view-id))))

(defn tracked-event
  [{:keys [rf-event metrics-data]}]
  (let [[event-name second-parameter] rf-event]
    (case event-name
      :profile/get-profiles-overview-success
      (user-journey-event app-started-event)

      :centralized-metrics/toggle-centralized-metrics
      (key-value-event "events.metrics-enabled" {:enabled second-parameter})

      :set-view-id
      (track-view-id-event second-parameter)

      :wallet-connect/approve-session-success
      (key-value-event "dapp-session"
                       (merge metrics-data
                              {:action :approved
                               :result :success}))

      :wallet-connect/approve-session-fail
      (key-value-event "dapp-session"
                       (merge metrics-data
                              {:action :approved
                               :result :fail}))

      :wallet-connect/reject-session-proposal-success
      (key-value-event "dapp-session"
                       (merge metrics-data
                              {:action :rejected
                               :result :success}))

      :wallet-connect/reject-session-proposal-fail
      (key-value-event "dapp-session"
                       (merge metrics-data
                              {:action :rejected
                               :result :fail}))

      :wallet-connect/disconnect-dapp-success
      (key-value-event "dapp-disconnected"
                       (merge metrics-data
                              {:result :success}))

      :wallet-connect/disconnect-dapp-fail
      (key-value-event "dapp-disconnected"
                       (merge metrics-data
                              {:result :fail}))

      :wallet-connect/on-new-session
      (key-value-event "dapp-connected" metrics-data)

      :wallet-connect/respond-sign-message-success
      (key-value-event "dapp-sign" (merge metrics-data {:result :success}))

      :wallet-connect/on-sign-error
      (key-value-event "dapp-sign" (merge metrics-data {:result :fail}))

      :wallet-connect/respond-send-transaction-success
      (key-value-event "dapp-send" (merge metrics-data {:result :success}))

      :wallet-connect/respond-send-transaction-error
      (key-value-event "dapp-send" (merge metrics-data {:result :fail}))

      nil)))
