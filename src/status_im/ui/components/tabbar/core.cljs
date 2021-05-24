(ns status-im.ui.components.tabbar.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]))

(defn get-height []
  (if platform/android?
    56
    (if platform/iphone-x?
      84
      50)))

(defn chat-tab []
  (let [count-subscription @(re-frame/subscribe [:chats/unread-messages-number])]
    (re-frame/dispatch [:change-tab-count :chat count-subscription])
    nil))

(defn profile-tab []
  (let [count-subscription @(re-frame/subscribe [:get-profile-unread-messages-number])]
    (re-frame/dispatch [:change-tab-count :profile count-subscription])
    nil))

(defn tabs-counts-subscriptions []
  [:<>
   [chat-tab]
   [profile-tab]])