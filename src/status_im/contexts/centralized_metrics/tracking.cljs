(ns status-im.contexts.centralized-metrics.tracking
  (:require
    [legacy.status-im.utils.build :as build]
    [react-native.platform :as platform]))

(defn user-journey-event
  [action]
  {:metric
   {:eventName  "user-journey"
    :platform   platform/os
    :appVersion build/app-short-version
    :eventValue {:action action}}})

(def ^:const app-started-event "app-started")
(def ^:const navigate-to-create-profile-event "navigate-to-create-profile")
(def ^:const communities-tab-clicked "communities-tab-clicked")
(def ^:const wallet-tab-clicked "wallet-tab-clicked")
(def ^:const chats-tab-clicked "chats-tab-clicked")

(defn track-view-id-event
  [view-id]
  (case view-id
    :communities-stack (user-journey-event communities-tab-clicked)
    :chats-stack       (user-journey-event chats-tab-clicked)
    :wallet-stack      (user-journey-event wallet-tab-clicked)
    nil))

(defn tracked-event
  [[event-name second-parameter]]
  (case event-name
    :onboarding/navigate-to-create-profile
    (user-journey-event navigate-to-create-profile-event)

    :profile/get-profiles-overview-success
    (user-journey-event app-started-event)

    :set-view-id
    (track-view-id-event second-parameter)

    nil))
