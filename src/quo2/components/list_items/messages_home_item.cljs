(ns quo2.components.list-items.messages-home-item
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [status-im.utils.handlers :refer [<sub >evt]]
            [quo2.components.avatars.user-avatar :as user-avatar]
            [quo2.foundations.typography :as typography]
            [clojure.string :as str]
            [status-im.utils.utils :as utils.utils]
            [status-im.utils.datetime :as time]
            [status-im.i18n.i18n :as i18n]
            [quo2.components.notifications.notification-dot :refer [notification-dot]]))


(defn messages-home-item [{:keys [chat-id] :as message}]
  (let [display-name (first (<sub [:contacts/contact-two-names-by-identity chat-id]))]
    (println "asdfasdf" message)
    [rn/view
     [user-avatar/user-avatar {:full-name         display-name
                               :status-indicator? true
                               :online?           true
                               :size              :small
                               :profile-picture   nil
                               :ring?             false}]
     ]))
